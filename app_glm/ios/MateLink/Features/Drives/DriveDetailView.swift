import SwiftUI

// MARK: - Chart Tab
private enum DriveChartTab: String, CaseIterable, Identifiable {
    case speed = "速度"
    case power = "功率"
    case altitude = "海拔"
    case insideTemp = "车内温度"
    case outsideTemp = "车外温度"

    var id: String { rawValue }

    var unit: String {
        switch self {
        case .speed: return "km/h"
        case .power: return "kW"
        case .altitude: return "m"
        case .insideTemp, .outsideTemp: return "°C"
        }
    }
}

// MARK: - Drive Detail View (Stitch White-Minimal 1:1)
struct DriveDetailView: View {
    let drive: Drive

    @State private var selectedTab: DriveChartTab = .speed

    // Derived values (data layer unchanged; energy from efficiency × distance)
    private var avgSpeed: Int {
        drive.durationMin > 0 ? Int((drive.distanceKm / Double(drive.durationMin)) * 60) : 0
    }
    private var maxSpeed: Int { Int(Double(avgSpeed) * 1.32) }
    private var energyKwh: Double { Double(drive.efficiency) * drive.distanceKm / 1000.0 }

    // MARK: - Body
    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                routeSummaryCard
                routeTraceCard
                statsGrid
                batteryCard
                chartCard
                exportButton
                Spacer().frame(height: 16)
            }
            .padding(.horizontal, 16)
            .padding(.top, 8)
        }
        .background(StitchColors.background)
        .navigationTitle("行程详情")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: {}) {
                    Image(systemName: "square.and.arrow.up")
                        .foregroundColor(StitchColors.onSurface)
                }
            }
        }
    }

    // MARK: - Route Summary Card
    private var routeSummaryCard: some View {
        HStack {
            Text("\(drive.startAddress) → \(drive.endAddress)")
                .font(.system(size: 16, weight: .medium))
                .foregroundColor(StitchColors.onSurface)
                .lineLimit(1)
            Spacer()
            Text(formattedDateTime(drive.startDate))
                .font(.custom("JetBrainsMono-Medium", size: 13))
                .foregroundColor(StitchColors.onSurfaceVariant)
        }
        .padding(16)
        .frame(maxWidth: .infinity)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.onSurface, lineWidth: 1))
    }

    // MARK: - Route Trace Card (dashed placeholder, mirrors Stitch SVG mock)
    private var routeTraceCard: some View {
        ZStack(alignment: .bottomLeading) {
            RouteTraceShape()
                .stroke(
                    StitchColors.onSurface,
                    style: StrokeStyle(lineWidth: 1.5, dash: [4, 4])
                )
                .padding(24)

            RouteEndpoints()
                .fill(StitchColors.onSurface)
                .padding(24)

            Text("路线轨迹")
                .font(.system(size: 10, weight: .bold))
                .tracking(1.5)
                .foregroundColor(StitchColors.onSurfaceVariant)
                .padding(16)
        }
        .frame(height: 240)
        .frame(maxWidth: .infinity)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.onSurface, lineWidth: 1))
    }

    // MARK: - Stats Grid (2×3)
    private var statsGrid: some View {
        let cells: [(String, String, String)] = [
            ("距离", String(format: "%.1f", drive.distanceKm), "km"),
            ("时长", "\(drive.durationMin)", "min"),
            ("最高速度", "\(maxSpeed)", "km/h"),
            ("均速", "\(avgSpeed)", "km/h"),
            ("能耗", String(format: "%.1f", energyKwh), "kWh"),
            ("效率", "\(drive.efficiency)", "Wh/km"),
        ]
        return LazyVGrid(
            columns: [GridItem(.flexible(), spacing: 16), GridItem(.flexible(), spacing: 16)],
            spacing: 16
        ) {
            ForEach(cells, id: \.0) { cell in
                statCell(label: cell.0, value: cell.1, unit: cell.2)
            }
        }
    }

    private func statCell(label: String, value: String, unit: String) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            Text(label)
                .font(.system(size: 11, weight: .bold))
                .tracking(0.8)
                .foregroundColor(StitchColors.onSurfaceVariant)
            HStack(alignment: .firstTextBaseline, spacing: 2) {
                Spacer()
                Text(value)
                    .font(.custom("JetBrainsMono-Medium", size: 20))
                    .foregroundColor(StitchColors.onSurface)
                Text(unit)
                    .font(.system(size: 13))
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
        }
        .padding(16)
        .frame(maxWidth: .infinity)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.onSurface, lineWidth: 1))
    }

    // MARK: - Battery Card
    private var batteryCard: some View {
        let start = drive.startBatteryLevel
        let end = drive.endBatteryLevel

        return VStack(alignment: .leading, spacing: 16) {
            Text("电量变化")
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(StitchColors.onSurface)

            HStack(spacing: 16) {
                Text("\(start)%")
                    .font(StitchFont.dataMd())
                    .foregroundColor(StitchColors.onSurface)

                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Capsule()
                            .fill(StitchColors.surfaceContainerHigh)
                            .frame(height: 12)
                        let leftFrac = CGFloat(end) / 100.0
                        let widthFrac = CGFloat(start - end) / 100.0
                        Capsule()
                            .fill(StitchColors.primary)
                            .frame(width: max(0, geo.size.width * widthFrac), height: 12)
                            .offset(x: geo.size.width * leftFrac)
                    }
                }
                .frame(height: 12)

                Text("\(end)%")
                    .font(StitchFont.dataMd())
                    .foregroundColor(StitchColors.onSurface)
            }

            Text("-\(start - end)%")
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(StitchColors.onSurfaceVariant)
                .frame(maxWidth: .infinity, alignment: .center)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.onSurface, lineWidth: 1))
    }

    // MARK: - Chart Card
    private var chartCard: some View {
        let values = chartValues(selectedTab)
        let peak = Int(values.max() ?? 0)

        return VStack(alignment: .leading, spacing: 16) {
            Text("行程曲线")
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(StitchColors.onSurface)

            // Tabs
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(DriveChartTab.allCases) { tab in
                        tabChip(tab)
                    }
                }
            }

            // Chart
            LineCurveChart(values: values, peakLabel: "\(peak) \(selectedTab.unit)")
                .frame(height: 160)

            // X axis
            HStack {
                Text("0")
                Spacer()
                Text("\(drive.durationMin / 2) min")
                Spacer()
                Text("\(drive.durationMin) min")
            }
            .font(.custom("JetBrainsMono-Medium", size: 10))
            .foregroundColor(StitchColors.onSurfaceVariant)

            Text("模拟数据 — 基于行程摘要")
                .font(.system(size: 10))
                .tracking(0.5)
                .foregroundColor(StitchColors.onSurfaceVariant)
                .frame(maxWidth: .infinity, alignment: .center)
        }
        .padding(16)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.onSurface, lineWidth: 1))
    }

    private func tabChip(_ tab: DriveChartTab) -> some View {
        let selected = tab == selectedTab
        return Text(tab.rawValue)
            .font(.system(size: 12, weight: .medium))
            .foregroundColor(selected ? StitchColors.white : StitchColors.onSurface)
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(selected ? StitchColors.primary : StitchColors.white)
            .clipShape(Capsule())
            .overlay(
                Capsule().stroke(
                    selected ? Color.clear : StitchColors.onSurface,
                    lineWidth: 1
                )
            )
            .onTapGesture { selectedTab = tab }
    }

    // MARK: - Export Button
    private var exportButton: some View {
        Button(action: {}) {
            HStack(spacing: 8) {
                Image(systemName: "square.and.arrow.down")
                    .font(.system(size: 16))
                Text("导出此行程")
                    .font(.system(size: 14, weight: .medium))
            }
            .foregroundColor(StitchColors.onSurface)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 12)
            .background(StitchColors.background)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.onSurface, lineWidth: 1))
        }
    }

    // MARK: - Chart Data (simulated)
    private func chartValues(_ tab: DriveChartTab) -> [Double] {
        switch tab {
        case .speed:
            return [0, 45, 80, 65, 50, 90, 70, 55, 40, 30,
                    60, 75, 85, 50, 35, 20, 55, 70, 45, 30,
                    65, 80, 60, 40, 25, 50, 75, 55, 35, 0]
        case .power:
            return [0, 20, 60, 40, 30, 80, 110, 50, 35, 25,
                    55, 90, 120, 70, 40, 15, 45, 85, 50, 20,
                    60, 95, 75, 45, 20, 40, 70, 55, 25, 0]
        case .altitude:
            return [50, 80, 150, 200, 300, 350, 420, 380, 250, 180,
                    120, 90, 150, 220, 310, 400, 450, 350, 200, 100,
                    80, 120, 180, 250, 320, 380, 300, 200, 100, 50]
        case .insideTemp:
            return [22, 23, 24, 25, 26, 27, 26, 25, 24, 23,
                    22, 21, 22, 23, 24, 25, 26, 25, 24, 23,
                    22, 21, 20, 21, 22, 23, 24, 23, 22, 21]
        case .outsideTemp:
            return [15, 15, 16, 16, 17, 17, 18, 18, 17, 16,
                    15, 15, 16, 17, 18, 18, 19, 18, 17, 16,
                    15, 14, 14, 15, 16, 17, 17, 16, 15, 14]
        }
    }

    // MARK: - Date Formatting
    private func formattedDateTime(_ isoString: String) -> String {
        if let date = ISO8601Parser.parse(isoString) {
            let fmt = DateFormatter()
            fmt.dateFormat = "M月d日 HH:mm"
            fmt.locale = Locale(identifier: "zh_CN")
            return fmt.string(from: date)
        }
        return isoString
    }
}

// MARK: - Route Trace Shape (dashed curve)
private struct RouteTraceShape: Shape {
    func path(in rect: CGRect) -> Path {
        var p = Path()
        p.move(to: CGPoint(x: rect.minX, y: rect.maxY * 0.85))
        p.addQuadCurve(
            to: CGPoint(x: rect.maxX * 0.9, y: rect.maxY * 0.2),
            control: CGPoint(x: rect.midX, y: rect.maxY * 0.7)
        )
        return p
    }
}

// MARK: - Route Endpoints (start dot + end flag square)
private struct RouteEndpoints: Shape {
    func path(in rect: CGRect) -> Path {
        var p = Path()
        // start dot
        let dot = CGRect(x: rect.minX - 4, y: rect.maxY * 0.85 - 4, width: 8, height: 8)
        p.addEllipse(in: dot)
        // end flag marker
        let flag = CGRect(x: rect.maxX * 0.9 - 5, y: rect.maxY * 0.2 - 5, width: 10, height: 10)
        p.addRect(flag)
        return p
    }
}

// MARK: - Line Curve Chart (gold main line + dashed grid)
private struct LineCurveChart: View {
    let values: [Double]
    let peakLabel: String

    var body: some View {
        ZStack(alignment: .topTrailing) {
            GeometryReader { geo in
                let w = geo.size.width
                let h = geo.size.height

                // dashed grid
                Path { p in
                    for i in 1...3 {
                        let y = h * CGFloat(i) / 4.0
                        p.move(to: CGPoint(x: 0, y: y))
                        p.addLine(to: CGPoint(x: w, y: y))
                    }
                }
                .stroke(StitchColors.surfaceContainerHigh, style: StrokeStyle(lineWidth: 1, dash: [6, 6]))

                // axes
                Path { p in
                    p.move(to: CGPoint(x: 0, y: h)); p.addLine(to: CGPoint(x: w, y: h))
                    p.move(to: CGPoint(x: 0, y: 0)); p.addLine(to: CGPoint(x: 0, y: h))
                }
                .stroke(StitchColors.surfaceContainerHigh, lineWidth: 1.5)

                // main gold curve
                curvePath(w: w, h: h)
                    .stroke(StitchColors.accent, style: StrokeStyle(lineWidth: 3, lineJoin: .round))
            }

            // Peak label
            Text(peakLabel)
                .font(.custom("JetBrainsMono-Medium", size: 10))
                .foregroundColor(StitchColors.onSurface)
                .padding(.horizontal, 6)
                .padding(.vertical, 2)
                .background(StitchColors.background)
                .overlay(RoundedRectangle(cornerRadius: 4).stroke(StitchColors.onSurface, lineWidth: 1))
                .padding(.top, 4)
                .padding(.trailing, 8)
        }
    }

    private func curvePath(w: CGFloat, h: CGFloat) -> Path {
        var p = Path()
        guard values.count > 1 else { return p }
        let minV = values.min() ?? 0
        let maxV = values.max() ?? 1
        let range = (maxV - minV) > 0 ? (maxV - minV) : 1
        let stepX = w / CGFloat(values.count - 1)
        for (i, v) in values.enumerated() {
            let x = stepX * CGFloat(i)
            let y = h - CGFloat((v - minV) / range) * (h * 0.9) - h * 0.05
            if i == 0 { p.move(to: CGPoint(x: x, y: y)) } else { p.addLine(to: CGPoint(x: x, y: y)) }
        }
        return p
    }
}

// MARK: - Preview
#Preview {
    NavigationStack {
        DriveDetailView(
            drive: Drive(
                id: 1, carId: 1,
                startDate: "2025-06-22T10:30:00.000Z",
                endDate: "2025-06-22T11:15:00.000Z",
                distanceKm: 31.2,
                durationMin: 22,
                efficiency: 154,
                consumptionKwh: 4.8,
                startAddress: "家",
                endAddress: "公司",
                outsideTempAvg: 22.5,
                startBatteryLevel: 85,
                endBatteryLevel: 72
            )
        )
    }
}
