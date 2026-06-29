import SwiftUI
import WatchConnectivity

// T-201: Watch app main view
// T-204: WatchConnectivity to sync data from iPhone

struct ContentView: View {
    @StateObject private var connectivity = WatchConnectivityManager.shared
    @State private var batteryLevel: Int = 0
    @State private var range: Int = 0
    @State private var state: String = "Unknown"
    @State private var isClimateOn: Bool = false
    @State private var isLocked: Bool = true
    @State private var reachabilityObservation: NSKeyValueObservation?

    var body: some View {
        TabView {
            // Page 1: Battery + Range
            VStack(spacing: 8) {
                Text("Tesla")
                    .font(.headline)
                    .foregroundColor(.white)
                CircularBatteryView(level: batteryLevel)
                Text("\(range) km")
                    .font(.system(size: 14, weight: .medium))
                    .foregroundColor(.white)
                Text(state)
                    .font(.caption2)
                    .foregroundColor(.secondary)
            }
            .containerBackground(.clear, for: .navigation)

            // Page 2: Quick controls
            VStack(spacing: 12) {
                Text("Controls")
                    .font(.headline)
                HStack(spacing: 16) {
                    ControlButton(icon: isLocked ? "lock.fill" : "lock.open.fill",
                                  label: isLocked ? "Locked" : "Unlocked",
                                  color: isLocked ? .green : .orange)
                    ControlButton(icon: isClimateOn ? "snowflake" : "thermometer.sun",
                                  label: isClimateOn ? "AC On" : "AC Off",
                                  color: isClimateOn ? .blue : .gray)
                }
            }
            .containerBackground(.clear, for: .navigation)
        }
        .tabViewStyle(.verticalPage)
        .onAppear { loadData() }
        .onDisappear { cancelReachabilityObservation() }
        .onReceive(connectivity.$lastUpdate) { update in
            guard let update else { return }
            batteryLevel = update.batteryLevel
            range = update.range
            state = update.state
            isClimateOn = update.isClimateOn
            isLocked = update.isLocked
        }
    }

    private func loadData() {
        connectivity.activate()
        connectivity.requestUpdate()

        // Observe WCSession reachability and request update when it becomes available
        reachabilityObservation = WCSession.default.observe(\.isReachable) { [weak connectivity] _, change in
            guard let connectivity, change.newValue == true else { return }
            DispatchQueue.main.async {
                connectivity.requestUpdate()
            }
        }

        // Fallback: if still no data after 1 second, retry once
        DispatchQueue.main.asyncAfter(deadline: .now() + 1.0) { [weak self] in
            guard self?.batteryLevel == 0 && self?.range == 0 else { return }
            self?.connectivity.requestUpdate()

            // Second fallback: if still no data after 3 seconds total, retry again
            DispatchQueue.main.asyncAfter(deadline: .now() + 2.0) { [weak self] in
                guard self?.batteryLevel == 0 && self?.range == 0 else { return }
                self?.connectivity.requestUpdate()
            }
        }
    }

    private func cancelReachabilityObservation() {
        reachabilityObservation?.invalidate()
        reachabilityObservation = nil
    }
}

private struct ControlButton: View {
    let icon: String
    let label: String
    let color: Color

    var body: some View {
        VStack(spacing: 4) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(color)
            Text(label)
                .font(.system(size: 10))
                .foregroundColor(.secondary)
        }
        .frame(width: 55, height: 55)
        .background(Color.white.opacity(0.1))
        .cornerRadius(12)
    }
}
