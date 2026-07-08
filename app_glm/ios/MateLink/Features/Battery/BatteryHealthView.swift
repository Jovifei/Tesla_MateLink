import SwiftUI

// MARK: - Battery-specific Stitch tokens

private let AccentGold = Color(hex: "A16207")
private let RingTrack = Color(hex: "F5F5F5")
private let RingProgress = Color(hex: "171717")
private let GridLine = Color(hex: "F5F5F5")
private let BaselineGray = Color(hex: "D1D5DB")
private let EmeraldBg = Color(hex: "D1FAE5")
private let EmeraldText = Color(hex: "059669")
private let TempLow = Color(hex: "DBEAFE")
private let TempLowText = Color(hex: "1D4ED8")
private let TempNormal = Color(hex: "10B981")
private let TempHigh = Color(hex: "FFEDD5")
private let TempHighText = Color(hex: "C2410C")

// MARK: - Mock data (matches Stitch design spec; data layer unchanged)
// TODO(data): wire to BatteryHealth domain model via repository.

private let healthPercent: Double = 95.8
private let nominalCapacity = "82.0"
private let currentCapacity = "78.5"
private let degradation = "4.2%"

// Monthly SOH samples (Jan..Dec) for current car and fleet baseline.
private let currentCarCurve: [CGFloat] = [1.00, 0.995, 0.99, 0.985, 0.98, 0.975, 0.97, 0.966, 0.963, 0.96, 0.959, 0.958]
private let fleetBaselineCurve: [CGFloat] = [1.00, 0.99, 0.982, 0.975, 0.968, 0.962, 0.955, 0.95, 0.945, 0.94, 0.936, 0.933]

private struct CycleStat: Identifiable {
    let id = UUID()
    let label: String
    let value: String
    let unit: String
}

private let cycleStats: [CycleStat] = [
    CycleStat(label: "总循环", value: "486", unit: "次"),
    CycleStat(label: "本月循环", value: "42", unit: "次"),
    CycleStat(label: "日均循环", value: "1.4", unit: "次")
]

private struct TempSegment: Identifiable {
    let id = UUID()
    let percent: CGFloat
    let color: Color
    let textColor: Color
    let legend: String
}

private let tempSegments: [TempSegment] = [
    TempSegment(percent: 0.15, color: TempLow, textColor: TempLowText, legend: "<10°C (Low)"),
    TempSegment(percent: 0.75, color: TempNormal, textColor: .white, legend: "10-35°C (Normal)"),
    TempSegment(percent: 0.10, color: TempHigh, textColor: TempHighText, legend: ">35°C (High)")
]

private struct Suggestion: Identifiable {
    let id = UUID()
    let icon: String
    let title: String
    let desc: String
}

private let suggestions: [Suggestion] = [
    Suggestion(icon: "bolt.fill", title: "避免频繁快充至100%", desc: "建议在非长途旅行时将限额设为80% - 90%"),
    Suggestion(icon: "battery.75", title: "保持电量在20-80%区间", desc: "在此区间内循环充放电可显著延长电芯寿命"),
    Suggestion(icon: "thermometer.medium", title: "高温环境减少快充", desc: "极端高温下快充会加速电解液老化")
]

// MARK: - BatteryHealthView

struct BatteryHealthView: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 32) {
                HealthRingCard()
                StatsGrid()
                DegradationTrendCard()
                CycleStatsCard()
                TemperatureCard()
                MaintenanceCard()
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 128)
        }
        .background(StitchColors.background)
        .navigationTitle("电池健康")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Image(systemName: "info.circle")
                    .foregroundColor(StitchColors.onSurface)
            }
        }
    }
}

// MARK: - Main Health Card

private struct HealthRingCard: View {
    var body: some View {
        StitchCard {
            VStack(spacing: 16) {
                ZStack {
                    Canvas { context, size in
                        let strokeWidth: CGFloat = 8
                        let diameter = min(size.width, size.height) - strokeWidth
                        let center = CGPoint(x: size.width / 2, y: size.height / 2)
                        let radius = diameter / 2
                        // Track
                        let track = Path { p in
                            p.addArc(center: center, radius: radius,
                                     startAngle: .degrees(0), endAngle: .degrees(360), clockwise: false)
                        }
                        context.stroke(track, with: .color(RingTrack), lineWidth: strokeWidth)
                        // Progress
                        let end = Angle.degrees(-90 + 360 * (healthPercent / 100))
                        let progress = Path { p in
                            p.addArc(center: center, radius: radius,
                                     startAngle: .degrees(-90), endAngle: end, clockwise: false)
                        }
                        context.stroke(progress, with: .color(RingProgress),
                                       style: StrokeStyle(lineWidth: strokeWidth, lineCap: .round))
                    }
                    .frame(width: 192, height: 192)

                    VStack(spacing: 8) {
                        Text(String(format: "%.1f%%", healthPercent))
                            .font(.custom("JetBrainsMono-Medium", size: 32))
                            .foregroundColor(StitchColors.onSurface)
                        Text("优秀")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(EmeraldText)
                            .padding(.horizontal, 8)
                            .padding(.vertical, 2)
                            .background(EmeraldBg)
                            .clipShape(RoundedRectangle(cornerRadius: 4))
                    }
                }
                Text("电池健康度")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            .frame(maxWidth: .infinity)
        }
    }
}

// MARK: - Stats Grid

private struct StatsGrid: View {
    var body: some View {
        HStack(spacing: 16) {
            StatCell(label: "标称容量", value: nominalCapacity, unit: "kWh")
            StatCell(label: "当前容量", value: currentCapacity, unit: "kWh")
            StatCell(label: "衰减", value: degradation, unit: nil, valueColor: AccentGold)
        }
    }
}

private struct StatCell: View {
    let label: String
    let value: String
    let unit: String?
    var valueColor: Color = StitchColors.onSurface

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
            HStack(alignment: .bottom, spacing: 0) {
                Text(value)
                    .font(StitchFont.dataMd())
                    .foregroundColor(valueColor)
                if let unit = unit {
                    Text(" \(unit)")
                        .font(.custom("JetBrainsMono-Medium", size: 10))
                        .foregroundColor(valueColor)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(StitchColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }
}

// MARK: - Degradation Trend Card

private struct DegradationTrendCard: View {
    var body: some View {
        StitchCard {
            HStack {
                Text("容量衰减趋势")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(StitchColors.onSurface)
                Spacer()
                Text("高于平均")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(EmeraldText)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 4)
                    .background(EmeraldBg)
                    .clipShape(RoundedRectangle(cornerRadius: 4))
            }
            .padding(.bottom, 24)

            Canvas { context, size in
                let w = size.width
                let h = size.height
                // Grid lines
                for i in 1...3 {
                    let y = h * CGFloat(i) / 4
                    var line = Path()
                    line.move(to: CGPoint(x: 0, y: y))
                    line.addLine(to: CGPoint(x: w, y: y))
                    context.stroke(line, with: .color(GridLine), lineWidth: 1)
                }
                // Axes
                var axis = Path()
                axis.move(to: CGPoint(x: 0, y: 0)); axis.addLine(to: CGPoint(x: 0, y: h))
                axis.move(to: CGPoint(x: 0, y: h)); axis.addLine(to: CGPoint(x: w, y: h))
                context.stroke(axis, with: .color(StitchColors.outlineVariant), lineWidth: 1)

                func makePath(_ points: [CGFloat]) -> Path {
                    let minV: CGFloat = 0.90
                    let maxV: CGFloat = 1.00
                    return Path { p in
                        for (idx, v) in points.enumerated() {
                            let x = w * CGFloat(idx) / CGFloat(points.count - 1)
                            let norm = max(0, min(1, (v - minV) / (maxV - minV)))
                            let y = h - norm * h
                            if idx == 0 { p.move(to: CGPoint(x: x, y: y)) }
                            else { p.addLine(to: CGPoint(x: x, y: y)) }
                        }
                    }
                }
                // Fleet baseline (dashed gray)
                context.stroke(makePath(fleetBaselineCurve), with: .color(BaselineGray),
                               style: StrokeStyle(lineWidth: 1.5, dash: [6, 3]))
                // Current car (solid gold)
                context.stroke(makePath(currentCarCurve), with: .color(AccentGold),
                               style: StrokeStyle(lineWidth: 2, lineCap: .round))
            }
            .frame(height: 192)
            .padding(.bottom, 8)

            HStack {
                ForEach(["1月", "4月", "7月", "10月", "12月"], id: \.self) { label in
                    Text(label)
                        .font(.system(size: 10, weight: .medium))
                        .foregroundColor(StitchColors.onSurfaceVariant)
                    if label != "12月" { Spacer() }
                }
            }
            .padding(.bottom, 12)

            HStack(spacing: 16) {
                LegendItem(color: AccentGold, text: "本车")
                LegendItem(color: BaselineGray, text: "车队基准")
                Spacer()
            }
        }
    }
}

private struct LegendItem: View {
    let color: Color
    let text: String
    var body: some View {
        HStack(spacing: 6) {
            Rectangle().fill(color).frame(width: 12, height: 2)
            Text(text)
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(StitchColors.onSurfaceVariant)
        }
    }
}

// MARK: - Cycle Stats Card

private struct CycleStatsCard: View {
    var body: some View {
        StitchCard {
            Text("循环统计")
                .font(.system(size: 20, weight: .semibold))
                .foregroundColor(StitchColors.onSurface)
                .padding(.bottom, 24)
            HStack(spacing: 16) {
                ForEach(cycleStats) { stat in
                    VStack(alignment: .leading, spacing: 4) {
                        Text(stat.label)
                            .font(StitchFont.labelCaps())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                        HStack(alignment: .bottom, spacing: 4) {
                            Text(stat.value)
                                .font(StitchFont.dataLg())
                                .foregroundColor(StitchColors.onSurface)
                            Text(stat.unit)
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(StitchColors.onSurfaceVariant)
                        }
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
        }
    }
}

// MARK: - Temperature Distribution Card

private struct TemperatureCard: View {
    var body: some View {
        StitchCard {
            Text("电池温度分布")
                .font(.system(size: 20, weight: .semibold))
                .foregroundColor(StitchColors.onSurface)
                .padding(.bottom, 24)

            GeometryReader { geo in
                HStack(spacing: 0) {
                    ForEach(tempSegments) { seg in
                        ZStack {
                            Rectangle().fill(seg.color)
                            Text("\(Int(seg.percent * 100))%")
                                .font(.system(size: 10, weight: .bold))
                                .foregroundColor(seg.textColor)
                        }
                        .frame(width: geo.size.width * seg.percent)
                    }
                }
            }
            .frame(height: 32)
            .clipShape(RoundedRectangle(cornerRadius: 4))
            .padding(.bottom, 24)

            HStack(spacing: 8) {
                ForEach(tempSegments) { seg in
                    HStack(spacing: 6) {
                        Circle().fill(seg.color).frame(width: 8, height: 8)
                        Text(seg.legend)
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(StitchColors.onSurfaceVariant)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
        }
    }
}

// MARK: - Maintenance Suggestions Card

private struct MaintenanceCard: View {
    var body: some View {
        StitchCard {
            HStack(spacing: 8) {
                Text("💡").font(.system(size: 20))
                Text("维护建议")
                    .font(.system(size: 20, weight: .semibold))
                    .foregroundColor(StitchColors.onSurface)
            }
            .padding(.bottom, 24)

            VStack(spacing: 16) {
                ForEach(suggestions) { s in
                    HStack(alignment: .top, spacing: 16) {
                        Image(systemName: s.icon)
                            .font(.system(size: 20))
                            .foregroundColor(AccentGold)
                            .frame(width: 24)
                        VStack(alignment: .leading, spacing: 2) {
                            Text(s.title)
                                .font(.system(size: 16, weight: .medium))
                                .foregroundColor(StitchColors.onSurface)
                            Text(s.desc)
                                .font(StitchFont.bodySm())
                                .foregroundColor(StitchColors.onSurfaceVariant)
                        }
                        Spacer(minLength: 0)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
                    .background(StitchColors.surfaceContainerLow)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                }
            }
        }
    }
}
