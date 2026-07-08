import SwiftUI

// MARK: - Charge Detail View (Stitch White-Minimal 1:1)

struct ChargeDetailView: View {
    let charge: Charge

    @State private var selectedTab: CurveTab = .power

    private let sampleCount = 12

    enum CurveTab: String, CaseIterable, Identifiable {
        case power, soc, voltage, temp
        var id: String { rawValue }
        var label: String {
            switch self {
            case .power: return "功率"
            case .soc: return "电量"
            case .voltage: return "电压"
            case .temp: return "温度"
            }
        }
    }

    // MARK: - Derived data

    private var isDC: Bool { charge.chargeType == "DC" }

    private var durationMinutes: Int {
        guard let end = charge.endDate,
              let s = ISO8601Parser.parse(charge.startDate),
              let e = ISO8601Parser.parse(end) else { return 135 }
        return max(1, Int(e.timeIntervalSince(s) / 60))
    }

    private var startSoc: Int { charge.startBatteryLevel }
    private var endSoc: Int { charge.endBatteryLevel ?? min(100, startSoc + 35) }

    private var avgPowerKw: Double {
        let hours = Double(durationMinutes) / 60.0
        guard hours > 0 else { return 0 }
        return charge.chargeEnergyAdded / hours
    }

    private var peakPowerKw: Double { isDC ? 120 : 7.4 }

    // MARK: - Body

    var body: some View {
        ScrollView {
            VStack(spacing: 32) {
                overviewCard
                batteryChangeCard
                chargingCurveCard
                statsGrid
                chargingStagesCard
                exportButton
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 8)
        }
        .background(StitchColors.background)
        .navigationTitle("充电详情")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button { } label: {
                    Image(systemName: "square.and.arrow.up")
                        .foregroundColor(StitchColors.onSurface)
                }
            }
        }
    }

    // MARK: - Overview Card

    private var overviewCard: some View {
        StitchCard {
            HStack(alignment: .center) {
                VStack(alignment: .leading, spacing: 4) {
                    Text(charge.address.isEmpty ? (isDC ? "超充站" : "家充") : charge.address)
                        .font(StitchFont.bodyLg())
                        .fontWeight(.medium)
                        .foregroundColor(StitchColors.onSurface)
                    Text(formatLongDate(charge.startDate))
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
                Spacer()
                HStack(spacing: 8) {
                    dcAcTag
                    HStack(alignment: .bottom, spacing: 4) {
                        Text(String(format: "+%.1f", charge.chargeEnergyAdded))
                            .font(StitchFont.dataLg())
                            .foregroundColor(StitchColors.accent)
                        Text("kWh")
                            .font(StitchFont.labelCaps())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                            .padding(.bottom, 3)
                    }
                }
            }
        }
    }

    private var dcAcTag: some View {
        Text(isDC ? "DC" : "AC")
            .font(.system(size: 10, weight: .bold))
            .tracking(0.5)
            .foregroundColor(isDC ? Color(hex: "F59E0B") : Color(hex: "3B82F6"))
            .padding(.horizontal, 8)
            .padding(.vertical, 2)
            .overlay(
                RoundedRectangle(cornerRadius: 4)
                    .stroke(isDC ? Color(hex: "F59E0B") : Color(hex: "3B82F6"), lineWidth: 1)
            )
    }

    // MARK: - Battery Change Card

    private var batteryChangeCard: some View {
        StitchCard {
            Text("电量变化")
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(StitchColors.onSurface)
            Spacer().frame(height: 24)
            GeometryReader { geo in
                let w = geo.size.width
                let startFrac = CGFloat(startSoc) / 100.0
                let rangeFrac = CGFloat(max(0, endSoc - startSoc)) / 100.0
                ZStack(alignment: .leading) {
                    RoundedRectangle(cornerRadius: 8)
                        .fill(StitchColors.accent.opacity(0.10))
                    RoundedRectangle(cornerRadius: 8)
                        .fill(StitchColors.accent.opacity(0.35))
                        .frame(width: w * rangeFrac)
                        .offset(x: w * startFrac)
                    HStack {
                        Text("\(startSoc)%")
                            .font(StitchFont.bodySm())
                            .fontWeight(.medium)
                            .foregroundColor(StitchColors.onSurface)
                        Spacer()
                        Text("+\(endSoc - startSoc)%")
                            .font(StitchFont.bodySm())
                            .fontWeight(.bold)
                            .foregroundColor(StitchColors.accent)
                        Spacer()
                        Text("\(endSoc)%")
                            .font(StitchFont.bodySm())
                            .fontWeight(.medium)
                            .foregroundColor(StitchColors.onSurface)
                    }
                    .padding(.horizontal, 16)
                    .monospacedDigit()
                }
            }
            .frame(height: 48)
        }
    }

    // MARK: - Charging Curve Card

    private var chargingCurveCard: some View {
        StitchCard {
            HStack {
                Text("充电曲线")
                    .font(.system(size: 16, weight: .semibold))
                    .foregroundColor(StitchColors.onSurface)
                Spacer()
                HStack(spacing: 16) {
                    ForEach(CurveTab.allCases) { tab in
                        VStack(spacing: 4) {
                            Text(tab.label)
                                .font(.system(size: 14, weight: selectedTab == tab ? .bold : .regular))
                                .foregroundColor(selectedTab == tab ? StitchColors.onSurface : StitchColors.onSurfaceVariant)
                            Rectangle()
                                .fill(selectedTab == tab ? StitchColors.onSurface : Color.clear)
                                .frame(width: 20, height: 2)
                        }
                        .onTapGesture { selectedTab = tab }
                    }
                }
            }
            Spacer().frame(height: 16)
            CurveChart(values: curveValues, peakLabel: curvePeakLabel)
                .frame(height: 180)
            Spacer().frame(height: 16)
            HStack {
                ForEach(curveTimeAxis.indices, id: \.self) { i in
                    Text(curveTimeAxis[i])
                        .font(.system(size: 10, weight: .bold, design: .monospaced))
                        .tracking(0.8)
                        .foregroundColor(StitchColors.onSurfaceVariant)
                    if i < curveTimeAxis.count - 1 { Spacer() }
                }
            }
            Spacer().frame(height: 20)
            Text("模拟数据 — 基于充电摘要")
                .font(.system(size: 11))
                .italic()
                .foregroundColor(StitchColors.onSurfaceVariant)
        }
    }

    // MARK: - Stats Grid (2x2)

    private var statsGrid: some View {
        VStack(spacing: 16) {
            HStack(spacing: 16) {
                statCell(label: "充入电量", value: String(format: "%.1f", charge.chargeEnergyAdded), unit: "kWh")
                statCell(label: "费用", value: charge.cost > 0 ? String(format: "¥%.2f", charge.cost) : "免费", unit: "")
            }
            HStack(spacing: 16) {
                statCell(label: "平均功率", value: String(format: "%.1f", avgPowerKw), unit: "kW")
                statCell(label: "用时", value: formatDuration(durationMinutes), unit: "")
            }
        }
    }

    private func statCell(label: String, value: String, unit: String) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            Text(label)
                .font(.system(size: 10, weight: .bold))
                .tracking(0.5)
                .foregroundColor(StitchColors.onSurfaceVariant)
            Spacer(minLength: 8)
            HStack(alignment: .bottom, spacing: 4) {
                Spacer()
                Text(value)
                    .font(StitchFont.dataLg())
                    .fontWeight(.bold)
                    .foregroundColor(StitchColors.onSurface)
                if !unit.isEmpty {
                    Text(unit)
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(StitchColors.onSurfaceVariant)
                        .padding(.bottom, 3)
                }
            }
        }
        .frame(maxWidth: .infinity, minHeight: 64, alignment: .leading)
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Charging Stages Card

    private struct Stage {
        let name: String
        let range: String
        let minutes: Int
        let weight: CGFloat
        let done: Bool
    }

    private var stages: [Stage] {
        let total = durationMinutes
        let cc = Int(Double(total) * 0.78)
        let cv = Int(Double(total) * 0.15)
        let trickle = max(0, total - cc - cv)
        return [
            Stage(name: "恒流段", range: "0-80%", minutes: cc, weight: 0.80, done: true),
            Stage(name: "恒压段", range: "80-95%", minutes: cv, weight: 0.15, done: false),
            Stage(name: "涓流段", range: "95-100%", minutes: trickle, weight: 0.05, done: false)
        ]
    }

    private var chargingStagesCard: some View {
        StitchCard {
            Text("充电阶段")
                .font(.system(size: 16, weight: .semibold))
                .foregroundColor(StitchColors.onSurface)
            Spacer().frame(height: 24)
            GeometryReader { geo in
                HStack(spacing: 0) {
                    ForEach(stages.indices, id: \.self) { i in
                        Rectangle()
                            .fill(i == 0 ? StitchColors.onSurface : StitchColors.surfaceContainerHigh)
                            .frame(width: geo.size.width * stages[i].weight)
                    }
                }
            }
            .frame(height: 12)
            .clipShape(RoundedRectangle(cornerRadius: 6))
            Spacer().frame(height: 16)
            VStack(spacing: 12) {
                ForEach(stages.indices, id: \.self) { i in
                    let s = stages[i]
                    HStack {
                        HStack(spacing: 8) {
                            Image(systemName: s.done ? "checkmark.circle.fill" : "circle")
                                .font(.system(size: 16))
                                .foregroundColor(s.done ? StitchColors.onSurface : StitchColors.onSurfaceVariant)
                            Text("\(s.name) (\(s.range))")
                                .font(StitchFont.bodySm())
                                .foregroundColor(s.done ? StitchColors.onSurface : StitchColors.onSurfaceVariant)
                        }
                        Spacer()
                        Text(formatDuration(s.minutes))
                            .font(.system(size: 12, design: .monospaced))
                            .foregroundColor(StitchColors.onSurfaceVariant)
                    }
                }
            }
        }
    }

    // MARK: - Export Button

    private var exportButton: some View {
        Button { } label: {
            Text("导出此充电记录")
                .font(.system(size: 12, weight: .bold))
                .tracking(0.6)
                .foregroundColor(StitchColors.onSurface)
                .frame(maxWidth: .infinity)
                .padding(.vertical, 16)
        }
        .background(StitchColors.background)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Curve helpers

    private var curveValues: [Double] {
        switch selectedTab {
        case .power:
            let shape: [Double] = [0, 0.4, 0.75, 0.95, 1, 0.92, 0.8, 0.6, 0.4, 0.22, 0.1, 0.04]
            return shape.map { $0 * peakPowerKw }
        case .soc:
            let s = Double(startSoc)
            let e = Double(max(startSoc + 1, endSoc))
            return (0..<sampleCount).map { s + (e - s) * (Double($0) / Double(sampleCount - 1)) }
        case .voltage:
            return [230, 235, 240, 245, 250, 252, 250, 246, 242, 238, 234, 230]
        case .temp:
            return [22, 24, 26, 28, 30, 31, 30, 28, 26, 24, 22, 21]
        }
    }

    private var curvePeakLabel: String {
        switch selectedTab {
        case .power: return String(format: "%.1f kW 峰值", peakPowerKw)
        case .soc: return "\(endSoc)% 峰值"
        case .voltage: return "252 V 峰值"
        case .temp: return "31°C 峰值"
        }
    }

    private var curveTimeAxis: [String] {
        let start: String = {
            let iso = charge.startDate
            if iso.count >= 16 {
                let idx = iso.index(iso.startIndex, offsetBy: 11)
                let end = iso.index(iso.startIndex, offsetBy: 16)
                return String(iso[idx..<end])
            }
            return "18:30"
        }()
        return [start, "", "", "结束"]
    }

    // MARK: - Format helpers

    private func formatDuration(_ minutes: Int) -> String {
        let h = minutes / 60
        let m = minutes % 60
        return h > 0 ? "\(h)h \(m)m" : "\(m)m"
    }

    private func formatLongDate(_ iso: String) -> String {
        guard iso.count >= 16 else { return iso }
        let chars = Array(iso)
        let month = Int(String(chars[5...6])) ?? 1
        let day = Int(String(chars[8...9])) ?? 1
        let time = String(chars[11...15])
        return "\(month)月\(day)日 \(time)"
    }
}

// MARK: - Curve Chart (gold single line, peak dot, baseline)

private struct CurveChart: View {
    let values: [Double]
    let peakLabel: String

    var body: some View {
        GeometryReader { geo in
            let w = geo.size.width
            let h = geo.size.height
            let baseLineY = h - 8
            let topPad: CGFloat = 24
            let usableH = baseLineY - topPad
            let maxV = values.max() ?? 1
            let minV = values.min() ?? 0
            let range = (maxV - minV) > 0 ? (maxV - minV) : 1

            func px(_ i: Int) -> CGFloat { values.count > 1 ? w * CGFloat(i) / CGFloat(values.count - 1) : 0 }
            func py(_ v: Double) -> CGFloat { baseLineY - CGFloat((v - minV) / range) * usableH }

            let peakIdx = values.firstIndex(of: maxV) ?? 0

            ZStack {
                // baseline
                Path { p in
                    p.move(to: CGPoint(x: 0, y: baseLineY))
                    p.addLine(to: CGPoint(x: w, y: baseLineY))
                }
                .stroke(StitchColors.border, lineWidth: 1)

                // smooth gold curve
                Path { p in
                    guard values.count > 1 else { return }
                    p.move(to: CGPoint(x: px(0), y: py(values[0])))
                    for i in 1..<values.count {
                        let prev = CGPoint(x: px(i - 1), y: py(values[i - 1]))
                        let cur = CGPoint(x: px(i), y: py(values[i]))
                        let midX = (prev.x + cur.x) / 2
                        p.addCurve(to: cur,
                                   control1: CGPoint(x: midX, y: prev.y),
                                   control2: CGPoint(x: midX, y: cur.y))
                    }
                }
                .stroke(StitchColors.accent, style: StrokeStyle(lineWidth: 2.2, lineCap: .round, lineJoin: .round))

                // peak dot
                Circle()
                    .fill(StitchColors.accent)
                    .frame(width: 8, height: 8)
                    .position(x: px(peakIdx), y: py(maxV))

                // peak label
                VStack {
                    Text(peakLabel)
                        .font(.system(size: 10, weight: .bold, design: .monospaced))
                        .foregroundColor(StitchColors.accent)
                    Spacer()
                }
            }
        }
    }
}

// MARK: - Preview

#Preview {
    let sample = Charge(
        id: 1, carId: 1,
        startDate: "2025-07-01T18:30:00.000Z",
        endDate: "2025-07-01T20:45:00.000Z",
        chargeEnergyAdded: 38.5,
        chargeEnergyUsed: 40.2,
        startBatteryLevel: 65,
        endBatteryLevel: 100,
        startIdealRangeKm: 200,
        endIdealRangeKm: 400,
        cost: 9.25,
        chargeType: "AC",
        address: "家充",
        fastChargerBrand: nil,
        fastChargerType: nil
    )
    NavigationStack {
        ChargeDetailView(charge: sample)
    }
}
