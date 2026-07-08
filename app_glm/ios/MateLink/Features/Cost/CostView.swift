import SwiftUI

// MARK: - Cost View (Stitch White-Minimal 1:1)
// TODO: 数据层未接入（不改 CostViewModel/TariffConfig），以下数值为 mock（对齐 Stitch 设计稿）

struct CostView: View {
    @State private var period: String = "本月"

    // MARK: Mock data

    private struct TrendBar: Identifiable {
        let id = UUID()
        let label: String
        let acPct: Double
        let dcPct: Double
        var current: Bool = false
    }

    private struct TouRow: Identifiable {
        let id = UUID()
        let name: String
        let time: String
        let price: String
        let pct: Int
        let color: Color
    }

    private struct HabitRow: Identifiable {
        let id = UUID()
        let title: String
        let sub: String?
        let subColor: Color?
        let value: String
    }

    private let acBlue = Color(hex: "3B82F6")
    private let dcOrange = Color(hex: "F59E0B")
    private let flatYellow = Color(hex: "FBBF24")

    private let trendBars: [TrendBar] = [
        .init(label: "M1", acPct: 0.60, dcPct: 0.30),
        .init(label: "M2", acPct: 0.50, dcPct: 0.20),
        .init(label: "M3", acPct: 0.70, dcPct: 0.15),
        .init(label: "M4", acPct: 0.55, dcPct: 0.25),
        .init(label: "M5", acPct: 0.45, dcPct: 0.30),
        .init(label: "M6", acPct: 0.60, dcPct: 0.35),
        .init(label: "M7", acPct: 0.50, dcPct: 0.28),
        .init(label: "M8", acPct: 0.65, dcPct: 0.20),
        .init(label: "M9", acPct: 0.58, dcPct: 0.22),
        .init(label: "M10", acPct: 0.48, dcPct: 0.30),
        .init(label: "M11", acPct: 0.40, dcPct: 0.40),
        .init(label: "NOW", acPct: 0.55, dcPct: 0.45, current: true)
    ]

    private var touRows: [TouRow] {
        [
            .init(name: "峰段", time: "18-23时", price: "¥1.0", pct: 38, color: StitchColors.error),
            .init(name: "平段", time: "7-18时", price: "¥0.6", pct: 45, color: flatYellow),
            .init(name: "谷段", time: "23-7时", price: "¥0.3", pct: 17, color: StitchColors.statusOnline)
        ]
    }

    private var chargeHabits: [HabitRow] {
        [
            .init(title: "最常充电站", sub: nil, subColor: nil, value: "家充 18次/64%"),
            .init(title: "最常时段", sub: "节省 ¥38", subColor: StitchColors.statusOnline, value: "22:00-06:00 谷段"),
            .init(title: "DC 快充占比", sub: nil, subColor: nil, value: "33% (同比+5%)"),
            .init(title: "平均单次充入", sub: nil, subColor: nil, value: "10.1 kWh")
        ]
    }

    private var driveHabits: [HabitRow] {
        [
            .init(title: "总里程", sub: "日均 34.1 km", subColor: StitchColors.onSurfaceVariant, value: "1,286 km"),
            .init(title: "能耗成本", sub: "¥186.50", subColor: StitchColors.onSurfaceVariant, value: "每公里 ¥0.145"),
            .init(title: "高速占比", sub: "能耗 +22%", subColor: StitchColors.error, value: "28%"),
            .init(title: "急加速次数", sub: "能耗 +8%", subColor: StitchColors.error, value: "12次")
        ]
    }

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                overviewCard
                statsGrid
                acDcSplitCard
                trendCard
                touCard
                habitCard(title: "本月充电习惯", rows: chargeHabits)
                habitCard(title: "本月驾驶习惯", rows: driveHabits)
                savingsCard
                Spacer().frame(height: 16)
            }
            .padding(.horizontal, 24)
            .padding(.top, 24)
        }
        .background(StitchColors.background)
        .navigationTitle("成本分析")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                periodSegmented
            }
        }
    }

    // MARK: - Overview (本月支出)
    private var overviewCard: some View {
        StitchCard {
            HStack(alignment: .top) {
                StitchLabel("本月支出")
                Spacer()
                Text("峰/平/谷")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(StitchColors.onSurface)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .overlay(
                        RoundedRectangle(cornerRadius: 4)
                            .stroke(StitchColors.outline, lineWidth: 1)
                    )
            }
            Spacer().frame(height: 8)
            Text("¥186.50")
                .font(.custom("JetBrainsMono-Medium", size: 48))
                .tracking(-0.96)
                .foregroundColor(StitchColors.onSurface)
            Spacer().frame(height: 8)
            HStack(spacing: 6) {
                Image(systemName: "arrow.down")
                    .font(.system(size: 12, weight: .bold))
                    .foregroundColor(StitchColors.statusOnline)
                Text("vs 上月 ¥210.20 ↓11.2%")
                    .font(StitchFont.dataMd())
                    .foregroundColor(StitchColors.statusOnline)
            }
        }
    }

    // MARK: - Stats Grid (充电 / 能耗 / 均价)
    private var statsGrid: some View {
        HStack(spacing: 0) {
            statCell("充电", "42次")
            vDivider
            statCell("能耗", "284kWh")
            vDivider
            statCell("均价", "¥0.66/kWh")
        }
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(StitchColors.border, lineWidth: 1)
        )
    }

    private func statCell(_ label: String, _ value: String) -> some View {
        VStack(spacing: 4) {
            Text(label)
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
            Text(value)
                .font(StitchFont.dataMd())
                .foregroundColor(StitchColors.onSurface)
        }
        .frame(maxWidth: .infinity)
        .padding(.vertical, 24)
    }

    private var vDivider: some View {
        Rectangle()
            .fill(StitchColors.border)
            .frame(width: 1, height: 72)
    }

    // MARK: - AC/DC Split (充电类型拆分)
    private var acDcSplitCard: some View {
        StitchCard {
            StitchLabel("充电类型拆分")
            Spacer().frame(height: 16)
            // 堆叠横条
            GeometryReader { geo in
                HStack(spacing: 0) {
                    Rectangle().fill(acBlue).frame(width: geo.size.width * 0.53)
                    Rectangle().fill(dcOrange)
                }
            }
            .frame(height: 32)
            .clipShape(RoundedRectangle(cornerRadius: 16))
            Spacer().frame(height: 24)
            HStack(alignment: .top, spacing: 24) {
                splitLegend(dot: acBlue, title: "AC 家充", amount: "¥98.30 (53%)", sub: "28次 | 7.4kW")
                splitLegend(dot: dcOrange, title: "DC 快充", amount: "¥88.20 (47%)", sub: "14次 | 120kW")
            }
        }
    }

    private func splitLegend(dot: Color, title: String, amount: String, sub: String) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 6) {
                Circle().fill(dot).frame(width: 8, height: 8)
                Text(title).font(.system(size: 14, weight: .bold)).foregroundColor(StitchColors.onSurface)
            }
            Spacer().frame(height: 4)
            Text(amount).font(StitchFont.dataMd()).foregroundColor(StitchColors.onSurface)
            Text(sub).font(StitchFont.bodySm()).foregroundColor(StitchColors.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }

    // MARK: - Trend Card (月度成本趋势)
    private var trendCard: some View {
        StitchCard {
            StitchLabel("月度成本趋势 (近12月)")
            Spacer().frame(height: 24)
            ZStack(alignment: .topLeading) {
                // 平均线 AVG ¥175 (约 40% 高度)
                VStack(spacing: 0) {
                    Spacer().frame(height: 76)
                    ZStack(alignment: .topLeading) {
                        Rectangle()
                            .fill(StitchColors.accent)
                            .frame(height: 1)
                        Text("AVG ¥175")
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(StitchColors.accent)
                            .padding(.leading, 8)
                            .offset(y: -10)
                    }
                    Spacer()
                }
                HStack(alignment: .bottom, spacing: 0) {
                    ForEach(trendBars) { bar in
                        trendColumn(bar)
                            .frame(maxWidth: .infinity)
                    }
                }
            }
            .frame(height: 192)
        }
    }

    private func trendColumn(_ bar: TrendBar) -> some View {
        VStack(spacing: 4) {
            Spacer(minLength: 0)
            // 堆叠柱：底部 AC 蓝、顶部 DC 橙
            VStack(spacing: 0) {
                Rectangle().fill(dcOrange).frame(height: 140 * CGFloat(bar.dcPct))
                Rectangle().fill(acBlue).frame(height: 140 * CGFloat(bar.acPct))
            }
            .frame(width: 14, height: bar.current ? 150 : 140, alignment: .bottom)
            .clipShape(RoundedRectangle(cornerRadius: 3))
            .overlay(
                RoundedRectangle(cornerRadius: 3)
                    .stroke(bar.current ? StitchColors.primary : Color.clear, lineWidth: 1)
            )
            Text(bar.label)
                .font(.system(size: 10, weight: bar.current ? .bold : .regular))
                .foregroundColor(StitchColors.onSurfaceVariant)
        }
    }

    // MARK: - TOU Card (分时电价·峰平谷)
    private var touCard: some View {
        StitchCard {
            StitchLabel("分时电价·峰平谷")
            Spacer().frame(height: 16)
            VStack(spacing: 16) {
                ForEach(touRows) { r in touBar(r) }
            }
        }
    }

    private func touBar(_ r: TouRow) -> some View {
        VStack(spacing: 6) {
            HStack(alignment: .bottom) {
                HStack(spacing: 8) {
                    Text(r.name).font(.system(size: 14, weight: .bold)).foregroundColor(StitchColors.onSurface)
                    Text(r.time).font(.system(size: 12)).foregroundColor(StitchColors.onSurfaceVariant)
                }
                Spacer()
                HStack(alignment: .bottom, spacing: 8) {
                    Text(r.price).font(StitchFont.dataMd()).foregroundColor(StitchColors.onSurface)
                    Text("\(r.pct)%").font(.system(size: 12)).foregroundColor(StitchColors.onSurfaceVariant)
                }
            }
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 3).fill(StitchColors.surfaceContainerLow)
                    RoundedRectangle(cornerRadius: 3)
                        .fill(r.color)
                        .frame(width: geo.size.width * CGFloat(r.pct) / 100.0)
                }
            }
            .frame(height: 6)
        }
    }

    // MARK: - Habit Card (充电/驾驶习惯)
    private func habitCard(title: String, rows: [HabitRow]) -> some View {
        StitchCard {
            StitchLabel(title)
            Spacer().frame(height: 8)
            VStack(spacing: 0) {
                ForEach(Array(rows.enumerated()), id: \.element.id) { i, row in
                    HStack {
                        VStack(alignment: .leading, spacing: 2) {
                            Text(row.title).font(StitchFont.bodyLg()).foregroundColor(StitchColors.onSurface)
                            if let sub = row.sub {
                                Text(sub)
                                    .font(.system(size: 12, weight: row.subColor == StitchColors.statusOnline ? .bold : .regular))
                                    .foregroundColor(row.subColor ?? StitchColors.onSurfaceVariant)
                            }
                        }
                        Spacer()
                        Text(row.value).font(StitchFont.dataMd()).foregroundColor(StitchColors.onSurface)
                    }
                    .padding(.vertical, 12)
                    if i < rows.count - 1 {
                        Rectangle().fill(StitchColors.outlineVariant).frame(height: 1)
                    }
                }
            }
        }
    }

    // MARK: - Savings Card (节约建议)
    private var savingsCard: some View {
        let tips = [
            "改用谷段充电可省 ¥38/月",
            "家充占比提升至 70% 可省 ¥25",
            "减少急加速可省 ¥15"
        ]
        return VStack(alignment: .leading, spacing: 0) {
            HStack(spacing: 8) {
                Image(systemName: "lightbulb.fill")
                    .font(.system(size: 18))
                    .foregroundColor(StitchColors.statusOnline)
                Text("节约建议")
                    .font(.system(size: 18, weight: .bold))
                    .foregroundColor(StitchColors.statusOnline)
            }
            Spacer().frame(height: 16)
            VStack(alignment: .leading, spacing: 12) {
                ForEach(tips, id: \.self) { tip in
                    HStack(alignment: .top, spacing: 8) {
                        Circle()
                            .fill(StitchColors.statusOnline)
                            .frame(width: 6, height: 6)
                            .padding(.top, 7)
                        Text(tip).font(StitchFont.bodyLg()).foregroundColor(StitchColors.onSurface)
                        Spacer(minLength: 0)
                    }
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(24)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(StitchColors.statusOnline, lineWidth: 2)
        )
    }
}
