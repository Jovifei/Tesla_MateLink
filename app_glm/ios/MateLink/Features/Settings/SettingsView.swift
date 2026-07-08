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

// MARK: - Stitch-styled Settings

private struct StitchTextField: View {
    let label: String
    @Binding var text: String
    var isSecure: Bool = false

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(StitchFont.bodySm())
                .fontWeight(.bold)
                .foregroundColor(StitchColors.primary)
            Group {
                if isSecure {
                    SecureField("", text: $text)
                } else {
                    TextField("", text: $text)
                        .autocorrectionDisabled()
                }
            }
            .font(StitchFont.dataMd())
            .foregroundColor(StitchColors.onSurface)
            .padding(.horizontal, 16)
            .padding(.vertical, 14)
            .background(StitchColors.white)
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .stroke(StitchColors.border, lineWidth: 1)
            )
        }
    }
}

private struct DropdownField: View {
    let label: String
    @Binding var selected: String
    let options: [String]

    var body: some View {
        HStack {
            Text(label)
                .font(StitchFont.bodyLg())
                .foregroundColor(StitchColors.onSurface)
            Spacer()
            Menu {
                ForEach(options, id: \.self) { opt in
                    Button(opt) { selected = opt }
                }
            } label: {
                HStack(spacing: 4) {
                    Text(selected)
                        .font(StitchFont.bodySm())
                    Image(systemName: "chevron.down")
                        .font(.system(size: 12, weight: .bold))
                }
                .foregroundColor(StitchColors.onSurface)
                .padding(.horizontal, 12)
                .padding(.vertical, 8)
                .background(StitchColors.white)
                .overlay(
                    RoundedRectangle(cornerRadius: 4)
                        .stroke(StitchColors.border, lineWidth: 1)
                )
            }
        }
    }
}

struct SettingsView: View {
    @EnvironmentObject var state: AppState
    @State private var testingConnection = false
    @State private var connectionResult: String?
    @State private var connectionSuccess = false
    @State private var language = "中文 (Chinese)"
    @State private var themeMode = "Light Mode"

    private var appVersion: String {
        let v = Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String ?? "0.0.0"
        let b = Bundle.main.infoDictionary?["CFBundleVersion"] as? String ?? "0"
        return "v\(v) (构建 \(b))"
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 32) {
                // ── Section 1: 实例 ──────────────────────────────
                VStack(alignment: .leading, spacing: 8) {
                    StitchLabel("实例")
                    StitchCard {
                        if let active = state.instances.first(where: { $0.id == state.activeInstanceId }) {
                            HStack {
                                VStack(alignment: .leading, spacing: 4) {
                                    Text(active.name)
                                        .font(StitchFont.bodyLg())
                                        .fontWeight(.semibold)
                                        .foregroundColor(StitchColors.onSurface)
                                    Text(active.serverUrl)
                                        .font(StitchFont.dataMd())
                                        .foregroundColor(StitchColors.onSurfaceVariant)
                                }
                                Spacer()
                                StitchStatusChip(text: "已连接", isOnline: true)
                            }
                        } else {
                            Text("未配置实例")
                                .font(StitchFont.bodySm())
                                .foregroundColor(StitchColors.onSurfaceVariant)
                        }

                        ForEach(state.instances.filter { $0.id != state.activeInstanceId }) { inst in
                            Divider().background(StitchColors.border).padding(.vertical, 12)
                            Button {
                                state.switchInstance(inst)
                            } label: {
                                HStack {
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(inst.name)
                                            .font(StitchFont.bodyLg())
                                            .foregroundColor(StitchColors.onSurface)
                                        Text(inst.serverUrl)
                                            .font(StitchFont.bodySm())
                                            .foregroundColor(StitchColors.onSurfaceVariant)
                                    }
                                    Spacer()
                                    Text("切换")
                                        .font(StitchFont.labelCaps())
                                        .foregroundColor(StitchColors.primary)
                                }
                            }
                            .buttonStyle(.plain)
                        }

                        Spacer(minLength: 16)
                        NavigationLink(destination: AddInstanceView()) {
                            HStack(spacing: 4) {
                                Image(systemName: "plus")
                                    .font(.system(size: 14, weight: .semibold))
                                Text("添加实例")
                                    .font(StitchFont.bodySm())
                                    .fontWeight(.semibold)
                            }
                            .foregroundColor(StitchColors.primary)
                        }
                    }
                }

                // ── Section 2: 连接 ──────────────────────────────
                VStack(alignment: .leading, spacing: 8) {
                    StitchLabel("连接")
                    StitchCard {
                        VStack(spacing: 16) {
                            StitchTextField(label: "服务器地址", text: $state.serverURL)
                            StitchTextField(label: "API 令牌", text: $state.apiToken, isSecure: true)
                            HStack(spacing: 8) {
                                Button {
                                    testingConnection = true
                                    connectionResult = nil
                                    Task {
                                        do {
                                            try await state.connect(url: state.serverURL, token: state.apiToken)
                                            connectionResult = "连接成功"
                                            connectionSuccess = true
                                        } catch {
                                            connectionResult = error.localizedDescription
                                            connectionSuccess = false
                                        }
                                        testingConnection = false
                                    }
                                } label: {
                                    Text(testingConnection ? "测试中..." : "测试连接")
                                        .font(StitchFont.bodySm())
                                        .fontWeight(.semibold)
                                        .foregroundColor(StitchColors.onSurface)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 14)
                                        .background(StitchColors.white)
                                        .overlay(
                                            RoundedRectangle(cornerRadius: 4)
                                                .stroke(StitchColors.border, lineWidth: 1)
                                        )
                                }
                                .disabled(testingConnection || state.serverURL.isEmpty)

                                Button {
                                    // save handled by AppSettings binding
                                } label: {
                                    Text("保存")
                                        .font(StitchFont.bodySm())
                                        .fontWeight(.semibold)
                                        .foregroundColor(StitchColors.white)
                                        .frame(maxWidth: .infinity)
                                        .padding(.vertical, 14)
                                        .background(StitchColors.primary)
                                        .cornerRadius(4)
                                }
                            }
                            if let result = connectionResult {
                                HStack(spacing: 6) {
                                    Circle()
                                        .fill(connectionSuccess ? StitchColors.statusOnline : StitchColors.error)
                                        .frame(width: 8, height: 8)
                                    Text(result)
                                        .font(StitchFont.bodySm())
                                        .foregroundColor(connectionSuccess ? StitchColors.statusOnline : StitchColors.error)
                                }
                            }
                        }
                    }
                }

                // ── Section 3: 显示 ──────────────────────────────
                VStack(alignment: .leading, spacing: 8) {
                    StitchLabel("显示")
                    StitchCard {
                        VStack(spacing: 16) {
                            DropdownField(
                                label: "语言",
                                selected: $language,
                                options: ["English", "中文 (Chinese)", "Deutsch"]
                            )
                            Divider().background(StitchColors.border)
                            DropdownField(
                                label: "主题",
                                selected: $themeMode,
                                options: ["System", "Light Mode", "Dark Mode"]
                            )
                            .onChange(of: themeMode) { newValue in
                                state.isDarkMode = (newValue == "Dark Mode")
                            }
                            Divider().background(StitchColors.border)
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text("模拟模式")
                                        .font(StitchFont.bodyLg())
                                        .foregroundColor(StitchColors.onSurface)
                                    Text("使用模拟数据进行测试")
                                        .font(StitchFont.bodySm())
                                        .foregroundColor(StitchColors.onSurfaceVariant)
                                }
                                Spacer()
                                Toggle("", isOn: $state.isMockMode)
                                    .labelsHidden()
                                    .tint(StitchColors.primary)
                            }
                        }
                    }
                }

                // ── Section 4: 关于 ──────────────────────────────
                VStack(alignment: .leading, spacing: 8) {
                    StitchLabel("关于")
                    StitchCard {
                        HStack {
                            Text("MateLink 应用")
                                .font(StitchFont.bodyLg())
                                .fontWeight(.semibold)
                                .foregroundColor(StitchColors.onSurface)
                            Spacer()
                            VStack(alignment: .trailing, spacing: 4) {
                                Text(appVersion)
                                    .font(StitchFont.dataMd())
                                    .foregroundColor(StitchColors.onSurfaceVariant)
                                Text("October 24, 2023")
                                    .font(StitchFont.bodySm())
                                    .foregroundColor(StitchColors.onSurfaceVariant)
                            }
                        }
                    }
                }
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 24)
        }
        .navigationTitle("设置")
        .navigationBarTitleDisplayMode(.inline)
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
