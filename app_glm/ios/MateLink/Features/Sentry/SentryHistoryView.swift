import SwiftUI

// MARK: - Sentry-specific Colors (from Stitch design spec)

private let SentryPurple = Color(hex: "7C3AED")
private let AlertRed = Color(hex: "DC2626")
private let SafeGreen = Color(hex: "059669")
private let DarkSwiss = Color(hex: "171717")

// MARK: - Data Models

private enum TimeRange: String, CaseIterable, Identifiable {
    case sevenDays = "7天"
    case thirtyDays = "30天"
    case all = "全部"
    var id: String { rawValue }
}

private struct SentryEvent: Identifiable {
    let id = UUID()
    let time: String
    let type: String
    let detail: String
    let color: Color
    let isAlert: Bool
}

private let timelineEvents: [SentryEvent] = [
    SentryEvent(time: "18:32", type: "人员靠近", detail: "持续 2min · 置信度 95%", color: SentryPurple, isAlert: false),
    SentryEvent(time: "14:15", type: "车辆经过", detail: "持续 1min · 置信度 88%", color: SentryPurple.opacity(0.5), isAlert: false),
    SentryEvent(time: "09:48", type: "震动检测", detail: "严重程度: 中 · 置信度 92%", color: AlertRed, isAlert: true),
    SentryEvent(time: "08:20", type: "人员靠近", detail: "持续 30s · 置信度 99%", color: SentryPurple.opacity(0.3), isAlert: false)
]

private struct DistributionItem: Identifiable {
    let id = UUID()
    let label: String
    let count: Int
    let color: Color
}

private let distribution: [DistributionItem] = [
    DistributionItem(label: "人员靠近", count: 12, color: SentryPurple),
    DistributionItem(label: "车辆经过", count: 10, color: DarkSwiss),
    DistributionItem(label: "震动检测", count: 2, color: AlertRed)
]

private let hourBars: [CGFloat] = [4, 2, 1, 3, 6, 20, 16, 24, 14, 8, 5, 3]

private enum Sensitivity: String, CaseIterable, Identifiable {
    case low = "低"
    case medium = "中"
    case high = "高"
    var id: String { rawValue }
    var desc: String {
        switch self {
        case .low: return "仅检测剧烈撞击或玻璃破碎"
        case .medium: return "推荐：检测人员停留及靠近"
        case .high: return "记录所有移动物体及微小光亮"
        }
    }
}

// MARK: - SentryHistoryView

struct SentryHistoryView: View {
    @State private var selectedRange: TimeRange = .sevenDays
    @State private var selectedSensitivity: Sensitivity = .medium

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 32) {
                SummaryCard()
                StatsGrid()
                DistributionCard()
                TimelineSection()
                TimeDistributionCard()
                StorageCard()
                SensitivitySection(selected: selectedSensitivity) { selectedSensitivity = $0 }
                ActionButtons()
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 128)
        }
        .background(StitchColors.background)
        .navigationTitle("哨兵历史")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                TimeRangeTabs(selected: selectedRange) { selectedRange = $0 }
            }
        }
    }
}

// MARK: - Time Range Tabs

private struct TimeRangeTabs: View {
    let selected: TimeRange
    let onSelect: (TimeRange) -> Void

    var body: some View {
        HStack(spacing: 4) {
            ForEach(TimeRange.allCases) { range in
                let isSelected = range == selected
                Text(range.rawValue)
                    .font(StitchFont.labelCaps())
                    .foregroundColor(isSelected ? StitchColors.white : StitchColors.onSurfaceVariant)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 6)
                    .background(isSelected ? DarkSwiss : Color.clear)
                    .clipShape(RoundedRectangle(cornerRadius: 6))
                    .onTapGesture { onSelect(range) }
            }
        }
        .padding(4)
        .background(StitchColors.surfaceContainerLow)
        .clipShape(RoundedRectangle(cornerRadius: 8))
    }
}

// MARK: - Summary Card

private struct SummaryCard: View {
    var body: some View {
        HStack(alignment: .top) {
            VStack(alignment: .leading, spacing: 8) {
                StitchLabel("7天哨兵事件")
                Text("24")
                    .font(StitchFont.displayLg())
                    .foregroundColor(StitchColors.onSurface)
                Text("日均 3.4 次 · 消耗 2.1 kWh")
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            Spacer()
            Image(systemName: "shield.fill")
                .font(.system(size: 36))
                .foregroundColor(SentryPurple)
        }
        .padding(24)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }
}

// MARK: - Stats Grid

private struct StatsGrid: View {
    var body: some View {
        HStack(spacing: 16) {
            StatCard(label: "触发", value: "18次", valueColor: DarkSwiss)
            StatCard(label: "误报", value: "6次", valueColor: AlertRed)
            StatCard(label: "持续时长", value: "8h", valueColor: StitchColors.onSurface)
        }
    }
}

private struct StatCard: View {
    let label: String
    let value: String
    let valueColor: Color

    var body: some View {
        VStack(spacing: 4) {
            Text(label)
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
            Text(value)
                .font(StitchFont.dataMd())
                .foregroundColor(valueColor)
        }
        .frame(maxWidth: .infinity)
        .padding(16)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }
}

// MARK: - Distribution Card (Donut + Legend)

private struct DistributionCard: View {
    var body: some View {
        StitchCard {
            StitchLabel("事件类型分布")
            Spacer().frame(height: 24)
            HStack {
                DonutChart(segments: distribution.map { ($0.count, $0.color) })
                    .frame(width: 128, height: 128)
                Spacer()
                VStack(alignment: .leading, spacing: 12) {
                    ForEach(distribution) { item in
                        HStack(spacing: 12) {
                            Circle()
                                .fill(item.color)
                                .frame(width: 8, height: 8)
                            Text(item.label)
                                .font(StitchFont.bodySm())
                                .foregroundColor(StitchColors.onSurface)
                            Spacer()
                            Text("\(item.count)次")
                                .font(StitchFont.dataMd())
                                .foregroundColor(StitchColors.onSurface)
                        }
                    }
                }
            }
        }
    }
}

private struct DonutChart: View {
    let segments: [(count: Int, color: Color)]

    var body: some View {
        let total = segments.reduce(0) { $0 + $1.count }
        ZStack {
            Canvas { context in
                let strokeWidth: CGFloat = 16
                let diameter = min(context.size.width, context.size.height) - strokeWidth
                let center = CGPoint(x: context.size.width / 2, y: context.size.height / 2)
                let radius = diameter / 2

                var startAngle = Angle.degrees(-90)
                for (count, color) in segments {
                    let fraction = Double(count) / Double(total)
                    let endAngle = startAngle + .degrees(fraction * 360)
                    let path = Path { p in
                        p.addArc(
                            center: center,
                            radius: radius,
                            startAngle: startAngle,
                            endAngle: endAngle,
                            clockwise: false
                        )
                    }
                    context.stroke(path, with: .color(color), lineWidth: strokeWidth)
                    startAngle = endAngle
                }
            }
            Text("100%")
                .font(StitchFont.dataMd())
                .foregroundColor(StitchColors.onSurface)
        }
    }
}

// MARK: - Timeline Section

private struct TimelineSection: View {
    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            StitchLabel("最近哨兵事件")
            VStack(spacing: 16) {
                ForEach(Array(timelineEvents.enumerated()), id: \.element.id) { index, event in
                    TimelineItem(event: event, isLast: index == timelineEvents.count - 1)
                }
            }
        }
    }
}

private struct TimelineItem: View {
    let event: SentryEvent
    let isLast: Bool

    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            // Timeline dot + line
            VStack(spacing: 0) {
                Circle()
                    .fill(event.color)
                    .frame(width: 10, height: 10)
                    .padding(.top, 6)
                if !isLast {
                    Rectangle()
                        .fill(StitchColors.border)
                        .frame(width: 1, height: 48)
                }
            }
            // Event card
            HStack {
                VStack(alignment: .leading, spacing: 2) {
                    Text(event.time)
                        .font(StitchFont.dataMd())
                        .foregroundColor(StitchColors.onSurface)
                    Text(event.type)
                        .font(.system(size: 14, weight: .bold))
                        .foregroundColor(event.isAlert ? AlertRed : DarkSwiss)
                    Text(event.detail)
                        .font(.system(size: 12))
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
                Spacer()
                HStack(spacing: 16) {
                    // Thumbnail placeholder
                    StitchColors.surfaceContainerHigh
                        .frame(width: 96, height: 54)
                        .clipShape(RoundedRectangle(cornerRadius: 4))
                    Text("查看")
                        .font(StitchFont.labelCaps())
                        .foregroundColor(StitchColors.onSurface)
                }
            }
            .padding(16)
            .frame(maxWidth: .infinity)
            .background(StitchColors.white)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
        }
    }
}

// MARK: - Time Distribution Card (Histogram)

private struct TimeDistributionCard: View {
    var body: some View {
        let maxValue = hourBars.max() ?? 1
        StitchCard {
            StitchLabel("事件时段分布")
            Spacer().frame(height: 24)
            // Histogram bars
            HStack(alignment: .bottom, spacing: 4) {
                ForEach(Array(hourBars.enumerated()), id: \.offset) { _, value in
                    let isPeak = value >= 14
                    let heightFraction = value / maxValue
                    RoundedRectangle(cornerRadius: 2)
                        .fill(isPeak ? SentryPurple : StitchColors.surfaceContainerHigh)
                        .frame(maxWidth: .infinity)
                        .frame(height: 96 * heightFraction)
                }
            }
            .frame(height: 96)
            Spacer().frame(height: 8)
            HStack {
                Text("00:00")
                    .font(.custom("JetBrainsMono-Medium", size: 10))
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                Text("高频时段 (22-06)")
                    .font(.custom("JetBrainsMono-Medium", size: 10))
                    .fontWeight(.bold)
                    .foregroundColor(SentryPurple)
                Spacer()
                Text("23:59")
                    .font(.custom("JetBrainsMono-Medium", size: 10))
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
        }
    }
}

// MARK: - Storage Card

private struct StorageCard: View {
    var body: some View {
        StitchCard {
            HStack {
                StitchLabel("录像存储")
                Spacer()
                Text("2.4 GB / 64 GB")
                    .font(StitchFont.dataMd())
                    .foregroundColor(DarkSwiss)
            }
            Spacer().frame(height: 16)
            // Progress bar
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    StitchColors.surfaceContainerHigh
                        .frame(height: 8)
                        .clipShape(Capsule())
                    SentryPurple
                        .frame(width: geo.size.width * 0.0375, height: 8)
                        .clipShape(Capsule())
                }
            }
            .frame(height: 8)
            Spacer().frame(height: 16)
            HStack(spacing: 8) {
                Image(systemName: "clock")
                    .font(.system(size: 14))
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Text("7天后自动清理旧录像")
                    .font(.system(size: 12))
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
        }
    }
}

// MARK: - Sensitivity Section

private struct SensitivitySection: View {
    let selected: Sensitivity
    let onSelect: (Sensitivity) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            StitchLabel("灵敏度设置")
            HStack(spacing: 16) {
                ForEach(Sensitivity.allCases) { level in
                    let isSelected = level == selected
                    VStack(alignment: .leading, spacing: 8) {
                        Text(level.rawValue)
                            .font(StitchFont.labelCaps())
                            .foregroundColor(StitchColors.onSurface)
                        Text(level.desc)
                            .font(.system(size: 10))
                            .foregroundColor(isSelected ? DarkSwiss : StitchColors.onSurfaceVariant)
                            .fontWeight(isSelected ? .medium : .regular)
                            .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    .padding(16)
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .background(isSelected ? StitchColors.surfaceContainerLow : StitchColors.white)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(isSelected ? DarkSwiss : StitchColors.border, lineWidth: isSelected ? 2 : 1)
                    )
                    .overlay(alignment: .topTrailing) {
                        if isSelected {
                            Image(systemName: "checkmark.circle.fill")
                                .font(.system(size: 12))
                                .foregroundColor(DarkSwiss)
                                .padding(8)
                        }
                    }
                    .onTapGesture { onSelect(level) }
                }
            }
        }
    }
}

// MARK: - Action Buttons

private struct ActionButtons: View {
    var body: some View {
        HStack(spacing: 16) {
            // Export button
            Text("导出事件")
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurface)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
                .background(StitchColors.white)
                .clipShape(RoundedRectangle(cornerRadius: 8))
                .overlay(RoundedRectangle(cornerRadius: 8).stroke(DarkSwiss, lineWidth: 1))

            // Clear history button
            HStack(spacing: 8) {
                Image(systemName: "trash")
                    .font(.system(size: 16))
                Text("清空历史")
                    .font(StitchFont.labelCaps())
            }
            .foregroundColor(AlertRed)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .background(StitchColors.white)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(AlertRed, lineWidth: 1))
        }
    }
}
