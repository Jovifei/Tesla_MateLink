import UserNotifications
import BackgroundTasks

@MainActor
final class NotificationManager: NSObject, ObservableObject, UNUserNotificationCenterDelegate {
    static let shared = NotificationManager()

    // MARK: - Notification Categories (T-201)

    enum Category: String, CaseIterable {
        case sentry = "SENTRY_ALERT"
        case charging = "CHARGING_COMPLETE"
        case tpms = "TPMS_ALERT"
        case update = "UPDATE_AVAILABLE"
        case mileage = "MILEAGE_MILESTONE"
        case battery = "BATTERY_HEALTH"
        case carState = "CAR_STATE_CHANGE"
    }

    // MARK: - Authorization (T-201)

    func requestAuthorization() {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .sound, .badge]) { granted, error in
            if let error = error {
                print("[NotificationManager] Authorization error: \(error)")
            }
            if !granted {
                print("[NotificationManager] Authorization denied — notifications disabled")
            }
        }
    }

    // MARK: - Send Notification (T-201)

    func sendNotification(title: String, body: String, category: Category) {
        let content = UNMutableNotificationContent()
        content.title = title
        content.body = body
        content.sound = .default
        content.categoryIdentifier = category.rawValue

        let request = UNNotificationRequest(
            identifier: UUID().uuidString,
            content: content,
            trigger: nil
        )
        UNUserNotificationCenter.current().add(request)
    }

    // MARK: - Background Task Framework (T-202)

    private static let backgroundTaskID = "com.teslamatelink.statusRefresh"
    private static let refreshInterval: TimeInterval = 15 * 60
    private static let lowTirePressureThreshold: Double = 2.0  // bar
    private static let milestoneKmInterval = 10000

    // MARK: - Notification Strings (localization-ready)

    private enum Strings {
        static let sentryTitle = "Sentry Mode Armed"
        static let sentryBody = "Vehicle sentry mode is now active"
        static let chargingTitle = "Charging Complete"
        static let tpmsTitle = "Low Tire Pressure"
        static let tpmsBodyPrefix = "Check tires:"
        static let milestoneTitle = "Milestone Reached"
    }

    // MARK: - Configuration (S2 fix: register categories + foreground delegate)

    func configure() {
        let center = UNUserNotificationCenter.current()
        center.delegate = self

        let categories = Category.allCases.map { cat in
            UNNotificationCategory(
                identifier: cat.rawValue,
                actions: [],
                intentIdentifiers: [],
                options: []
            )
        }
        center.setNotificationCategories(Set(categories))
    }

    // MARK: - Foreground Display (S2 fix)

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        completionHandler([.banner, .sound])
    }

    func registerBackgroundTasks() {
        BGTaskScheduler.shared.register(
            forTaskWithIdentifier: Self.backgroundTaskID,
            using: nil
        ) { [weak self] task in
            guard let refreshTask = task as? BGAppRefreshTask else {
                task.setTaskCompleted(success: false)
                return
            }
            Task { @MainActor [weak self] in
                self?.handleBackgroundRefresh(refreshTask)
            }
        }
    }

    private func handleBackgroundRefresh(_ task: BGAppRefreshTask) {
        // Schedule next refresh BEFORE doing work — if the OS kills this task
        // before async completes, the chain still continues.
        scheduleNextRefresh()

        task.expirationHandler = {
            task.setTaskCompleted(success: false)
        }

        Task {
            await checkForStatusChanges()
            task.setTaskCompleted(success: true)
        }
    }

    func scheduleNextRefresh() {
        let request = BGAppRefreshTaskRequest(identifier: Self.backgroundTaskID)
        request.earliestBeginDate = Date(timeIntervalSinceNow: Self.refreshInterval)
        try? BGTaskScheduler.shared.submit(request)
    }

    // MARK: - Previous Status Tracking

    private var hasCheckedOnce = false
    private var lastSentryMode: Bool = false
    private var lastChargingState: CarState = .offline
    private var lastMileage: Int = 0
    private var lastBatteryLevel: Int = 0
    private var lastLowTires: Set<String> = []
    private var lastNotifiedVersion: String = ""
    private var lastNotifiedSoH: Int = 100

    // MARK: - Status Change Detection (T-203~T-207)

    func checkForStatusChanges() async {
        guard let state = AppState.shared,
              let api = state.real,
              let carId = state.currentCar?.id else { return }

        do {
            let status: CarStatus = try await api.fetch("/api/v1/cars/\(carId)/status")

            // Skip notifications on first check — only seed baseline values
            guard hasCheckedOnce else {
                lastSentryMode = status.sentryMode
                lastChargingState = status.state
                lastBatteryLevel = status.batteryLevel
                lastMileage = status.odometer
                if let tp = status.tirePressure {
                    var lowTires: Set<String> = []
                    if tp.frontLeft < Self.lowTirePressureThreshold { lowTires.insert("FL") }
                    if tp.frontRight < Self.lowTirePressureThreshold { lowTires.insert("FR") }
                    if tp.rearLeft < Self.lowTirePressureThreshold { lowTires.insert("RL") }
                    if tp.rearRight < Self.lowTirePressureThreshold { lowTires.insert("RR") }
                    lastLowTires = lowTires
                }
                hasCheckedOnce = true
                return
            }

            // T-203: Sentry mode armed
            if status.sentryMode && !lastSentryMode {
                sendNotification(
                    title: Strings.sentryTitle,
                    body: Strings.sentryBody,
                    category: .sentry
                )
            }
            lastSentryMode = status.sentryMode

            // T-204: Charging complete
            if status.state == .charging && status.batteryLevel >= status.chargeLimitSoc {
                if lastChargingState != .charging || lastBatteryLevel < status.chargeLimitSoc {
                    let energy = String(format: "%.1f", status.chargeEnergyAdded)
                    sendNotification(
                        title: Strings.chargingTitle,
                        body: "Battery at \(status.batteryLevel)% — \(energy) kWh added",
                        category: .charging
                    )
                }
            }
            lastChargingState = status.state
            lastBatteryLevel = status.batteryLevel

            // T-205: TPMS low pressure alert
            if let tp = status.tirePressure {
                var lowTires: Set<String> = []
                if tp.frontLeft < Self.lowTirePressureThreshold { lowTires.insert("FL") }
                if tp.frontRight < Self.lowTirePressureThreshold { lowTires.insert("FR") }
                if tp.rearLeft < Self.lowTirePressureThreshold { lowTires.insert("RL") }
                if tp.rearRight < Self.lowTirePressureThreshold { lowTires.insert("RR") }
                if !lowTires.isEmpty && lowTires != lastLowTires {
                    sendNotification(
                        title: Strings.tpmsTitle,
                        body: "\(Strings.tpmsBodyPrefix) \(lowTires.sorted().joined(separator: ", "))",
                        category: .tpms
                    )
                }
                lastLowTires = lowTires
            }

            // T-206: Software update available
            if let sw = status.swVersion, !sw.isEmpty, sw != lastNotifiedVersion {
                if !lastNotifiedVersion.isEmpty {
                    sendNotification(
                        title: "Update Available",
                        body: "Software version \(sw) is now available",
                        category: .update
                    )
                }
                lastNotifiedVersion = sw
            }

            // T-207: Battery health warning (SoH drops below 80%)
            if let soh = status.batterySoH {
                if soh < 80 && lastNotifiedSoH >= 80 {
                    sendNotification(
                        title: "Battery Health Warning",
                        body: "Battery health has dropped to \(soh)%",
                        category: .battery
                    )
                }
                lastNotifiedSoH = soh
            }

            // T-207: Milestone notifications
            let mileageKm = status.odometer
            if lastMileage > 0 {
                let prevK = lastMileage / Self.milestoneKmInterval
                let currK = mileageKm / Self.milestoneKmInterval
                if currK > prevK {
                    sendNotification(
                        title: Strings.milestoneTitle,
                        body: "Your vehicle has passed \(currK * Self.milestoneKmInterval) km!",
                        category: .mileage
                    )
                }
            }
            lastMileage = mileageKm

        } catch {
            print("[NotificationManager] Status fetch failed: \(error)")
        }
    }
}
