import Foundation
import WatchConnectivity
import ClockKit

// T-204: WatchConnectivity bridge between iPhone and Watch
// iPhone side sends car status updates; Watch side receives them

struct CarStatusUpdate: Codable {
    let batteryLevel: Int
    let range: Int
    let state: String
    let isClimateOn: Bool
    let isLocked: Bool
}

class WatchConnectivityManager: NSObject, ObservableObject, WCSessionDelegate {
    static let shared = WatchConnectivityManager()

    @Published var lastUpdate: CarStatusUpdate?

    private override init() { super.init() }

    func activate() {
        guard WCSession.isSupported() else { return }
        WCSession.default.delegate = self
        WCSession.default.activate()
    }

    func requestUpdate() {
        guard WCSession.default.isReachable else { return }
        WCSession.default.sendMessage(["action": "requestStatus"], replyHandler: { reply in
            self.handleUpdate(reply)
        })
    }

    // iPhone side: call this to push status to Watch
    func sendToWatch(status: CarStatus) {
        guard WCSession.default.activationState == .activated else { return }
        let update: [String: Any] = [
            "batteryLevel": status.batteryLevel,
            "range": status.estBatteryRangeKm,
            "state": status.state.rawValue,
            "isClimateOn": status.isClimateOn,
            "isLocked": status.locked
        ]
        if WCSession.default.isReachable {
            WCSession.default.sendMessage(update, replyHandler: nil)
        }
        // Also save to AppGroup for complication data
        let defaults = UserDefaults(suiteName: "group.com.teslamatelink")
        defaults?.set(status.batteryLevel, forKey: "batteryLevel")
        defaults?.set(status.estBatteryRangeKm, forKey: "range")
    }

    // MARK: - WCSessionDelegate

    func session(_ session: WCSession, activationDidCompleteWith activationState: WCSessionActivationState, error: Error?) {}

    func session(_ session: WCSession, didReceiveMessage message: [String: Any]) {
        handleUpdate(message)
    }

    func session(_ session: WCSession, didReceiveMessage message: [String: Any], replyHandler: @escaping ([String: Any]) -> Void) {
        handleUpdate(message)
        replyHandler(["ok": true])
    }

    #if os(iOS)
    func sessionDidBecomeInactive(_ session: WCSession) {}
    func sessionDidDeactivate(_ session: WCSession) {
        WCSession.default.activate()
    }
    #endif

    private func handleUpdate(_ dict: [String: Any]) {
        guard let battery = dict["batteryLevel"] as? Int,
              let range = dict["range"] as? Int,
              let state = dict["state"] as? String else { return }
        let update = CarStatusUpdate(
            batteryLevel: battery,
            range: range,
            state: state,
            isClimateOn: dict["isClimateOn"] as? Bool ?? false,
            isLocked: dict["isLocked"] as? Bool ?? true
        )
        DispatchQueue.main.async {
            self.lastUpdate = update
            // Persist for complication reads (on main for consistency with @Published)
            let defaults = UserDefaults(suiteName: "group.com.teslamatelink")
            defaults?.set(battery, forKey: "batteryLevel")
            defaults?.set(range, forKey: "range")
            // Reload complications so they show fresh data
            CLKComplicationServer.shared.reloadComplications()
        }
    }
}
