import SwiftUI

struct MoreView: View {
    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 32) {
                    // Vehicle summary card
                    NavigationLink {
                        PlaceholderView(title: "车辆详情")
                    } label: {
                        VehicleSummaryCard()
                    }
                    .buttonStyle(.plain)

                    // 数据分析
                    sectionHeader("数据分析")
                    VStack(spacing: 0) {
                        ForEach(Array(analysisEntries.enumerated()), id: \.offset) { _, entry in
                            NavigationLink {
                                destinationView(for: entry.route)
                            } label: {
                                MoreRow(entry: entry)
                            }
                            .buttonStyle(.plain)
                        }
                    }

                    // 报告与导出
                    sectionHeader("报告与导出")
                    VStack(spacing: 0) {
                        ForEach(Array(reportEntries.enumerated()), id: \.offset) { _, entry in
                            NavigationLink {
                                destinationView(for: entry.route)
                            } label: {
                                MoreRow(entry: entry)
                            }
                            .buttonStyle(.plain)
                        }
                    }

                    // 系统
                    sectionHeader("系统")
                    VStack(spacing: 0) {
                        ForEach(Array(systemEntries.enumerated()), id: \.offset) { _, entry in
                            NavigationLink {
                                destinationView(for: entry.route)
                            } label: {
                                MoreRow(entry: entry)
                            }
                            .buttonStyle(.plain)
                        }
                    }

                    // Logout
                    Button(action: { /* TODO: wire to auth logout */ }) {
                        HStack(spacing: 8) {
                            Image(systemName: "rectangle.portrait.and.arrow.right")
                                .font(.system(size: 18))
                            Text("退出登录")
                                .font(StitchFont.bodyLg())
                                .fontWeight(.medium)
                        }
                        .foregroundColor(StitchColors.error)
                        .padding(.vertical, 16)
                    }
                    .frame(maxWidth: .infinity)

                    Spacer(minLength: 32)
                }
                .padding(.horizontal, 24)
                .padding(.top, 8)
            }
            .background(StitchColors.background)
            .navigationTitle("更多")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    NavigationLink {
                        PlaceholderView(title: "通知")
                    } label: {
                        Image(systemName: "bell")
                            .foregroundColor(StitchColors.onSurface)
                    }
                }
            }
        }
    }

    // MARK: - Section Header

    private func sectionHeader(_ text: String) -> some View {
        Text(text.uppercased())
            .font(StitchFont.labelCaps())
            .foregroundColor(StitchColors.onSurfaceVariant)
    }

    // MARK: - Destination Resolver

    @ViewBuilder
    private func destinationView(for route: String) -> some View {
        switch route {
        case "statistics":
            StatisticsView()
        case "heatmap":
            HeatmapView()
        case "efficiency":
            EfficiencyView()
        case "range":
            RangeAnalysisView()
        case "vampire":
            PlaceholderView(title: "能耗分析")
        case "battery_health":
            PlaceholderView(title: "电池健康")
        case "timeline":
            PlaceholderView(title: "时间线")
        case "annual_report":
            PlaceholderView(title: "年度报告")
        case "data_export":
            PlaceholderView(title: "数据导出")
        case "firmware_version":
            PlaceholderView(title: "固件版本")
        case "settings":
            PlaceholderView(title: "设置")
        case "about":
            AboutView()
        case "sentry_history":
            SentryHistoryView()
        default:
            PlaceholderView(title: route)
        }
    }
}

// MARK: - Entry Model

private struct MoreEntry {
    let title: String
    let subtitle: String
    let icon: String
    let route: String
    let trailingValue: String?
    let trailingValueGold: Bool

    init(_ title: String, _ subtitle: String, _ icon: String, _ route: String,
         trailingValue: String? = nil, trailingValueGold: Bool = false) {
        self.title = title; self.subtitle = subtitle; self.icon = icon; self.route = route
        self.trailingValue = trailingValue; self.trailingValueGold = trailingValueGold
    }
}

private let analysisEntries: [MoreEntry] = [
    MoreEntry("统计", "月度与年度汇总", "chart.bar", "statistics"),
    MoreEntry("热力图", "驾驶频率与模式", "map", "heatmap"),
    MoreEntry("效率", "Golden Foot 评分", "leaf", "efficiency"),
    MoreEntry("续航", "预估 vs 实际", "bolt", "range"),
    MoreEntry("能耗分析", "待机能耗分析", "moon", "vampire"),
    MoreEntry("电池健康", "电池衰减与 SOH", "battery.100.bolt", "battery_health", trailingValue: "95.8%", trailingValueGold: true),
    MoreEntry("时间线", "充电与行程时间线", "clock.arrow.2.circlepath", "timeline"),
    MoreEntry("哨兵历史", "哨兵事件与记录", "shield", "sentry_history"),
]

private let reportEntries: [MoreEntry] = [
    MoreEntry("年度报告 PDF", "年度驾驶总结", "doc.richtext", "annual_report"),
    MoreEntry("数据导出 CSV/JSON", "导出原始数据", "square.and.arrow.down", "data_export"),
    MoreEntry("固件版本", "当前固件版本", "terminal", "firmware_version", trailingValue: "2024.26.7"),
]

private let systemEntries: [MoreEntry] = [
    MoreEntry("设置", "应用偏好", "gearshape", "settings"),
    MoreEntry("关于", "版本与开源许可", "info.circle", "about"),
]

// MARK: - Vehicle Summary Card

private struct VehicleSummaryCard: View {
    var body: some View {
        HStack(spacing: 16) {
            // Vehicle icon box
            Image(systemName: "car")
                .font(.system(size: 28))
                .foregroundColor(StitchColors.onSurface)
                .frame(width: 48, height: 48)
                .background(StitchColors.outlineVariant.opacity(0.3))
                .clipShape(RoundedRectangle(cornerRadius: 4))

            VStack(alignment: .leading, spacing: 4) {
                Text("Tesla Model 3") // TODO: inject real car name
                    .font(.system(size: 18, weight: .semibold))
                    .foregroundColor(StitchColors.onSurface)
                Text("VIN 5YJ3E1...") // TODO: inject real VIN
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            Spacer()
            // Online status chip
            Text("在线")
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(StitchColors.statusOnline)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(StitchColors.statusOnlineBg)
                .clipShape(RoundedRectangle(cornerRadius: 4))
        }
        .padding(24)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }
}

// MARK: - More Row

private struct MoreRow: View {
    let entry: MoreEntry

    var body: some View {
        VStack(spacing: 0) {
            HStack(spacing: 16) {
                Image(systemName: entry.icon)
                    .font(.system(size: 22))
                    .foregroundColor(StitchColors.onSurface)
                    .frame(width: 24)

                VStack(alignment: .leading, spacing: 2) {
                    Text(entry.title)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(StitchColors.onSurface)
                    Text(entry.subtitle)
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
                Spacer()
                if let value = entry.trailingValue {
                    Text(value)
                        .font(StitchFont.dataMd())
                        .foregroundColor(entry.trailingValueGold ? StitchColors.accent : StitchColors.onSurfaceVariant)
                }
                Image(systemName: "chevron.right")
                    .font(.system(size: 14))
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            .padding(.vertical, 16)
            Divider()
                .frame(height: 1)
                .background(StitchColors.border)
        }
    }
}

// MARK: - Placeholder Views

private struct PlaceholderView: View {
    let title: String
    var body: some View {
        Text(title)
            .navigationTitle(title)
    }
}

private struct RangeAnalysisView: View {
    var body: some View {
        Text("续航分析")
            .navigationTitle("续航分析")
    }
}
