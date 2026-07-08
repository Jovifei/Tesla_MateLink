import SwiftUI

// ── Stitch semantic colors (estimate/actual/deviation) ──
private extension Color {
    static let estimate = Color(hex: "3B82F6")
    static let actual = Color(hex: "059669")
    static let deviation = Color(hex: "F59E0B")
}

// TODO: 数据层未提供以下续航分析字段，暂用 mock 对照 Stitch HTML 还原。
//       接入真实统计后替换。
private struct RangeAnalysis {
    let estimatedKm: Int
    let actualKm: Int
    let deviationKm: Int
    let deviationPct: Double
    let achievementPct: Double
    let rating: String
    let ratingLabel: String
    let betterThanPct: Int
}

private struct RangeFactor: Identifiable {
    let id = UUID()
    let label: String
    let value: String
    let color: Color
}

private struct SeasonBar: Identifiable {
    let id = UUID()
    let label: String
    let value: Int
    let ratio: CGFloat
    let isLow: Bool
}

struct RangePageView: View {
    @EnvironmentObject var state: AppState
    @State private var selectedPeriod = "30天"

    // TODO: mock 数据，等 ViewModel 提供真实续航分析后替换
    private let analysis = RangeAnalysis(
        estimatedKm: 312,
        actualKm: 286,
        deviationKm: -26,
        deviationPct: -8.3,
        achievementPct: 91.7,
        rating: "A-",
        ratingLabel: "优秀级别",
        betterThanPct: 78
    )
    private let factors: [RangeFactor] = [
        RangeFactor(label: "外界温度", value: "-12%", color: .deviation),
        RangeFactor(label: "空调热泵", value: "-8%", color: StitchColors.secondary),
        RangeFactor(label: "高速行驶", value: "-15%", color: StitchColors.primary),
        RangeFactor(label: "急加/减速", value: "-6%", color: .actual)
    ]
    private let seasons: [SeasonBar] = [
        SeasonBar(label: "春季", value: 315, ratio: 0.75, isLow: false),
        SeasonBar(label: "夏季", value: 302, ratio: 0.70, isLow: false),
        SeasonBar(label: "秋季", value: 328, ratio: 0.85, isLow: false),
        SeasonBar(label: "冬季", value: 268, ratio: 0.55, isLow: true)
    ]

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 24) {
                    periodChips
                    comparisonCard
                    ratingCard
                    distributionCard
                    factorsCard
                    trendCard
                    seasonCard
                }
                .padding(24)
            }
            .background(StitchColors.surface)
            .navigationTitle("续航分析")
            .navigationBarTitleDisplayMode(.inline)
        }
    }

    // MARK: - Period chips

    private var periodChips: some View {
        HStack(spacing: 8) {
            ForEach(["7天", "30天", "90天"], id: \.self) { period in
                let selected = period == selectedPeriod
                Text(period)
                    .font(StitchFont.labelCaps())
                    .foregroundColor(selected ? StitchColors.white : StitchColors.onSurface)
                    .padding(.horizontal, 16)
                    .padding(.vertical, 6)
                    .background(selected ? StitchColors.primary : Color.clear)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(StitchColors.border, lineWidth: selected ? 0 : 1)
                    )
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                    .onTapGesture { selectedPeriod = period }
            }
            Spacer()
        }
    }

    // MARK: - 预估 vs 实际偏差卡

    private var comparisonCard: some View {
        StitchCard {
            StitchLabel("预估 VS 实际续航")
            Spacer().frame(height: 24)
            HStack(alignment: .center) {
                HStack(spacing: 24) {
                    RingChart(value: analysis.estimatedKm, ringColor: .estimate,
                              valueColor: StitchColors.onSurface, fillFraction: 0.80)
                    RingChart(value: analysis.actualKm, ringColor: .actual,
                              valueColor: .actual, fillFraction: 0.66)
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 0) {
                    Text("\(analysis.deviationKm) km")
                        .font(StitchFont.dataLg())
                        .foregroundColor(.deviation)
                    Text(String(format: "%.1f%%", analysis.deviationPct))
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(StitchColors.statusError)
                        .padding(.horizontal, 8).padding(.vertical, 2)
                        .background(StitchColors.statusErrorBg)
                        .clipShape(RoundedRectangle(cornerRadius: 4))
                        .padding(.top, 8)
                    Text("本周期实际续航达成率 \(String(format: "%.1f", analysis.achievementPct))%")
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                        .multilineTextAlignment(.trailing)
                        .frame(maxWidth: 120, alignment: .trailing)
                        .padding(.top, 16)
                }
            }
        }
    }

    // MARK: - 综合续航评级 + 同车型相对位置

    private var ratingCard: some View {
        StitchCard {
            StitchLabel("续航效率评级")
            VStack(spacing: 8) {
                Text(analysis.rating)
                    .font(.custom("JetBrainsMono-Medium", size: 64))
                    .foregroundColor(StitchColors.onSurface)
                Text(analysis.ratingLabel)
                    .font(StitchFont.labelCaps())
                    .foregroundColor(.actual)
            }
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            HStack(spacing: 0) {
                Text("优于同车型 ")
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Text("\(analysis.betterThanPct)%")
                    .font(.system(size: 14, weight: .bold))
                    .foregroundColor(StitchColors.primary)
                Text(" 的用户")
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            .frame(maxWidth: .infinity)
        }
    }

    // MARK: - 续航偏差分布（散点）

    private var distributionCard: some View {
        // TODO: mock 散点，接入真实 (预估,实际) 数据点后替换
        let points: [(CGFloat, CGFloat, Color)] = [
            (0.20, 0.85, .actual), (0.35, 0.70, .actual), (0.50, 0.60, .deviation),
            (0.65, 0.45, .actual), (0.80, 0.35, StitchColors.error),
            (0.45, 0.55, .deviation), (0.15, 0.90, .actual)
        ]
        return StitchCard {
            StitchLabel("续航偏差分布")
            Spacer().frame(height: 24)
            GeometryReader { geo in
                let w = geo.size.width
                let h = geo.size.height
                ZStack {
                    Path { p in
                        p.move(to: CGPoint(x: 0, y: 0)); p.addLine(to: CGPoint(x: 0, y: h))
                        p.addLine(to: CGPoint(x: w, y: h))
                    }.stroke(StitchColors.border, lineWidth: 1)
                    Path { p in
                        p.move(to: CGPoint(x: 0, y: h)); p.addLine(to: CGPoint(x: w, y: 0))
                    }.stroke(style: StrokeStyle(lineWidth: 1, dash: [4, 4]))
                        .foregroundColor(StitchColors.border)
                    ForEach(points.indices, id: \.self) { i in
                        Circle()
                            .stroke(points[i].2, lineWidth: 1)
                            .frame(width: 8, height: 8)
                            .position(x: w * points[i].0, y: h * points[i].1)
                    }
                }
            }
            .aspectRatio(16.0 / 9.0, contentMode: .fit)
            Spacer().frame(height: 24)
            HStack(spacing: 16) {
                legendDot(.actual, "<5%")
                legendDot(.deviation, "5-15%")
                legendDot(StitchColors.error, ">15%")
            }
            .frame(maxWidth: .infinity)
        }
    }

    private func legendDot(_ color: Color, _ label: String) -> some View {
        HStack(spacing: 6) {
            Circle().fill(color).frame(width: 8, height: 8)
            Text(label)
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(StitchColors.onSurface)
        }
    }

    // MARK: - 续航影响因素列表

    private var factorsCard: some View {
        StitchCard {
            StitchLabel("续航影响因素")
            Spacer().frame(height: 24)
            LazyVGrid(columns: Array(repeating: GridItem(.flexible(), spacing: 24), count: 2), spacing: 24) {
                ForEach(factors) { factor in
                    VStack(spacing: 8) {
                        Text(factor.label)
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(StitchColors.onSurface)
                        Text(factor.value)
                            .font(StitchFont.dataMd())
                            .foregroundColor(factor.color)
                    }
                    .frame(maxWidth: .infinity)
                    .padding(16)
                    .overlay(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(StitchColors.border, lineWidth: 1)
                    )
                }
            }
        }
    }

    // MARK: - 30天续航衰减趋势

    private var trendCard: some View {
        // TODO: mock 趋势曲线，接入真实衰减序列后替换（理论值 vs 实际值）
        let theory: [CGFloat] = [0.20, 0.22, 0.21, 0.25, 0.23, 0.24, 0.26, 0.24, 0.27, 0.25, 0.28]
        let real: [CGFloat] = [0.35, 0.38, 0.34, 0.45, 0.40, 0.42, 0.48, 0.45, 0.55, 0.50, 0.58]
        return StitchCard {
            HStack(alignment: .bottom) {
                VStack(alignment: .leading, spacing: 4) {
                    StitchLabel("30天续航衰减趋势")
                    Text("满电后续航稳定性分析")
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
                Spacer()
                HStack(spacing: 16) {
                    trendLegend(.estimate, "理论值")
                    trendLegend(.actual, "实际值")
                }
            }
            Spacer().frame(height: 24)
            GeometryReader { geo in
                let w = geo.size.width
                let h = geo.size.height
                ZStack {
                    linePath(theory, w: w, h: h).stroke(Color.estimate, lineWidth: 1.5)
                    linePath(real, w: w, h: h).stroke(Color.actual, lineWidth: 1.5)
                }
            }
            .frame(height: 192)
        }
    }

    private func linePath(_ data: [CGFloat], w: CGFloat, h: CGFloat) -> Path {
        Path { p in
            for (i, v) in data.enumerated() {
                let x = w * CGFloat(i) / CGFloat(data.count - 1)
                let y = h * v
                if i == 0 { p.move(to: CGPoint(x: x, y: y)) }
                else { p.addLine(to: CGPoint(x: x, y: y)) }
            }
        }
    }

    private func trendLegend(_ color: Color, _ label: String) -> some View {
        HStack(spacing: 8) {
            Rectangle().fill(color).frame(width: 12, height: 2)
            Text(label)
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(StitchColors.onSurface)
        }
    }

    // MARK: - 季节续航对比

    private var seasonCard: some View {
        StitchCard {
            StitchLabel("季节续航对比 (km)")
            Spacer().frame(height: 24)
            HStack(alignment: .bottom, spacing: 16) {
                ForEach(seasons) { season in
                    VStack(spacing: 16) {
                        Text("\(season.value)")
                            .font(StitchFont.dataMd())
                            .foregroundColor(season.isLow ? StitchColors.error : StitchColors.onSurface)
                        GeometryReader { geo in
                            RoundedRectangle(cornerRadius: 2)
                                .fill(season.isLow ? StitchColors.error : StitchColors.surfaceContainerHigh)
                                .frame(height: geo.size.height * season.ratio)
                                .frame(maxHeight: .infinity, alignment: .bottom)
                        }
                        Text(season.label)
                            .font(.system(size: 10, weight: .bold))
                            .foregroundColor(season.isLow ? StitchColors.error : StitchColors.onSurface)
                    }
                    .frame(maxWidth: .infinity)
                }
            }
            .frame(height: 200)
        }
    }
}

// MARK: - 双环续航对比图（预估/实际）

private struct RingChart: View {
    let value: Int
    let ringColor: Color
    let valueColor: Color
    let fillFraction: CGFloat

    var body: some View {
        ZStack {
            Circle()
                .stroke(StitchColors.surfaceContainerHigh, lineWidth: 8)
            Circle()
                .trim(from: 0, to: fillFraction)
                .stroke(ringColor, style: StrokeStyle(lineWidth: 8, lineCap: .butt))
                .rotationEffect(.degrees(-90))
            VStack(spacing: 0) {
                Text("\(value)")
                    .font(StitchFont.dataMd())
                    .foregroundColor(valueColor)
                Text("km")
                    .font(.system(size: 10))
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
        }
        .frame(width: 112, height: 112)
    }
}
