import SwiftUI

// MARK: - Efficiency View (Stitch White-Minimal 1:1)
// TODO: 数据层未接入，以下数值为 mock（对齐 Stitch 设计稿）

struct EfficiencyView: View {
    @State private var range: String = "30天"

    // Trend mock data
    private let trendData: [Double] = [165, 158, 160, 150, 148, 155, 152]
    private let trendLabels: [String] = ["1", "5", "10", "15", "20", "25", "30"]
    private let fleetAvg: Double = 160

    var body: some View {
        ScrollView {
            VStack(spacing: 24) {
                metricsBento
                trendCard
                breakdownCard
                compareCard
                adviceCard
                Spacer().frame(height: 16)
            }
            .padding(.horizontal, 24)
            .padding(.top, 24)
        }
        .background(StitchColors.background)
        .navigationTitle("能耗分析")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button(action: {}) {
                    Image(systemName: "gearshape")
                        .foregroundColor(StitchColors.onSurface)
                }
            }
        }
    }

    // MARK: - Metrics Bento (平均能耗 + 能效评级)
    private var metricsBento: some View {
        HStack(alignment: .top, spacing: 24) {
            // 平均能耗
            StitchCard {
                sectionHeader(icon: "bolt", "平均能耗")
                Spacer().frame(height: 16)
                HStack(alignment: .bottom, spacing: 8) {
                    Text("154")
                        .font(.custom("JetBrainsMono-Medium", size: 48))
                        .foregroundColor(StitchColors.primary)
                    Text("Wh/km")
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                        .padding(.bottom, 4)
                }
                Spacer().frame(height: 16)
                HStack(spacing: 4) {
                    Image(systemName: "arrow.trending.down")
                        .font(.system(size: 12, weight: .bold))
                        .foregroundColor(StitchColors.statusOnline)
                    Text("-5.2%")
                        .font(StitchFont.dataMd())
                        .foregroundColor(StitchColors.statusOnline)
                    Text("vs 上月")
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
            }
            // 能效评级
            StitchCard {
                sectionHeader(icon: "star", "能效评级")
                Spacer().frame(height: 16)
                Text("A+")
                    .font(.system(size: 48, weight: .bold))
                    .foregroundColor(StitchColors.accent)
                Spacer().frame(height: 16)
                HStack(spacing: 0) {
                    Text("优于 ")
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                    Text("92%")
                        .font(StitchFont.dataMd())
                        .foregroundColor(StitchColors.primary)
                    Text(" 的车主")
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
            }
        }
    }

    // MARK: - Trend Card
    private var trendCard: some View {
        StitchCard {
            HStack {
                sectionHeader(icon: "chart.line.uptrend.xyaxis", "近30天能耗趋势")
                Spacer()
                HStack(spacing: 8) {
                    rangeChip("7天", selected: range == "7天")
                    rangeChip("30天", selected: range == "30天")
                }
            }
            Spacer().frame(height: 24)
            TrendChart(data: trendData, labels: trendLabels, fleetAvg: fleetAvg, yMin: 130, yMax: 180)
                .frame(height: 256)
        }
    }

    // MARK: - Breakdown Card (能耗分布)
    private var breakdownCard: some View {
        StitchCard {
            sectionHeader(icon: "chart.bar", "能耗分布")
            Spacer().frame(height: 24)
            VStack(spacing: 16) {
                breakdownBar("行驶", pct: 75, color: StitchColors.primary)
                breakdownBar("空调", pct: 15, color: StitchColors.outline)
                breakdownBar("待机", pct: 7, color: StitchColors.outlineVariant)
                breakdownBar("其他", pct: 3, color: Color(hex: "5F5E5E"))
            }
        }
    }

    // MARK: - Compare Card (同车型能耗对比)
    private var compareCard: some View {
        StitchCard {
            sectionHeader(icon: "arrow.left.arrow.right", "同车型能耗对比")
            Spacer().frame(height: 24)
            VStack(spacing: 16) {
                compareBar("你的", value: "154", fraction: 0.65, color: StitchColors.primary, valueColor: StitchColors.onSurface)
                compareBar("平均", value: "168", fraction: 0.80, color: StitchColors.outlineVariant, valueColor: StitchColors.onSurface)
                compareBar("最佳", value: "138", fraction: 0.45, color: StitchColors.statusOnline, valueColor: StitchColors.statusOnline)
            }
        }
    }

    // MARK: - Advice Card (优化建议)
    private var adviceCard: some View {
        StitchCard {
            sectionHeader(icon: "lightbulb", "优化建议")
            Spacer().frame(height: 24)
            let items: [(icon: String, title: String, desc: String)] = [
                ("speedometer", "减少高速超速", "控制车速在 110km/h 内，可节省约 10% 能耗。"),
                ("snowflake", "优化空调使用", "建议设置自动模式 22°C，避免频繁手动大风量。"),
                ("gauge", "保持胎压正常", "当前左前胎压偏低，补充至 2.9 bar 可减少滚阻。")
            ]
            VStack(spacing: 0) {
                ForEach(Array(items.enumerated()), id: \.offset) { i, item in
                    HStack(alignment: .top, spacing: 12) {
                        Image(systemName: item.icon)
                            .font(.system(size: 16))
                            .foregroundColor(StitchColors.primary)
                            .frame(width: 20)
                            .padding(.top, 2)
                        VStack(alignment: .leading, spacing: 4) {
                            Text(item.title)
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(StitchColors.onSurface)
                            Text(item.desc)
                                .font(StitchFont.bodySm())
                                .foregroundColor(StitchColors.onSurfaceVariant)
                        }
                        Spacer(minLength: 0)
                    }
                    .padding(.vertical, i == 0 ? 0 : 12)
                    if i < items.count - 1 {
                        Divider().background(StitchColors.surfaceContainerHigh)
                    }
                }
            }
        }
    }

    // MARK: - Components

    private func sectionHeader(icon: String, _ title: String) -> some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 14))
                .foregroundColor(StitchColors.onSurfaceVariant)
            Text(title)
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
        }
    }

    private func rangeChip(_ label: String, selected: Bool) -> some View {
        Text(label)
            .font(StitchFont.labelCaps())
            .foregroundColor(selected ? StitchColors.white : StitchColors.onSurfaceVariant)
            .padding(.horizontal, 12)
            .padding(.vertical, 4)
            .background(selected ? StitchColors.primary : Color.clear)
            .clipShape(RoundedRectangle(cornerRadius: 4))
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .stroke(selected ? StitchColors.primary : StitchColors.surfaceContainerHigh, lineWidth: 1)
            )
            .onTapGesture { range = label }
    }

    private func breakdownBar(_ label: String, pct: Int, color: Color) -> some View {
        VStack(spacing: 4) {
            HStack {
                Text(label).font(StitchFont.bodySm()).foregroundColor(StitchColors.onSurface)
                Spacer()
                Text("\(pct)%").font(StitchFont.dataMd()).foregroundColor(StitchColors.onSurface)
            }
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(StitchColors.surfaceContainerHigh)
                    RoundedRectangle(cornerRadius: 4)
                        .fill(color)
                        .frame(width: geo.size.width * CGFloat(pct) / 100.0)
                }
            }
            .frame(height: 8)
        }
    }

    private func compareBar(_ label: String, value: String, fraction: Double, color: Color, valueColor: Color) -> some View {
        VStack(spacing: 4) {
            HStack(alignment: .bottom) {
                Text(label).font(StitchFont.bodySm()).foregroundColor(StitchColors.onSurface)
                Spacer()
                HStack(alignment: .bottom, spacing: 4) {
                    Text(value).font(StitchFont.dataMd()).foregroundColor(valueColor)
                    Text("Wh/km").font(.system(size: 11)).foregroundColor(valueColor).padding(.bottom, 1)
                }
            }
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 4)
                        .fill(StitchColors.surfaceContainerHigh)
                    RoundedRectangle(cornerRadius: 4)
                        .fill(color)
                        .frame(width: geo.size.width * CGFloat(fraction))
                }
            }
            .frame(height: 8)
        }
    }
}

// MARK: - Trend Chart (金色主线 + 车队平均虚线，纯 SwiftUI Path)
private struct TrendChart: View {
    let data: [Double]
    let labels: [String]
    let fleetAvg: Double
    let yMin: Double
    let yMax: Double

    private let padL: CGFloat = 34
    private let padB: CGFloat = 20
    private let padT: CGFloat = 8
    private let padR: CGFloat = 8

    var body: some View {
        GeometryReader { geo in
            let chartW = geo.size.width - padL - padR
            let chartH = geo.size.height - padT - padB

            func yToPx(_ v: Double) -> CGFloat {
                padT + chartH * CGFloat(1 - (v - yMin) / (yMax - yMin))
            }
            func xToPx(_ i: Int) -> CGFloat {
                padL + chartW * CGFloat(Double(i) / Double(max(data.count - 1, 1)))
            }

            ZStack {
                // Y 轴网格 + 标签
                ForEach(Array(stride(from: yMin, through: yMax, by: 10)), id: \.self) { yv in
                    let y = yToPx(yv)
                    Path { p in
                        p.move(to: CGPoint(x: padL, y: y))
                        p.addLine(to: CGPoint(x: geo.size.width - padR, y: y))
                    }
                    .stroke(StitchColors.surfaceContainer, lineWidth: 1)
                    Text("\(Int(yv))")
                        .font(.custom("JetBrainsMono-Medium", size: 10))
                        .foregroundColor(StitchColors.outline)
                        .position(x: padL - 16, y: y)
                }

                // X 轴标签
                ForEach(Array(labels.enumerated()), id: \.offset) { i, label in
                    Text(label)
                        .font(.custom("JetBrainsMono-Medium", size: 10))
                        .foregroundColor(StitchColors.outline)
                        .position(x: xToPx(i), y: geo.size.height - padB / 2 + 2)
                }

                // 车队平均虚线
                Path { p in
                    let y = yToPx(fleetAvg)
                    p.move(to: CGPoint(x: padL, y: y))
                    p.addLine(to: CGPoint(x: geo.size.width - padR, y: y))
                }
                .stroke(StitchColors.outlineVariant, style: StrokeStyle(lineWidth: 1, dash: [5, 5]))

                // 主曲线 (金色)
                Path { p in
                    for (i, v) in data.enumerated() {
                        let pt = CGPoint(x: xToPx(i), y: yToPx(v))
                        if i == 0 { p.move(to: pt) } else { p.addLine(to: pt) }
                    }
                }
                .stroke(StitchColors.accent, lineWidth: 2)

                // 数据点 (白心金环)
                ForEach(Array(data.enumerated()), id: \.offset) { i, v in
                    Circle()
                        .fill(StitchColors.accent)
                        .frame(width: 6, height: 6)
                        .overlay(Circle().fill(StitchColors.white).frame(width: 3, height: 3))
                        .position(x: xToPx(i), y: yToPx(v))
                }
            }
        }
    }
}
