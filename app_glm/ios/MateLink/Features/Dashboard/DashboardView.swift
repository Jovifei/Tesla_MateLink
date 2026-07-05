import SwiftUI

struct DashboardView: View {
    @EnvironmentObject var state: AppState
    @State private var status: CarStatus?
    @State private var showCarSwitcher = false
    @State private var isRefreshing = false
    let timer = Timer.publish(every: 5, on: .main, in: .common).autoconnect()

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    // ── Header ─────────────────────────────────────────
                    headerBar

                    if let s = status {
                        // ── Battery Card ───────────────────────────────────
                        StitchCard {
                            StitchLabel("BATTERY")
                            HStack(alignment: .bottom) {
                                Text("\(s.batteryLevel)%")
                                    .font(StitchFont.dataLg())
                                    .foregroundColor(StitchColors.onSurface)
                                Spacer()
                                Text("\(s.estBatteryRangeKm) km")
                                    .font(StitchFont.dataMd())
                                    .foregroundColor(StitchColors.onSurfaceVariant)
                            }
                            Spacer(minLength: 12)
                            // Progress bar
                            GeometryReader { geo in
                                ZStack(alignment: .leading) {
                                    Capsule()
                                        .fill(StitchColors.border)
                                        .frame(height: 4)
                                    Capsule()
                                        .fill(StitchColors.onSurface)
                                        .frame(width: geo.size.width * CGFloat(s.batteryLevel) / 100, height: 4)
                                }
                            }
                            .frame(height: 4)
                            Spacer(minLength: 8)
                            HStack {
                                Text("Limit: \(s.chargeLimitSoc)%")
                                    .font(StitchFont.labelCaps())
                                    .foregroundColor(StitchColors.onSurfaceVariant)
                                Spacer()
                                Text("\(s.odometer) km")
                                    .font(StitchFont.labelCaps())
                                    .foregroundColor(StitchColors.onSurfaceVariant)
                            }
                        }

                        // ── Charging Card (conditional) ────────────────────
                        if s.state == .charging {
                            StitchCard {
                                StitchLabel("CHARGING", color: StitchColors.statusCharging)
                                Spacer(minLength: 12)
                                HStack {
                                    StitchDataColumn(label: "Power", value: "\(Int(s.chargerPower)) kW")
                                    Spacer()
                                    StitchDataColumn(label: "Added", value: String(format: "%.1f kWh", s.chargeEnergyAdded), alignment: .center)
                                    Spacer()
                                    StitchDataColumn(label: "Remaining", value: String(format: "%.1fh", s.timeToFullCharge), alignment: .trailing)
                                }
                            }
                        }

                        // ── Climate Card ───────────────────────────────────
                        StitchCard {
                            StitchLabel("CLIMATE")
                            Spacer(minLength: 12)
                            HStack {
                                StitchDataColumn(label: "Inside", value: String(format: "%.1f°C", s.insideTemp))
                                Spacer()
                                StitchDataColumn(label: "Outside", value: String(format: "%.1f°C", s.outsideTemp), alignment: .center)
                                Spacer()
                                StitchDataColumn(label: "Climate", value: s.isClimateOn ? "ON" : "OFF", alignment: .trailing)
                            }
                        }

                        // ── Status Card ────────────────────────────────────
                        StitchCard {
                            StitchLabel("STATUS")
                            Spacer(minLength: 12)
                            StitchDataRow(label: "LOCK", value: s.locked ? "Locked" : "Unlocked")
                            Spacer(minLength: 12)
                            StitchDataRow(label: "SENTRY", value: s.sentryMode ? "Active" : "Off")
                            Spacer(minLength: 12)
                            StitchDataRow(label: "PLUG", value: s.chargePortDoorOpen ? "Connected" : "Disconnected")
                        }

                        // ── Tire Pressure Card ─────────────────────────────
                        if let t = s.tirePressure {
                            StitchCard {
                                StitchLabel("TIRE PRESSURE")
                                Spacer(minLength: 12)
                                HStack {
                                    StitchDataColumn(label: "FL", value: String(format: "%.1f", t.frontLeft))
                                    Spacer()
                                    StitchDataColumn(label: "FR", value: String(format: "%.1f", t.frontRight), alignment: .center)
                                    Spacer()
                                    StitchDataColumn(label: "RL", value: String(format: "%.1f", t.rearLeft), alignment: .center)
                                    Spacer()
                                    StitchDataColumn(label: "RR", value: String(format: "%.1f", t.rearRight), alignment: .trailing)
                                }
                            }
                        }

                        // ── Location Map Card ──────────────────────────────
                        locationMapCard

                        // ── Quick Access Card ──────────────────────────────
                        StitchCard {
                            StitchLabel("QUICK ACCESS")
                            Spacer(minLength: 16)
                            StitchDataRow(label: "DRIVES", value: "→")
                            Spacer(minLength: 12)
                            StitchDataRow(label: "CHARGES", value: "→")
                            Spacer(minLength: 12)
                            StitchDataRow(label: "BATTERY", value: "→")
                        }
                    } else {
                        // Loading state
                        VStack(spacing: 16) {
                            ProgressView()
                                .tint(StitchColors.onSurface)
                            Text("Loading...")
                                .font(StitchFont.bodySm())
                                .foregroundColor(StitchColors.onSurfaceVariant)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(60)
                    }

                    // Bottom spacing
                    Spacer(minLength: 24)
                }
                .padding(.horizontal, 24)
            }
            .background(StitchColors.background)
            .navigationBarHidden(true)
            .refreshable { await refresh() }
            .onReceive(timer) { _ in Task { await refresh() } }
            .task { await refresh() }
            .sheet(isPresented: $showCarSwitcher) { CarSwitcherView() }
        }
    }

    // MARK: - Header Bar

    private var headerBar: some View {
        HStack(alignment: .center) {
            Button(action: { showCarSwitcher.toggle() }) {
                HStack(spacing: 4) {
                    Text(state.currentCar?.name ?? "Tesla")
                        .font(StitchFont.headlineMd())
                        .foregroundColor(StitchColors.onSurface)
                    Image(systemName: "chevron.down")
                        .font(.system(size: 14, weight: .medium))
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
            }
            Spacer()
            if let s = status {
                StitchStatusChip(
                    text: StateColor.label(s.state).uppercased(),
                    isOnline: s.state == .online,
                    isCharging: s.state == .charging
                )
            }
            Button(action: { /* settings navigation handled by tab */ }) {
                Image(systemName: "gearshape")
                    .font(.system(size: 20))
                    .foregroundColor(StitchColors.onSurface)
            }
        }
        .padding(.top, 8)
    }

    // MARK: - Location Map Card

    private var locationMapCard: some View {
        StitchCard {
            StitchLabel("LOCATION")
            Spacer(minLength: 12)
            if let s = status {
                AmapView(latitude: s.latitude, longitude: s.longitude, title: "Current Location")
                    .frame(height: 150)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
            } else {
                Text("Loading map...")
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                    .frame(maxWidth: .infinity, minHeight: 150)
            }
        }
    }

    // MARK: - Refresh

    func refresh() async {
        guard !isRefreshing else { return }
        isRefreshing = true
        defer { isRefreshing = false }
        if state.isMockMode {
            status = await state.mock.mockStatus(state.currentCarId)
        } else if let api = state.real {
            status = try? await api.fetch("/api/v1/cars/\(state.currentCarId)/status")
        }
        writeWidgetData()
        if let s = status {
            PhoneWCSessionManager.shared.lastStatus = s
            PhoneWCSessionManager.shared.sendToWatch(s)
        }
    }

    // MARK: - Widget Data (T-001)

    private func writeWidgetData() {
        guard let s = status, let defaults = UserDefaults(suiteName: "group.com.teslamatelink") else { return }
        let renderer = ImageRenderer(content: CarImageView(color: state.carAccent, model: state.currentCar?.model ?? ""))
        if let uiImage = renderer.uiImage, let jpegData = uiImage.jpegData(compressionQuality: 0.5) {
            defaults.set(jpegData.base64EncodedString(), forKey: "carImageData")
        }
        defaults.set(state.currentCar?.model ?? "", forKey: "carModel")
        defaults.set(Int(s.batteryLevel), forKey: "widget_battery")
        defaults.set(s.estBatteryRangeKm, forKey: "widget_range")
        defaults.set(s.state.rawValue, forKey: "widget_state")
        defaults.set(s.locked, forKey: "widget_locked")
        defaults.set(s.sentryMode, forKey: "widget_sentry")
        defaults.set(s.chargePortDoorOpen, forKey: "widget_pluggedIn")
        defaults.set(s.insideTemp, forKey: "widget_insideTemp")
        defaults.set(s.chargerVoltage, forKey: "widget_chargerVoltage")
        defaults.set(s.chargerActualCurrent, forKey: "widget_chargerActualCurrent")
        defaults.set(s.chargePhases, forKey: "widget_chargePhases")
        defaults.set(s.chargeLimitSoc, forKey: "widget_chargeLimitSoc")
    }
}

// MARK: - Stitch Card

private struct StitchCard<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            content
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(24)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(StitchColors.border, lineWidth: 1)
        )
    }
}

// MARK: - Stitch Label (section header)

private struct StitchLabel: View {
    let text: String
    let color: Color

    init(_ text: String, color: Color = StitchColors.onSurfaceVariant) {
        self.text = text
        self.color = color
    }

    var body: some View {
        Text(text)
            .font(StitchFont.labelCaps())
            .foregroundColor(color)
    }
}

// MARK: - Stitch Data Column

private struct StitchDataColumn: View {
    let label: String
    let value: String
    var alignment: HorizontalAlignment = .leading

    var body: some View {
        VStack(alignment: alignment, spacing: 4) {
            Text(label)
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
            Text(value)
                .font(StitchFont.dataMd())
                .foregroundColor(StitchColors.onSurface)
        }
    }
}

// MARK: - Stitch Data Row

private struct StitchDataRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
            Spacer()
            Text(value)
                .font(StitchFont.bodyLg())
                .foregroundColor(StitchColors.onSurface)
        }
    }
}

// MARK: - Stitch Status Chip

private struct StitchStatusChip: View {
    let text: String
    var isOnline: Bool = false
    var isCharging: Bool = false

    private var backgroundColor: Color {
        if isOnline { return StitchColors.statusOnlineBg }
        if isCharging { return StitchColors.statusChargingBg }
        return StitchColors.statusOfflineBg
    }

    private var textColor: Color {
        if isOnline { return StitchColors.statusOnline }
        if isCharging { return StitchColors.statusCharging }
        return StitchColors.statusOffline
    }

    var body: some View {
        Text(text)
            .font(.system(size: 11, weight: .bold))
            .foregroundColor(textColor)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(backgroundColor)
            .clipShape(Capsule())
    }
}

// MARK: - CarImageView (preserved)

struct CarImageView: View {
    let color: Color; let model: String
    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: "car.fill")
                .font(.system(size: 64)).foregroundColor(color)
                .frame(height: 100)
            Text(model).font(StitchFont.bodySm()).foregroundColor(StitchColors.onSurfaceVariant)
        }.frame(maxWidth: .infinity).padding(.vertical, 8)
    }
}

// MARK: - CarSwitcherView (Stitch styled)

struct CarSwitcherView: View {
    @EnvironmentObject var state: AppState; @Environment(\.dismiss) var dismiss
    var body: some View {
        NavigationStack {
            List(state.cars) { car in
                Button(action: { state.currentCarId = car.id; dismiss() }) {
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(car.name)
                                .font(StitchFont.bodyLg())
                                .foregroundColor(StitchColors.onSurface)
                            Text("\(car.model) · \(car.totalDrives) drives")
                                .font(StitchFont.bodySm())
                                .foregroundColor(StitchColors.onSurfaceVariant)
                        }
                        Spacer()
                        if car.id == state.currentCarId {
                            Image(systemName: "checkmark")
                                .foregroundColor(StitchColors.accent)
                        }
                    }
                }
            }
            .scrollContentBackground(.hidden)
            .background(StitchColors.background)
            .navigationTitle("Select Vehicle")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}
