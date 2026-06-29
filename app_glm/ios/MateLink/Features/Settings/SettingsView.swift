import SwiftUI

struct MoreView: View {
    var body: some View {
        NavigationStack {
            List {
                NavigationLink(destination: StatisticsView()) { Label("Statistics", systemImage: "chart.bar.fill") }
                NavigationLink(destination: HeatmapView()) { Label("Heatmap", systemImage: "flame.fill") }
                NavigationLink(destination: EfficiencyView()) { Label("Efficiency", systemImage: "leaf.fill") }
                NavigationLink(destination: DestinationsView()) { Label("Destinations", systemImage: "mappin.and.ellipse") }
                NavigationLink(destination: CostView()) { Label("Cost", systemImage: "yensign.circle.fill") }
                NavigationLink(destination: RangePageView()) { Label("Range", systemImage: "arrow.triangle.pull") }
                NavigationLink(destination: VampireView()) { Label("Vampire Drain", systemImage: "bolt.slash.fill") }
                NavigationLink(destination: BatteryHealthView()) { Label("Battery Health", systemImage: "battery.75percent") }
                NavigationLink(destination: TimelineView()) { Label("Timeline", systemImage: "clock") }
                NavigationLink(destination: UpdatesView()) { Label("Updates", systemImage: "desktopcomputer") }
                NavigationLink(destination: SettingsView()) { Label("Settings", systemImage: "gear") }
                NavigationLink(destination: AboutView()) { Label("About", systemImage: "info.circle") }
            }.navigationTitle("More")
        }
    }
}

struct SettingsView: View {
    @EnvironmentObject var state: AppState
    @State private var testingConnection = false
    @State private var connectionResult: String?
    @State private var connectionSuccess = false

    var body: some View {
        List {
            Section("Instances") {
                ForEach(state.instances) { instance in
                    Button {
                        state.switchInstance(instance)
                    } label: {
                        HStack {
                            VStack(alignment: .leading) {
                                Text(instance.name).foregroundColor(.primary)
                                Text(instance.serverUrl).font(.caption).foregroundColor(.secondary)
                            }
                            Spacer()
                            if instance.id == state.activeInstanceId {
                                Image(systemName: "checkmark.circle.fill").foregroundColor(.green)
                            }
                        }
                    }
                }
                NavigationLink(destination: AddInstanceView()) {
                    Label("Add Instance", systemImage: "plus.circle")
                }
            }
            Section("Connection") {
                TextField("Server URL", text: $state.serverURL)
                SecureField("API Token", text: $state.apiToken)
                VStack(alignment: .leading, spacing: 8) {
                    Button("Test Connection") {
                        testingConnection = true
                        connectionResult = nil
                        Task {
                            do {
                                try await state.connect(url: state.serverURL, token: state.apiToken)
                                connectionResult = "Connected successfully"
                                connectionSuccess = true
                            } catch {
                                connectionResult = error.localizedDescription
                                connectionSuccess = false
                            }
                            testingConnection = false
                        }
                    }.disabled(testingConnection || state.serverURL.isEmpty)
                    if testingConnection {
                        HStack(spacing: 6) {
                            ProgressView().scaleEffect(0.8)
                            Text("Testing...").font(.caption).foregroundColor(.secondary)
                        }
                    }
                    if let result = connectionResult {
                        Text(result)
                            .font(.caption)
                            .foregroundColor(connectionSuccess ? .green : .red)
                    }
                }
            }
            Section("Preferences") {
                Toggle("Dark Mode", isOn: $state.isDarkMode)
            }
            Section("Development") {
                Toggle("Mock Mode", isOn: $state.isMockMode)
            }
            Section {
                let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "0.0.0"
                let appBuild = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "0"
                Text("Version \(appVersion) (\(appBuild))").font(.caption).foregroundColor(.secondary)
            }
        }.navigationTitle("Settings")
    }
}

struct AddInstanceView: View {
    @EnvironmentObject var state: AppState
    @Environment(\.dismiss) var dismiss
    @State private var name = ""
    @State private var serverUrl = ""
    @State private var apiToken = ""
    @State private var carId = 1

    var body: some View {
        List {
            Section("Instance Details") {
                TextField("Name", text: $name)
                TextField("Server URL", text: $serverUrl)
                SecureField("API Token", text: $apiToken)
            }
            Section {
                Button("Save") {
                    let instance = Instance(
                        id: UUID().uuidString,
                        name: name,
                        serverUrl: serverUrl,
                        apiToken: apiToken,
                        carId: carId
                    )
                    state.instances.append(instance)
                    state.switchInstance(instance)
                    dismiss()
                }.disabled(name.isEmpty || serverUrl.isEmpty || apiToken.isEmpty)
            }
        }.navigationTitle("New Instance")
    }
}

struct AboutView: View {
    private var versionText: String {
        let appVersion = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "0.0.0"
        let appBuild = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "0"
        return "Version \(appVersion) (\(appBuild)) · MIT License"
    }

    var body: some View {
        VStack(spacing: 16) {
            Image(systemName: "car.fill").font(.system(size: 60)).foregroundColor(.blue)
            Text("Tesla_MateLink").font(.title).bold()
            Text("Your Tesla Data Companion").font(.subheadline).foregroundColor(.secondary)
            Text("Not affiliated with Tesla, Inc.\nRequires self-hosted TeslaMate + TeslaMateApi.").font(.caption).multilineTextAlignment(.center).foregroundColor(.secondary)
            Text(versionText).font(.caption2).foregroundColor(.secondary)
        }.padding(40)
    }
}
