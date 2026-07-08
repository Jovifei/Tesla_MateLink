import SwiftUI

// MARK: - Vampire (待机耗电详情) — Stitch White-Minimal 1:1

struct VampireView: View {
    @EnvironmentObject var state: AppState
    @State private var smartStandby = true

    // Mock data (matches Stitch design spec; data layer unchanged)
    // TODO: 接入待机摘要真实数据（standby summary / vampire drain 计算结果）
    private let sources: [DrainSource] = [
        DrainSource(icon: "videocam", title: "哨兵模式", detail: "12 events", value: "-0.45%"),
        DrainSource(icon: "snowflake", title: "温度预调节", detail: "45 mins", value: "-0.22%"),
        DrainSource(icon: "iphone", title: "应用唤醒", detail: "8 API calls", value: "-0.13%")
    ]
    private let advice: [AdviceItem] = [
        AdviceItem(icon: "tree.fill", title: "尽量停在阴凉处", desc: "降低电池过热唤醒频率"),
        AdviceItem(icon: "puzzlepiece.extension", title: "减少第三方插件", desc: "防止非必要 API 轮询唤醒")
    ]
    // 24h 电量曲线（模拟）：SoC 百分比随时间下降
    private let socCurve: [Double] = [80, 78, 74, 70, 66, 62, 58, 55, 52, 50, 48, 46]

    var body: some View {
        ScrollView {
            VStack(spacing: 32) {
                statsGrid
                trendCard
                breakdownSection
                adviceSection
                smartStandbyCard
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 8)
        }
        .background(StitchColors.background)
        .navigationTitle("待机耗电详情")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button { } label: {
                    Image(systemName: "gearshape")
                        .foregroundColor(StitchColors.onSurface)
                }
            }
        }
    }

    // MARK: - Stats Grid (2x2)

    private var statsGrid: some View {
        VStack(spacing: 16) {
            HStack(spacing: 16) {
                statCard("总耗电", "-0.8", "%")
                statCard("待机时长", "12.5", "h")
            }
            HStack(spacing: 16) {
                statCard("效率排名", "85", "%")
                statCard("平均功率", "42", "W")
            }
        }
    }

    private func statCard(_ label: String, _ value: String, _ unit: String) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label)
                .font(.system(size: 11, weight: .bold))
                .tracking(1)
                .foregroundColor(StitchColors.onSurfaceVariant)
            HStack(alignment: .bottom, spacing: 4) {
                Spacer()
                Text(value)
                    .font(StitchFont.dataLg())
                    .foregroundColor(StitchColors.onSurface)
                Text(unit)
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                    .padding(.bottom, 2)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Trend Card (24h line chart)

    private var trendCard: some View {
        StitchCard {
            Text("耗电趋势 (24h)")
                .font(.system(size: 14, weight: .medium))
                .foregroundColor(StitchColors.onSurface)
            Spacer().frame(height: 16)
            ZStack(alignment: .topLeading) {
                GeometryReader { geo in
                    let w = geo.size.width
                    let h = geo.size.height
                    // 横向虚线网格
                    Path { p in
                        for i in 0...3 {
                            let y = h * CGFloat(i) / 3
                            p.move(to: CGPoint(x: 0, y: y))
                            p.addLine(to: CGPoint(x: w, y: y))
                        }
                    }
                    .stroke(StitchColors.surfaceContainerHigh.opacity(0.6),
                            style: StrokeStyle(lineWidth: 1, dash: [6, 4]))
                    // 左轴 + 底轴
                    Path { p in
                        p.move(to: CGPoint(x: 0, y: 0))
                        p.addLine(to: CGPoint(x: 0, y: h))
                        p.addLine(to: CGPoint(x: w, y: h))
                    }
                    .stroke(StitchColors.surfaceContainerHigh, lineWidth: 1)
                    // 金色曲线
                    curvePath(width: w, height: h)
                        .stroke(StitchColors.accent,
                                style: StrokeStyle(lineWidth: 1.5, lineCap: .round, lineJoin: .round))
                }
                .frame(height: 160)
                // 起点数值标签
                Text("80%")
                    .font(.system(size: 10, design: .monospaced))
                    .foregroundColor(StitchColors.onSurface)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .background(StitchColors.background)
                    .overlay(RoundedRectangle(cornerRadius: 4).stroke(StitchColors.border, lineWidth: 1))
                    .padding(.leading, 8)
                    .padding(.top, 12)
            }
            .frame(height: 160)
            Spacer().frame(height: 8)
            HStack {
                ForEach(["08:30", "14:30", "20:30"], id: \.self) { t in
                    Text(t)
                        .font(.system(size: 10, design: .monospaced))
                        .foregroundColor(StitchColors.onSurfaceVariant)
                    if t != "20:30" { Spacer() }
                }
            }
            Spacer().frame(height: 16)
            Text("模拟数据 — 基于待机摘要")
                .font(.system(size: 10, weight: .bold))
                .tracking(0.6)
                .foregroundColor(StitchColors.onSurfaceVariant)
                .frame(maxWidth: .infinity, alignment: .center)
        }
    }

    private func curvePath(width w: CGFloat, height h: CGFloat) -> Path {
        var path = Path()
        guard let maxV = socCurve.max(), let minV = socCurve.min() else { return path }
        let range = max(maxV - minV, 1)
        for (i, v) in socCurve.enumerated() {
            let x = w * CGFloat(i) / CGFloat(socCurve.count - 1)
            let y = h * (1 - CGFloat((v - minV) / range)) * 0.85 + h * 0.075
            if i == 0 { path.move(to: CGPoint(x: x, y: y)) }
            else { path.addLine(to: CGPoint(x: x, y: y)) }
        }
        return path
    }

    // MARK: - Breakdown Section

    private var breakdownSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("耗电分布")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(StitchColors.primary)
            VStack(spacing: 12) {
                ForEach(sources) { breakdownRow($0) }
            }
        }
    }

    private func breakdownRow(_ s: DrainSource) -> some View {
        HStack {
            HStack(spacing: 12) {
                ZStack {
                    Circle()
                        .fill(StitchColors.surfaceContainerLow)
                        .overlay(Circle().stroke(StitchColors.border, lineWidth: 1))
                        .frame(width: 40, height: 40)
                    Image(systemName: s.icon)
                        .font(.system(size: 20))
                        .foregroundColor(StitchColors.onSurface)
                }
                VStack(alignment: .leading, spacing: 0) {
                    Text(s.title)
                        .font(.system(size: 16, weight: .medium))
                        .foregroundColor(StitchColors.onSurface)
                    Text(s.detail)
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
            }
            Spacer()
            Text(s.value)
                .font(StitchFont.dataLg())
                .foregroundColor(StitchColors.onSurface)
        }
        .padding(16)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Advice Section (2 bento cards)

    private var adviceSection: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("优化建议")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(StitchColors.primary)
            HStack(spacing: 16) {
                ForEach(advice) { adviceCard($0) }
            }
        }
    }

    private func adviceCard(_ a: AdviceItem) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Image(systemName: a.icon)
                .font(.system(size: 24))
                .foregroundColor(StitchColors.onSurfaceVariant)
            Spacer()
            Text(a.title)
                .font(StitchFont.bodyLg())
                .foregroundColor(StitchColors.primary)
            Spacer().frame(height: 4)
            Text(a.desc)
                .font(StitchFont.bodySm())
                .foregroundColor(StitchColors.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity, minHeight: 128, alignment: .leading)
        .padding(24)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Smart Standby Toggle

    private var smartStandbyCard: some View {
        HStack {
            VStack(alignment: .leading, spacing: 0) {
                Text("智能待机模式")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(StitchColors.primary)
                Text("夜间自动关闭哨兵以节省电量")
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            Spacer()
            Toggle("", isOn: $smartStandby)
                .labelsHidden()
                .tint(StitchColors.primary)
        }
        .padding(16)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }
}

// MARK: - Models

private struct DrainSource: Identifiable {
    let id = UUID()
    let icon: String
    let title: String
    let detail: String
    let value: String
}

private struct AdviceItem: Identifiable {
    let id = UUID()
    let icon: String
    let title: String
    let desc: String
}
