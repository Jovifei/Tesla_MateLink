import Foundation
import WatchConnectivity

// iPhone-side WatchConnectivity manager
// Activates WCSession and replies to Watch status requests

class PhoneWCSessionManager: NSObject, WCSessionDelegate {
    static let shared = PhoneWCSessionManager()

    /// Last known car status, set by DashboardView after each refresh
    var lastStatus: CarStatus?

    private override init() { super.init() }

    func activate() {
        guard WCSession.isSupported() else { return }
        WCSession.default.delegate = self
        WCSession.default.activate()
    }

    /// Push current status to Watch (call after each Dashboard refresh)
    func sendToWatch(_ status: CarStatus) {
        guard WCSession.default.activationState == .activated else { return }
        let message: [String: Any] = [
            "batteryLevel": status.batteryLevel,
            "range": status.estBatteryRangeKm,
            "state": status.state.rawValue,
            "isClimateOn": status.isClimateOn,
            "isLocked": status.locked
        ]
        if WCSession.default.isReachable {
            WCSession.default.sendMessage(message, replyHandler: nil)
        }
        // Also persist to AppGroup for complications
        let defaults = UserDefaults(suiteName: "group.com.teslamatelink")
        defaults?.set(status.batteryLevel, forKey: "batteryLevel")
        defaults?.set(status.estBatteryRangeKm, forKey: "range")
    }

    // MARK: - WCSessionDelegate

    func session(_ session: WCSession, activationDidCompleteWith activationState: WCSessionActivationState, error: Error?) {}

    func sessionDidBecomeInactive(_ session: WCSession) {}
    func sessionDidDeactivate(_ session: WCSession) {
        WCSession.default.activate()
    }

    // Reply to Watch's requestStatus message with current data
    func session(_ session: WCSession, didReceiveMessage message: [String: Any], replyHandler: @escaping ([String: Any]) -> Void) {
        if message["action"] as? String == "requestStatus", let s = lastStatus {
            replyHandler([
                "batteryLevel": s.batteryLevel,
                "range": s.estBatteryRangeKm,
                "state": s.state.rawValue,
                "isClimateOn": s.isClimateOn,
                "isLocked": s.locked
            ])
        } else {
            replyHandler(["ok": true])
        }
    }

    func session(_ session: WCSession, didReceiveMessage message: [String: Any]) {}
}
