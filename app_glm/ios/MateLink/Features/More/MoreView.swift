import SwiftUI

struct MoreView: View {
    var body: some View {
        NavigationStack {
            List {
                // MARK: - 分析 Section
                Section("分析") {
                    NavigationLink {
                        StatisticsView()
                    } label: {
                        MoreRow(icon: "chart.bar", title: "统计", subtitle: "月度与年度汇总")
                    }

                    NavigationLink {
                        HeatmapView()
                    } label: {
                        MoreRow(icon: "map", title: "热力图", subtitle: "驾驶频率与模式")
                    }

                    NavigationLink {
                        EfficiencyView()
                    } label: {
                        MoreRow(icon: "speedometer", title: "效率分析", subtitle: "能耗效率分析")
                    }

                    NavigationLink {
                        RangeAnalysisView()
                    } label: {
                        MoreRow(icon: "battery.100.bolt", title: "续航分析", subtitle: "预估续航与衰减")
                    }
                }

                // MARK: - 驾驶 Section
                Section("驾驶") {
                    NavigationLink {
                        DrivingScoreView()
                    } label: {
                        MoreRow(icon: "steeringwheel", title: "驾驶评分", subtitle: "驾驶习惯评估")
                    }

                    NavigationLink {
                        FavoriteRoutesView()
                    } label: {
                        MoreRow(icon: "map.fill", title: "常用路线", subtitle: "高频出行路线")
                    }
                }
            }
            .scrollContentBackground(.hidden)
            .background(StitchColors.background)
            .navigationTitle("更多")
        }
    }
}

// MARK: - Row Component

private struct MoreRow: View {
    let icon: String
    let title: String
    let subtitle: String

    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(StitchColors.accent)
                .frame(width: 28, alignment: .center)

            VStack(alignment: .leading, spacing: 2) {
                Text(title)
                    .font(StitchFont.bodyLg())
                    .foregroundColor(StitchColors.onSurface)

                Text(subtitle)
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
        }
        .padding(.vertical, 4)
    }
}

// MARK: - Placeholder Views (replace with real implementations)

private struct RangeAnalysisView: View {
    var body: some View {
        Text("续航分析")
            .navigationTitle("续航分析")
    }
}

private struct DrivingScoreView: View {
    var body: some View {
        Text("驾驶评分")
            .navigationTitle("驾驶评分")
    }
}

private struct FavoriteRoutesView: View {
    var body: some View {
        Text("常用路线")
            .navigationTitle("常用路线")
    }
}
