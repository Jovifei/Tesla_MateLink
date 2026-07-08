import SwiftUI

// MARK: - Stitch brand activity colors (from Stitch tailwind config)

private enum BrandColors {
    static let drive  = Color(hex: "059669")
    static let charge = Color(hex: "F59E0B")
    static let rest   = Color(hex: "A3A3A3")
    static let sentry = Color(hex: "7C3AED")

    static func color(for type: String) -> Color {
        switch type {
        case "drive": return drive
        case "charge": return charge
        case "sentry": return sentry
        default: return rest
        }
    }
}

// MARK: - Timeline Event Model

struct TimelineEvent: Identifiable, Equatable {
    let id: String          // "drive_123" or "charge_456"
    let type: String        // "drive" or "charge"
    let start: Date
    let end: Date?
    let label: String
    let detail: String
    let metrics: String

    static func == (lhs: TimelineEvent, rhs: TimelineEvent) -> Bool { lhs.id == rhs.id }
}

// MARK: - 24h Activity Segment (Stitch 1:1 model)

private struct ActivitySegment: Identifiable {
    let id = UUID()
    let fraction: CGFloat   // width fraction of 24h (0..1)
    let type: String
}

// TODO: replace with real per-day aggregation from TimelineViewModel.
// Mirrors the Stitch 1:1 mock (2025/07/02).
private let mockSegments: [ActivitySegment] = [
    ActivitySegment(fraction: 0.291, type: "rest"),
    ActivitySegment(fraction: 0.052, type: "drive"),
    ActivitySegment(fraction: 0.385, type: "rest"),
    ActivitySegment(fraction: 0.021, type: "charge"),
    ActivitySegment(fraction: 0.028, type: "drive"),
    ActivitySegment(fraction: 0.055, type: "rest"),
    ActivitySegment(fraction: 0.017, type: "drive"),
    ActivitySegment(fraction: 0.151, type: "rest")
]

// MARK: - Date Helpers

private func parseDate(_ raw: String) -> Date {
    ISO8601Parser.parse(raw) ?? .distantPast
}

// MARK: - ViewModel

@MainActor
final class TimelineViewModel: ObservableObject {
    @Published var events: [TimelineEvent] = []
    @Published var isLoading = true

    func load(mock: MockAPI, carId: Int) async {
        isLoading = true
        defer { isLoading = false }

        let drives = await mock.getDrives(carId)
        let charges = await mock.getCharges(carId)

        var merged: [TimelineEvent] = []

        for d in drives {
            let start = parseDate(d.startDate)
            let end = parseDate(d.endDate)
            merged.append(TimelineEvent(
                id: "drive_\(d.id)",
                type: "drive",
                start: start,
                end: end,
                label: d.startAddress.isEmpty ? "驾驶" : "驾驶至 \(shortAddress(d.endAddress))",
                detail: "\(d.startAddress) \u{2192} \(d.endAddress)",
                metrics: "\(String(format: "%.1f", d.distanceKm)) km \u{00b7} \(d.durationMin) min"
            ))
        }

        for c in charges {
            let start = parseDate(c.startDate)
            let end = c.endDate.map(parseDate)
            let typeLabel = c.chargeType.isEmpty ? "充电" : "\(c.chargeType) 充电"
            let addr = c.address.isEmpty ? "未知" : shortAddress(c.address)
            merged.append(TimelineEvent(
                id: "charge_\(c.id)",
                type: "charge",
                start: start,
                end: end,
                label: typeLabel,
                detail: addr,
                metrics: "+\(String(format: "%.1f", c.chargeEnergyAdded)) kWh \u{00b7} \u{00a5}\(String(format: "%.2f", c.cost))"
            ))
        }

        merged.sort { $0.start > $1.start }
        self.events = merged
    }
}

private func shortAddress(_ addr: String) -> String {
    let parts = addr.components(separatedBy: ", ")
    if parts.count >= 2 { return parts[0] }
    return addr.components(separatedBy: ",").first ?? addr
}

// MARK: - TimelineView

struct TimelineView: View {
    @EnvironmentObject var state: AppState
    @StateObject private var vm = TimelineViewModel()

    var body: some View {
        Group {
            if vm.isLoading {
                VStack(spacing: 16) {
                    ProgressView().tint(StitchColors.onSurface)
                    Text("加载中...")
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
            } else {
                content
            }
        }
        .background(StitchColors.background)
        .navigationTitle("时间线")
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItem(placement: .principal) {
                HStack(spacing: 4) {
                    Image(systemName: "chevron.left")
                        .font(.system(size: 14))
                        .foregroundColor(StitchColors.onSurfaceVariant)
                    Text("2025年7月2日")
                        .font(StitchFont.labelCaps())
                        .foregroundColor(StitchColors.primary)
                    Image(systemName: "chevron.right")
                        .font(.system(size: 14))
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
            }
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    Task { await vm.load(mock: state.mock, carId: state.currentCarId) }
                } label: {
                    Image(systemName: "arrow.clockwise")
                        .foregroundColor(StitchColors.primary)
                }
            }
        }
        .task { await vm.load(mock: state.mock, carId: state.currentCarId) }
    }

    private var content: some View {
        ScrollView {
            VStack(spacing: 24) {
                SummaryCard()
                ActivityBarCard()
                ActivityDetailCard()
                PieChartCard()
                WeeklyChartCard()
                Spacer(minLength: 24)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 24)
        }
    }
}

// MARK: - Summary Card

private struct SummaryCard: View {
    var body: some View {
        StitchCard {
            HStack(spacing: 0) {
                summaryColumn("驾驶", h: "4", m: "32", pct: "19%", color: BrandColors.drive)
                divider
                summaryColumn("充电", h: "2", m: "15", pct: "9%", color: BrandColors.charge)
                divider
                summaryColumn("休息", h: "17", m: "13", pct: "72%", color: BrandColors.rest)
            }
        }
    }

    private var divider: some View {
        Rectangle()
            .fill(StitchColors.outlineVariant)
            .frame(width: 1, height: 60)
    }

    private func summaryColumn(_ label: String, h: String, m: String, pct: String, color: Color) -> some View {
        VStack(spacing: 4) {
            Text(label)
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
            HStack(alignment: .lastTextBaseline, spacing: 1) {
                Text(h).font(StitchFont.dataLg()).foregroundColor(color)
                Text("h").font(.system(size: 12)).foregroundColor(color)
                Text(m).font(StitchFont.dataLg()).foregroundColor(color).padding(.leading, 3)
                Text("min").font(.system(size: 12)).foregroundColor(color)
            }
            Text(pct)
                .font(StitchFont.dataMd())
                .foregroundColor(StitchColors.onSurfaceVariant.opacity(0.6))
        }
        .frame(maxWidth: .infinity)
    }
}

// MARK: - 24h Activity Bar Card

private struct ActivityBarCard: View {
    var body: some View {
        StitchCard {
            HStack {
                Text("今日活动·24小时")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                Text("2025/07/02")
                    .font(StitchFont.dataMd())
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            .padding(.bottom, 24)

            // Segmented 24h strip
            GeometryReader { geo in
                HStack(spacing: 0) {
                    ForEach(mockSegments) { seg in
                        BrandColors.color(for: seg.type)
                            .frame(width: max(0, geo.size.width * seg.fraction))
                            .overlay(alignment: .leading) {
                                Rectangle().fill(Color.white).frame(width: 1)
                            }
                    }
                }
            }
            .frame(height: 32)
            .clipShape(RoundedRectangle(cornerRadius: 4))

            // Time axis
            HStack(spacing: 0) {
                axisLabel("00:00", 0.291, StitchColors.onSurfaceVariant, StitchColors.outlineVariant)
                axisLabel("07:00", 0.052, BrandColors.drive, BrandColors.drive)
                axisLabel("08:15", 0.385, StitchColors.onSurfaceVariant, StitchColors.outlineVariant)
                axisLabel("17:30", 0.021, BrandColors.charge, BrandColors.charge)
                axisLabel("18:00", 0.028, BrandColors.drive, BrandColors.drive)
                axisLabel("20:00", 0.223, StitchColors.onSurfaceVariant, StitchColors.outlineVariant)
            }
            .padding(.top, 6)

            // Dynamic labels
            VStack(alignment: .leading, spacing: 12) {
                dynamicLabel(BrandColors.drive, "07:00 - 08:15", "家 → 公司 (31km)")
                dynamicLabel(BrandColors.charge, "17:30 - 18:00", "公司超充 (+18kWh)")
            }
            .padding(.top, 16)
            .frame(maxWidth: .infinity, alignment: .leading)
        }
    }

    private func axisLabel(_ time: String, _ fraction: CGFloat, _ textColor: Color, _ borderColor: Color) -> some View {
        HStack(spacing: 2) {
            Rectangle().fill(borderColor).frame(width: 1, height: 14)
            Text(time)
                .font(.custom("JetBrainsMono-Medium", size: 10))
                .foregroundColor(textColor)
            Spacer(minLength: 0)
        }
        .frame(maxWidth: .infinity)
        .layoutPriority(Double(fraction))
    }

    private func dynamicLabel(_ dot: Color, _ time: String, _ desc: String) -> some View {
        HStack(spacing: 8) {
            Circle().fill(dot).frame(width: 6, height: 6)
            Text(time)
                .font(StitchFont.dataMd())
                .foregroundColor(StitchColors.primary)
            Text(desc)
                .font(StitchFont.bodySm())
                .foregroundColor(StitchColors.onSurfaceVariant)
        }
    }
}

// MARK: - Activity Detail Card

private struct ActivityDetailCard: View {
    var body: some View {
        StitchCard {
            Text("活动详情")
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
                .padding(.bottom, 24)
            VStack(spacing: 16) {
                // TODO: bind to real segmented events once ViewModel exposes them.
                detailRow(BrandColors.drive, "car.fill", "20:00 - 20:25", "12.5km", StitchColors.primary, "驾驶 · 25min")
                detailRow(BrandColors.rest, "parkingsign", "18:40 - 20:00", "公司停车", StitchColors.onSurfaceVariant, "休息 · 1h 20min")
                detailRow(BrandColors.drive, "car.fill", "18:00 - 18:40", "18.2km", StitchColors.primary, "驾驶 · 40min")
                detailRow(BrandColors.charge, "bolt.fill", "17:30 - 18:00", "+18.5kWh", BrandColors.charge, "充电 · 30min", cost: "\u{00a5}9.25")
            }
        }
    }

    @ViewBuilder
    private func detailRow(_ accent: Color, _ icon: String, _ time: String, _ value: String, _ valueColor: Color, _ subtitle: String, cost: String? = nil) -> some View {
        HStack(spacing: 16) {
            RoundedRectangle(cornerRadius: 2)
                .fill(accent)
                .frame(width: 4, height: 48)
            Image(systemName: icon)
                .font(.system(size: 18))
                .foregroundColor(accent)
                .frame(width: 36, height: 36)
                .background(StitchColors.surfaceContainer)
                .clipShape(RoundedRectangle(cornerRadius: 4))
            VStack(alignment: .leading, spacing: 2) {
                HStack(alignment: .lastTextBaseline) {
                    Text(time).font(StitchFont.dataMd()).foregroundColor(StitchColors.onSurface)
                    Spacer()
                    Text(value).font(StitchFont.dataMd()).foregroundColor(valueColor)
                }
                if let cost = cost {
                    HStack {
                        Text(subtitle).font(StitchFont.bodySm()).foregroundColor(StitchColors.onSurfaceVariant)
                        Spacer()
                        Text(cost).font(StitchFont.dataMd()).foregroundColor(StitchColors.onSurface)
                    }
                } else {
                    Text(subtitle)
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                        .frame(maxWidth: .infinity, alignment: .leading)
                }
            }
        }
    }
}

// MARK: - Pie Chart Card (时段占比)

private struct PieChartCard: View {
    private let slices: [(Color, Double)] = [
        (BrandColors.rest, 72),
        (BrandColors.drive, 19),
        (BrandColors.charge, 9)
    ]

    var body: some View {
        StitchCard {
            Text("时段占比")
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
                .padding(.bottom, 24)
            VStack(spacing: 24) {
                ZStack {
                    ForEach(0..<slices.count, id: \.self) { i in
                        DonutSlice(startPct: startPct(i), sweepPct: slices[i].1)
                            .stroke(slices[i].0, style: StrokeStyle(lineWidth: 18))
                    }
                    VStack(spacing: 2) {
                        Text("TOTAL")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(StitchColors.onSurfaceVariant)
                        Text("24h")
                            .font(StitchFont.dataLg())
                            .foregroundColor(StitchColors.onSurface)
                    }
                }
                .frame(width: 160, height: 160)

                VStack(spacing: 8) {
                    legendRow(BrandColors.drive, "驾驶", "19%")
                    legendRow(BrandColors.charge, "充电", "9%")
                    legendRow(BrandColors.rest, "休息", "72%")
                }
            }
            .frame(maxWidth: .infinity)
        }
    }

    private func startPct(_ index: Int) -> Double {
        slices.prefix(index).reduce(0) { $0 + $1.1 }
    }

    private func legendRow(_ dot: Color, _ label: String, _ value: String) -> some View {
        HStack {
            HStack(spacing: 8) {
                Circle().fill(dot).frame(width: 8, height: 8)
                Text(label).font(StitchFont.bodySm()).foregroundColor(StitchColors.onSurface)
            }
            Spacer()
            Text(value).font(StitchFont.dataMd()).foregroundColor(StitchColors.onSurface)
        }
    }
}

// Donut slice shape (percentages of 360°, starting at top / -90°)
private struct DonutSlice: Shape {
    let startPct: Double
    let sweepPct: Double

    func path(in rect: CGRect) -> Path {
        let radius = min(rect.width, rect.height) / 2 - 9  // half of 18pt stroke
        let center = CGPoint(x: rect.midX, y: rect.midY)
        let startAngle = Angle(degrees: startPct / 100 * 360 - 90)
        let endAngle = Angle(degrees: (startPct + sweepPct) / 100 * 360 - 90)
        var p = Path()
        p.addArc(center: center, radius: radius, startAngle: startAngle, endAngle: endAngle, clockwise: false)
        return p
    }
}

// MARK: - Weekly Chart Card (周对比)

private struct WeekBar: Identifiable {
    let id = UUID()
    let rest: CGFloat
    let drive: CGFloat
    let charge: CGFloat
    let label: String
    var active: Bool = false
    var dim: Bool = false
}

// TODO: replace with real weekly aggregation.
private let mockWeek: [WeekBar] = [
    WeekBar(rest: 48, drive: 16, charge: 8, label: "一"),
    WeekBar(rest: 40, drive: 24, charge: 16, label: "二"),
    WeekBar(rest: 56, drive: 32, charge: 12, label: "三", active: true),
    WeekBar(rest: 64, drive: 8, charge: 0, label: "四", dim: true),
    WeekBar(rest: 48, drive: 20, charge: 12, label: "五", dim: true),
    WeekBar(rest: 32, drive: 40, charge: 24, label: "六", dim: true),
    WeekBar(rest: 40, drive: 16, charge: 0, label: "日", dim: true)
]

private struct WeeklyChartCard: View {
    var body: some View {
        StitchCard {
            Text("周对比")
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
                .padding(.bottom, 16)
            HStack(alignment: .bottom, spacing: 4) {
                ForEach(mockWeek) { bar in
                    VStack(spacing: 8) {
                        VStack(spacing: 2) {
                            RoundedRectangle(cornerRadius: 2)
                                .fill(BrandColors.rest.opacity(bar.dim ? 0.4 : 1))
                                .frame(height: bar.rest)
                            if bar.drive > 0 {
                                Rectangle()
                                    .fill(BrandColors.drive.opacity(bar.dim ? 0.4 : 1))
                                    .frame(height: bar.drive)
                            }
                            if bar.charge > 0 {
                                RoundedRectangle(cornerRadius: 2)
                                    .fill(BrandColors.charge.opacity(bar.dim ? 0.4 : 1))
                                    .frame(height: bar.charge)
                            }
                        }
                        .overlay(
                            RoundedRectangle(cornerRadius: 2)
                                .stroke(bar.active ? StitchColors.primary : Color.clear, lineWidth: 2)
                                .padding(-2)
                        )
                        Text(bar.label)
                            .font(.custom("JetBrainsMono-Medium", size: 10))
                            .fontWeight(bar.active ? .bold : .regular)
                            .foregroundColor(bar.active ? StitchColors.primary : StitchColors.onSurfaceVariant)
                    }
                    .frame(maxWidth: .infinity, alignment: .bottom)
                }
            }
            .frame(height: 160, alignment: .bottom)

            Rectangle()
                .fill(StitchColors.outlineVariant)
                .frame(height: 1)
                .padding(.top, 16)

            HStack {
                Text("平均活跃").font(StitchFont.bodySm()).foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                Text("6h 12min").font(StitchFont.dataMd()).foregroundColor(StitchColors.primary)
            }
            .padding(.top, 16)
        }
    }
}

// MARK: - Preview

#Preview {
    NavigationStack {
        TimelineView()
            .environmentObject(AppState())
    }
}
