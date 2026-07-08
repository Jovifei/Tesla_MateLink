import SwiftUI

// MARK: - Aggregation Models

struct MonthSummary: Identifiable {
    let id = UUID()
    let year: Int
    let month: Int
    let label: String
    let totalKm: Double
    let totalKwh: Double
    let driveCount: Int
    let avgEfficiency: Int
}

struct DaySummary: Identifiable {
    let id = UUID()
    let date: String
    let label: String
    let totalKm: Double
    let driveCount: Int
}

// MARK: - Navigation Target

enum StatsNavTarget: Hashable {
    case month(year: Int, month: Int)
    case day(date: String)
}

// MARK: - 里程钻取 - 年度总览 (Root, Stitch 白色瑞士风)

struct StatisticsView: View {
    @EnvironmentObject var state: AppState
    @State private var drives: [Drive] = []
    @State private var months: [MonthSummary] = []
    @State private var loading = true

    private let calendar = Calendar.current

    var body: some View {
        NavigationStack {
            Group {
                if loading {
                    ProgressView("加载中...")
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        VStack(spacing: 16) {
                            yearTotalCard        // 1. 年度总里程大数字
                            monthlyTrendCard     // 2. 12月柱状趋势
                            sceneDistributionCard // 3. 场景分布
                            heatmapCard          // 4. 365天热力网格
                            topDaysCard          // 5. Top5 里程日
                        }
                        .padding(16)
                    }
                }
            }
            .navigationTitle("里程钻取")
            .navigationBarTitleDisplayMode(.inline)
            .background(StitchColors.background)
            .task { await load() }
            .navigationDestination(for: StatsNavTarget.self) { target in
                switch target {
                case .month(let y, let m):
                    MonthDetailView(drives: drives, year: y, month: m)
                case .day(let d):
                    DayDetailView(drives: drives, date: d)
                }
            }
        }
    }

    // MARK: - 1. 年度总里程
    private var yearTotalCard: some View {
        let totalKm     = months.reduce(0) { $0 + $1.totalKm }
        let totalKwh    = months.reduce(0) { $0 + $1.totalKwh }
        let totalDrives = months.reduce(0) { $0 + $1.driveCount }
        return StitchCard {
            StitchLabel("\(currentYear) 年度总里程")
            HStack(alignment: .bottom, spacing: 6) {
                Text("\(Int(totalKm))")
                    .font(.custom("JetBrainsMono-Medium", size: 44))
                    .foregroundColor(StitchColors.onSurface)
                Text("km")
                    .font(StitchFont.bodyLg())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                    .padding(.bottom, 8)
            }
            .padding(.top, 8)
            Divider().padding(.vertical, 12)
            StitchDataRow(label: "总能耗", value: "\(Int(totalKwh)) kWh")
            Spacer().frame(height: 8)
            StitchDataRow(label: "总行程", value: "\(totalDrives) 次")
        }
    }

    // MARK: - 2. 月度趋势柱状
    private var monthlyTrendCard: some View {
        StitchCard {
            StitchLabel("月度趋势")
            if months.isEmpty {
                Text("暂无行驶数据")
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                    .padding(.top, 8)
            } else {
                let maxKm = max(months.map { $0.totalKm }.max() ?? 1, 1)
                HStack(alignment: .bottom, spacing: 6) {
                    ForEach(months.sorted { $0.month < $1.month }) { m in
                        NavigationLink(value: StatsNavTarget.month(year: m.year, month: m.month)) {
                            VStack(spacing: 4) {
                                Spacer(minLength: 0)
                                RoundedRectangle(cornerRadius: 2)
                                    .fill(StitchColors.onSurface)
                                    .frame(height: max(CGFloat(m.totalKm / maxKm) * 110, 4))
                                Text("\(m.month)")
                                    .font(.custom("JetBrainsMono-Medium", size: 9))
                                    .foregroundColor(StitchColors.onSurfaceVariant)
                            }
                        }
                        .buttonStyle(.plain)
                        .frame(maxWidth: .infinity)
                    }
                }
                .frame(height: 140)
                .padding(.top, 16)
                Text("点击柱状钻取至月度详情")
                    .font(.system(size: 10))
                    .foregroundColor(StitchColors.onSurfaceVariant)
                    .padding(.top, 8)
            }
        }
    }

    // MARK: - 3. 场景分布
    private var sceneDistributionCard: some View {
        // TODO: 场景分布数据待接入 (drive.tags/geofence 分类), 暂用 mock 比例
        let totalKm = months.reduce(0) { $0 + $1.totalKm }
        let scenes: [(String, Double)] = [
            ("通勤", 0.45), ("出行", 0.30), ("出差", 0.15), ("其他", 0.10)
        ]
        return StitchCard {
            StitchLabel("场景分布")
            VStack(spacing: 12) {
                ForEach(scenes, id: \.0) { scene in
                    VStack(spacing: 6) {
                        HStack {
                            Text(scene.0)
                                .font(StitchFont.bodySm())
                                .foregroundColor(StitchColors.onSurface)
                            Spacer()
                            Text("\(Int(totalKm * scene.1)) km")
                                .font(StitchFont.dataMd())
                                .foregroundColor(StitchColors.onSurface)
                        }
                        GeometryReader { geo in
                            ZStack(alignment: .leading) {
                                RoundedRectangle(cornerRadius: 3)
                                    .fill(StitchColors.surfaceContainerHigh)
                                    .frame(height: 6)
                                RoundedRectangle(cornerRadius: 3)
                                    .fill(StitchColors.onSurface)
                                    .frame(width: geo.size.width * scene.1, height: 6)
                            }
                        }
                        .frame(height: 6)
                    }
                }
            }
            .padding(.top, 16)
        }
    }

    // MARK: - 4. 365 天热力网格
    private var heatmapCard: some View {
        // TODO: 每日里程强度待接入, 暂用确定性 mock
        let intensity = Self.mockIntensity()
        return StitchCard {
            StitchLabel("全年活跃热力")
            HStack(alignment: .top, spacing: 3) {
                ForEach(0..<53, id: \.self) { week in
                    VStack(spacing: 3) {
                        ForEach(0..<7, id: \.self) { dow in
                            let idx = week * 7 + dow
                            RoundedRectangle(cornerRadius: 1)
                                .fill(Self.heatColor(idx < intensity.count ? intensity[idx] : -1))
                                .frame(width: 6, height: 6)
                        }
                    }
                }
            }
            .padding(.top, 16)
            HStack(spacing: 3) {
                Text("少").font(.system(size: 10)).foregroundColor(StitchColors.onSurfaceVariant)
                ForEach([0.1, 0.4, 0.7, 1.0], id: \.self) { v in
                    RoundedRectangle(cornerRadius: 1)
                        .fill(Self.heatColor(v))
                        .frame(width: 6, height: 6)
                }
                Text("多").font(.system(size: 10)).foregroundColor(StitchColors.onSurfaceVariant)
            }
            .padding(.top, 12)
        }
    }

    // MARK: - 5. Top5 里程日
    private var topDaysCard: some View {
        // TODO: Top5 里程日待接入 (按日聚合排序), 暂用 mock
        let topDays: [(String, Int)] = [
            ("2026-05-02", 412), ("2026-04-18", 386), ("2026-06-11", 355),
            ("2026-03-27", 328), ("2026-05-30", 301)
        ]
        return StitchCard {
            StitchLabel("Top5 里程日")
            VStack(spacing: 12) {
                ForEach(Array(topDays.enumerated()), id: \.offset) { idx, item in
                    HStack {
                        ZStack {
                            RoundedRectangle(cornerRadius: 4)
                                .fill(StitchColors.onSurface)
                                .frame(width: 20, height: 20)
                            Text("\(idx + 1)")
                                .font(.custom("JetBrainsMono-Medium", size: 11))
                                .foregroundColor(StitchColors.white)
                        }
                        Text(item.0)
                            .font(StitchFont.dataMd())
                            .foregroundColor(StitchColors.onSurface)
                        Spacer()
                        Text("\(item.1) km")
                            .font(StitchFont.dataMd())
                            .foregroundColor(StitchColors.onSurface)
                    }
                }
            }
            .padding(.top, 16)
        }
    }

    // MARK: - Data Helpers
    private var currentYear: Int { calendar.component(.year, from: Date()) }

    private func parseDate(_ iso: String) -> Date? { ISO8601Parser.parse(iso) }

    private func dateComponents(_ iso: String) -> (year: Int, month: Int)? {
        guard let d = parseDate(iso) else { return nil }
        return (calendar.component(.year, from: d), calendar.component(.month, from: d))
    }

    private func aggregateMonth(year: Int, month: Int) -> MonthSummary {
        let monthDrives = drives.filter { d in
            guard let (y, m) = dateComponents(d.startDate) else { return false }
            return y == year && m == month
        }
        let totalKm  = monthDrives.reduce(0) { $0 + $1.distanceKm }
        let totalKwh = monthDrives.reduce(0) { $0 + $1.consumptionKwh }
        let avgEff   = monthDrives.isEmpty ? 0
            : Int(monthDrives.reduce(0) { $0 + $1.efficiency } / monthDrives.count)
        let monthName = DateFormatter().shortMonthSymbols[month - 1]
        return MonthSummary(
            year: year, month: month, label: monthName,
            totalKm: totalKm, totalKwh: totalKwh,
            driveCount: monthDrives.count, avgEfficiency: avgEff
        )
    }

    private func load() async {
        loading = true
        if state.isMockMode {
            drives = await state.mock.getDrives(state.currentCarId)
        } else if let api = state.real {
            drives = (try? await api.fetch("/api/v1/cars/\(state.currentCarId)/drives")) ?? []
        }
        months = (1...12).map { aggregateMonth(year: currentYear, month: $0) }
        loading = false
    }

    // 确定性伪随机热力强度
    private static func mockIntensity() -> [Double] {
        var seed: UInt64 = 20260707
        return (0..<365).map { _ in
            seed = seed &* 6364136223846793005 &+ 1442695040888963407
            return Double((seed >> 33) & 0xFF) / 255.0
        }
    }

    private static func heatColor(_ v: Double) -> Color {
        switch v {
        case ..<0:      return Color(hex: "F1EDEC")   // 无数据
        case ..<0.25:   return Color(hex: "D6D3D1")
        case ..<0.5:    return Color(hex: "9CA3AF")
        case ..<0.75:   return Color(hex: "4B5563")
        default:        return Color(hex: "1C1B1B")
        }
    }
}

// MARK: - 里程钻取 - 月度详情 (2级)

struct MonthDetailView: View {
    let drives: [Drive]
    let year: Int
    let month: Int

    @State private var daySummaries: [DaySummary] = []

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                // 月度汇总
                let totalKm    = daySummaries.reduce(0) { $0 + $1.totalKm }
                let driveCount = daySummaries.reduce(0) { $0 + $1.driveCount }
                StitchCard {
                    StitchLabel("月度总里程")
                    HStack(alignment: .bottom, spacing: 6) {
                        Text("\(Int(totalKm))")
                            .font(.custom("JetBrainsMono-Medium", size: 40))
                            .foregroundColor(StitchColors.onSurface)
                        Text("km")
                            .font(StitchFont.bodyLg())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                            .padding(.bottom, 6)
                    }
                    .padding(.top, 8)
                    Divider().padding(.vertical, 12)
                    StitchDataRow(label: "活跃天数", value: "\(daySummaries.count) 天")
                    Spacer().frame(height: 8)
                    StitchDataRow(label: "行程总数", value: "\(driveCount) 次")
                }

                // 每日列表 (点击钻取至当日)
                StitchCard {
                    StitchLabel("每日里程")
                    if daySummaries.isEmpty {
                        Text("本月暂无行程")
                            .font(StitchFont.bodySm())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                            .padding(.top, 8)
                    } else {
                        VStack(spacing: 0) {
                            ForEach(daySummaries) { day in
                                NavigationLink(value: StatsNavTarget.day(date: day.date)) {
                                    HStack {
                                        VStack(alignment: .leading, spacing: 2) {
                                            Text(day.label)
                                                .font(StitchFont.bodyLg())
                                                .foregroundColor(StitchColors.onSurface)
                                            Text("\(day.driveCount) 次行程")
                                                .font(.system(size: 12))
                                                .foregroundColor(StitchColors.onSurfaceVariant)
                                        }
                                        Spacer()
                                        Text("\(Int(day.totalKm)) km")
                                            .font(StitchFont.dataMd())
                                            .foregroundColor(StitchColors.onSurface)
                                    }
                                    .padding(.vertical, 12)
                                }
                                .buttonStyle(.plain)
                                Divider()
                            }
                        }
                        .padding(.top, 8)
                    }
                }
            }
            .padding(16)
        }
        .navigationTitle(monthLabel)
        .navigationBarTitleDisplayMode(.inline)
        .background(StitchColors.background)
        .onAppear { buildDaySummaries() }
    }

    private var monthLabel: String { "\(year) 年 \(month) 月" }

    private var drivesForThisMonth: [Drive] {
        let prefix = "\(year)-\(String(format: "%02d", month))"
        return drives.filter { $0.startDate.hasPrefix(prefix) }
    }

    private static let dateFormatter: DateFormatter = {
        let f = DateFormatter(); f.dateFormat = "M月d日"; return f
    }()

    private func buildDaySummaries() {
        var dayMap: [String: [Drive]] = [:]
        for d in drivesForThisMonth {
            let dateKey = String(d.startDate.prefix(10))
            dayMap[dateKey, default: []].append(d)
        }
        daySummaries = dayMap.keys.sorted(by: >).map { dateKey in
            let dayDrives = dayMap[dateKey]!
            let totalKm   = dayDrives.reduce(0) { $0 + $1.distanceKm }
            var label = dateKey
            if let d = ISO8601Parser.parse(dateKey + "T12:00:00Z") {
                label = Self.dateFormatter.string(from: d)
            }
            return DaySummary(date: dateKey, label: label,
                              totalKm: totalKm, driveCount: dayDrives.count)
        }
    }
}

// MARK: - 里程钻取 - 当日行程 (3级)

struct DayDetailView: View {
    let drives: [Drive]
    let date: String

    private var dayDrives: [Drive] {
        drives.filter { $0.startDate.hasPrefix(date) }
    }

    var body: some View {
        let totalKm = dayDrives.reduce(0) { $0 + $1.distanceKm }
        return ScrollView {
            VStack(spacing: 16) {
                // 当日汇总
                StitchCard {
                    StitchLabel("当日总里程")
                    HStack(alignment: .bottom, spacing: 6) {
                        Text("\(Int(totalKm))")
                            .font(.custom("JetBrainsMono-Medium", size: 40))
                            .foregroundColor(StitchColors.onSurface)
                        Text("km")
                            .font(StitchFont.bodyLg())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                            .padding(.bottom, 6)
                    }
                    .padding(.top, 8)
                    Divider().padding(.vertical, 12)
                    StitchDataRow(label: "行程数", value: "\(dayDrives.count) 次")
                }

                // 行程明细
                if dayDrives.isEmpty {
                    StitchCard {
                        Text("当日无行程")
                            .font(StitchFont.bodySm())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                    }
                } else {
                    ForEach(dayDrives) { drive in
                        NavigationLink(destination: DriveDetailView(drive: drive)) {
                            StitchCard {
                                HStack {
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(drive.startAddress)
                                            .font(StitchFont.bodyLg())
                                            .foregroundColor(StitchColors.onSurface)
                                            .lineLimit(1)
                                        Text("→ \(drive.endAddress)")
                                            .font(.system(size: 13))
                                            .foregroundColor(StitchColors.onSurfaceVariant)
                                            .lineLimit(1)
                                    }
                                    Spacer()
                                    VStack(alignment: .trailing, spacing: 2) {
                                        Text("\(drive.distanceKm, specifier: "%.1f") km")
                                            .font(StitchFont.dataMd())
                                            .foregroundColor(StitchColors.onSurface)
                                        Text("\(drive.durationMin) min")
                                            .font(.custom("JetBrainsMono-Medium", size: 12))
                                            .foregroundColor(StitchColors.onSurfaceVariant)
                                    }
                                }
                            }
                        }
                        .buttonStyle(.plain)
                    }
                }
            }
            .padding(16)
        }
        .navigationTitle(formattedDate)
        .navigationBarTitleDisplayMode(.inline)
        .background(StitchColors.background)
    }

    private static let dateFormatter: DateFormatter = {
        let f = DateFormatter(); f.dateFormat = "M月d日 EEEE"; return f
    }()

    private var formattedDate: String {
        if let d = ISO8601Parser.parse(date + "T12:00:00Z") {
            return Self.dateFormatter.string(from: d)
        }
        return date
    }
}

// MARK: - Preview

#Preview("里程钻取") {
    StatisticsView().environmentObject(AppState())
}
