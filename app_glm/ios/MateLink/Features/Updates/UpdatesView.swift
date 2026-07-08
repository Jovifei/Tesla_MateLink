import SwiftUI

struct UpdatesView: View {
    @EnvironmentObject var state: AppState
    @State private var updates: [UpdateItem] = []
    @State private var loading = true
    @State private var installing = false

    var body: some View {
        NavigationStack {
            Group {
                if loading {
                    ProgressView("加载中...").padding()
                } else if updates.isEmpty {
                    ContentUnavailableView("无更新", systemImage: "desktopcomputer", description: Text("暂无固件更新记录"))
                } else {
                    ScrollView {
                        VStack(spacing: 24) {
                            currentVersionCard
                            statsGrid
                            updateAvailableCard
                            historyTimeline
                            chartsSection
                        }
                        .padding(.horizontal, 24)
                        .padding(.vertical, 24)
                    }
                }
            }
            .navigationTitle("固件版本")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    Image(systemName: "arrow.clockwise")
                        .foregroundColor(StitchColors.onSurface)
                }
            }
            .refreshable { await load() }
            .task { await load() }
        }
    }

    private var current: UpdateItem? { updates.first }

    private var totalUpdates: Int { updates.count }
    private var firstVersion: String { updates.last?.version ?? "—" }
    private var totalMinutes: Int {
        updates.reduce(0) { sum, item in
            sum + Int(item.endDate.isoDate.timeIntervalSince(item.startDate.isoDate) / 60)
        }
    }

    // ── Current Version Card ──────────────────────────────────
    private var currentVersionCard: some View {
        ZStack(alignment: .topTrailing) {
            VStack(alignment: .leading, spacing: 8) {
                Text("当前版本")
                    .font(StitchFont.bodySm())
                    .fontWeight(.bold)
                    .foregroundColor(StitchColors.onSurfaceVariant)
                if let cur = current {
                    Text(cur.version)
                        .font(.custom("JetBrainsMono-Bold", size: 42))
                        .foregroundColor(StitchColors.onSurface)
                    HStack(spacing: 8) {
                        Text("已安装于")
                            .font(StitchFont.bodySm())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                        Text(String(cur.startDate.prefix(10)))
                            .font(StitchFont.dataMd())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                        Circle().fill(StitchColors.border).frame(width: 4, height: 4)
                        Text("Model 3 2022")
                            .font(StitchFont.bodySm())
                            .fontWeight(.medium)
                            .foregroundColor(StitchColors.onSurfaceVariant)
                    }
                }
            }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(24)
            .background(StitchColors.white)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(StitchColors.border, lineWidth: 1)
            )
            .padding(.top, 24)

            Text("最新")
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.statusOnline)
                .padding(.horizontal, 12)
                .padding(.vertical, 4)
                .background(StitchColors.statusOnline.opacity(0.1))
                .cornerRadius(2)
        }
    }

    // ── Stats Grid ────────────────────────────────────────────
    private var statsGrid: some View {
        HStack(spacing: 8) {
            statCard(label: "总更新", value: "\(totalUpdates)", suffix: "次")
            statCard(label: "首次安装", value: firstVersion, small: true)
            statCard(label: "累计时长", value: "\(totalMinutes / 60)", suffix: "h")
        }
    }

    private func statCard(label: String, value: String, suffix: String = "", small: Bool = false) -> some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(label.uppercased())
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(StitchColors.onSurfaceVariant)
            HStack(alignment: .firstTextBaseline, spacing: 2) {
                Text(value)
                    .font(.custom("JetBrainsMono-Bold", size: small ? 14 : 20))
                    .foregroundColor(StitchColors.onSurface)
                if !suffix.isEmpty {
                    Text(suffix)
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurface)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(StitchColors.border, lineWidth: 1)
        )
    }

    // ── Update Available Card ─────────────────────────────────
    private var updateAvailableCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 8) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.system(size: 20))
                            .foregroundColor(StitchColors.statusCharging)
                        Text("新版本可用")
                            .font(StitchFont.bodyLg())
                            .fontWeight(.bold)
                            .foregroundColor(StitchColors.statusCharging)
                    }
                    Text("2025.24.8")
                        .font(.custom("JetBrainsMono-Bold", size: 24))
                        .foregroundColor(StitchColors.onSurface)
                    Text("大小: 1.2GB")
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
                Spacer()
                Text("发布说明 ▼")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurface)
            }
            VStack(spacing: 12) {
                Button {
                    installing = true
                    DispatchQueue.main.asyncAfter(deadline: .now() + 2) { installing = false }
                } label: {
                    Text(installing ? "准备中..." : "立即安装")
                        .font(StitchFont.bodyLg())
                        .fontWeight(.bold)
                        .foregroundColor(StitchColors.white)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(StitchColors.primary)
                        .cornerRadius(8)
                }
                Button {} label: {
                    Text("预约安装")
                        .font(StitchFont.bodyLg())
                        .fontWeight(.bold)
                        .foregroundColor(StitchColors.onSurface)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 14)
                        .background(StitchColors.white)
                        .cornerRadius(8)
                        .overlay(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(StitchColors.border, lineWidth: 1)
                        )
                }
            }
        }
        .padding(24)
        .background(StitchColors.statusCharging.opacity(0.05))
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(StitchColors.statusCharging, lineWidth: 1)
        )
    }

    // ── Update History Timeline ───────────────────────────────
    private var historyTimeline: some View {
        VStack(alignment: .leading, spacing: 24) {
            Text("更新历史")
                .font(StitchFont.bodyLg())
                .fontWeight(.bold)
                .foregroundColor(StitchColors.onSurface)
            VStack(spacing: 32) {
                ForEach(Array(updates.enumerated()), id: \.element.id) { index, item in
                    TimelineRow(item: item, isLast: index == updates.count - 1)
                }
            }
        }
        .padding(24)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(StitchColors.border, lineWidth: 1)
        )
    }

    // ── Charts Section ────────────────────────────────────────
    private var chartsSection: some View {
        VStack(spacing: 16) {
            // Feature Distribution Donut
            VStack(alignment: .leading, spacing: 24) {
                Text("年度功能更新")
                    .font(StitchFont.bodyLg())
                    .fontWeight(.bold)
                    .foregroundColor(StitchColors.onSurface)
                HStack(spacing: 32) {
                    DonutChartView()
                    VStack(spacing: 8) {
                        legendItem(color: StitchColors.onSurface, label: "自动驾驶", value: "32%")
                        legendItem(color: StitchColors.statusOnline, label: "安全", value: "25%")
                        legendItem(color: StitchColors.accent, label: "媒体", value: "18%")
                        legendItem(color: StitchColors.statusCharging, label: "导航", value: "15%")
                    }
                }
            }
            .padding(24)
            .background(StitchColors.white)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(StitchColors.border, lineWidth: 1)
            )

            // Installation Time Distribution
            VStack(alignment: .leading, spacing: 24) {
                Text("安装时段分布")
                    .font(StitchFont.bodyLg())
                    .fontWeight(.bold)
                    .foregroundColor(StitchColors.onSurface)
                VStack(spacing: 24) {
                    distributionBar(label: "凌晨", sublabel: "0-4时", percent: 78, color: StitchColors.primary)
                    distributionBar(label: "白天", sublabel: "20-22时", percent: 15, color: StitchColors.accent)
                    distributionBar(label: "其他", sublabel: "", percent: 7, color: StitchColors.border)
                }
            }
            .padding(24)
            .background(StitchColors.white)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(StitchColors.border, lineWidth: 1)
            )
        }
    }

    private func legendItem(color: Color, label: String, value: String) -> some View {
        HStack {
            HStack(spacing: 8) {
                Circle().fill(color).frame(width: 8, height: 8)
                Text(label).font(StitchFont.bodySm()).foregroundColor(StitchColors.onSurface)
            }
            Spacer()
            Text(value).font(StitchFont.dataMd()).foregroundColor(StitchColors.onSurface)
        }
    }

    private func distributionBar(label: String, sublabel: String, percent: Int, color: Color) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                HStack(alignment: .bottom, spacing: 0) {
                    Text(label).font(StitchFont.bodySm()).foregroundColor(StitchColors.onSurface)
                    if !sublabel.isEmpty {
                        Text(" \(sublabel)")
                            .font(StitchFont.dataMd())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                    }
                }
                Spacer()
                Text("\(percent)%")
                    .font(StitchFont.dataMd())
                    .fontWeight(.bold)
                    .foregroundColor(StitchColors.onSurface)
            }
            GeometryReader { geo in
                ZStack(alignment: .leading) {
                    Capsule().fill(StitchColors.border).frame(height: 8)
                    Capsule().fill(color).frame(width: geo.size.width * CGFloat(percent) / 100, height: 8)
                }
            }
            .frame(height: 8)
        }
    }

    func load() async {
        loading = true
        updates = state.isMockMode ? await state.mock.getUpdates(state.currentCarId) : []
        loading = false
    }
}

// MARK: - Timeline Row

private struct TimelineRow: View {
    let item: UpdateItem
    let isLast: Bool

    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            ZStack {
                if !isLast {
                    Rectangle()
                        .fill(StitchColors.border)
                        .frame(width: 1)
                        .offset(y: 28)
                }
                Circle()
                    .stroke(StitchColors.onSurface, lineWidth: 1)
                    .background(Circle().fill(StitchColors.white))
                    .frame(width: 28, height: 28)
                    .overlay(
                        Image(systemName: "checkmark.circle.fill")
                            .font(.system(size: 16))
                            .foregroundColor(StitchColors.statusOnline)
                    )
            }
            .frame(width: 28)

            VStack(alignment: .leading, spacing: 4) {
                HStack {
                    Text(item.version)
                        .font(.custom("JetBrainsMono-Bold", size: 16))
                        .foregroundColor(StitchColors.onSurface)
                    Spacer()
                    Text("SUCCESS")
                        .font(StitchFont.dataMd())
                        .fontWeight(.bold)
                        .foregroundColor(StitchColors.statusOnline)
                }
                Text("\(String(item.startDate.prefix(10))) • 1.0GB • \(durationMinutes(item))min")
                    .font(.custom("JetBrainsMono-Medium", size: 12))
                    .foregroundColor(StitchColors.onSurfaceVariant)
                    .textCase(.uppercase)
            }
        }
    }

    private func durationMinutes(_ item: UpdateItem) -> Int {
        let d = item.endDate.isoDate.timeIntervalSince(item.startDate.isoDate)
        return Int(d / 60)
    }
}

// MARK: - Donut Chart

private struct DonutChartView: View {
    var body: some View {
        ZStack {
            Canvas { context in
                let center = CGPoint(x: 64, y: 64)
                let radius: CGFloat = 56
                let segments: [(Double, Color)] = [
                    (0.32, StitchColors.onSurface),
                    (0.25, StitchColors.statusOnline),
                    (0.18, StitchColors.accent),
                    (0.15, StitchColors.statusCharging),
                    (0.10, StitchColors.border)
                ]
                var startAngle = -Double.pi / 2
                for (fraction, color) in segments {
                    let sweep = fraction * 2 * Double.pi
                    let path = Path { p in
                        p.addArc(center: center, radius: radius, startAngle: startAngle, endAngle: startAngle + sweep, clockwise: false)
                    }
                    context.stroke(path, with: .color(color), lineWidth: 16)
                    startAngle += sweep
                }
            }
            .frame(width: 128, height: 128)
            Text("100%")
                .font(.custom("JetBrainsMono-Bold", size: 20))
                .foregroundColor(StitchColors.onSurface)
        }
    }
}

private extension String {
    var isoDate: Date {
        ISO8601Parser.parse(self) ?? .distantPast
    }
}
