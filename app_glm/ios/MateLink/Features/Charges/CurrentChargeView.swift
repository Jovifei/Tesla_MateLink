import SwiftUI

/// 当前充电 (Current Charge) — real-time charging monitor.
///
/// 1:1 restoration of Stitch screen 5d52c8ca82df434e9bd4a67e74290ffc.
///
/// DATA-LAYER GAP: there is no live-charging view model on iOS. `Charge`
/// (Core/Models/CarStatus.swift) only models a completed historical charge
/// record; `CarStatus` DOES carry every live field this screen needs
/// (chargerPower, chargerVoltage, chargerActualCurrent, batteryLevel,
/// chargeLimitSoc, timeToFullCharge, chargeEnergyAdded) but nothing polls a
/// live CarStatus into the charges feature, and the 1-second power curve is
/// not persisted anywhere.
///
/// TODO(data-layer): poll `api/v1/cars/{id}/status` on a 1s cadence while
///   `state == .charging`, map CarStatus -> the fields below, and keep a
///   rolling power buffer for the live curve. Until then this renders
///   representative mock values matching the Stitch comp.
struct CurrentChargeView: View {
    // Stitch brand accents (current-charge.html)
    private let brandOrange = Color(hex: "F59E0B")
    private let brandGreen = Color(hex: "059669")
    private let brandGold = Color(hex: "A16207")
    private let ringTrack = Color(hex: "F5F5F5")

    // TODO(data-layer): replace mock snapshot with live CarStatus mapping.
    private let soc = 78
    private let startSoc = 65
    private let targetSoc = 100
    private let powerKw = 7.4
    private let voltageV = 230
    private let currentA = 32
    private let chargeType = "AC 慢充"
    private let stationName = "家庭充电桩"
    private let pluggedTime = "18:30"
    private let elapsed = "42 min"
    private let energyAdded = 5.2
    private let etaClock = "21:45"
    private let etaRemaining = "约 2h 15min 后"
    private let etaEnergy = "+12.3 kWh"
    private let batteryTemp = 42
    private let costSoFar = 3.85
    private let costEstimate = 9.10
    private let avgPrice = 0.74
    // Rolling power-curve samples (0..1 normalized). Mock trace.
    private let powerCurve: [CGFloat] = [0.20, 0.25, 0.22, 0.30, 0.28, 0.60, 0.55, 0.70, 0.68]

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 32) {
                // Section 2: Giant charging status card
                chargingStatusCard

                // Section 3: Real-time data grid
                HStack(spacing: 16) {
                    metricCell(value: String(format: "%.1f", powerKw), unit: "kW",
                               label: "功率", valueColor: brandOrange)
                    metricCell(value: "\(voltageV)", unit: "V", label: "电压",
                               valueColor: StitchColors.onSurface)
                    metricCell(value: "\(currentA)", unit: "A", label: "电流",
                               valueColor: StitchColors.onSurface)
                }

                // Section 4: Charging info list
                chargingInfoList

                // Section 5: Estimated completion
                estimatedCompletionCard

                // Section 6: Real-time power curve
                powerCurveCard

                // Section 7: Charging phases
                chargingPhasesCard

                // Section 8 & 9: Temperature & cost
                HStack(spacing: 16) {
                    batteryTempCard
                    costEstimateCard
                }

                // Section 10: Control buttons
                controlButton(text: "停止充电", icon: "stop.circle", color: StitchColors.error) {
                    // TODO(data-layer): POST stop-charge command
                }
                controlButton(text: "设置充电限制", icon: "slider.horizontal.3", color: StitchColors.primary) {
                    // TODO: navigate to charge-limit setting
                }

                Spacer(minLength: 16)
            }
            .padding(.horizontal, 16)
            .padding(.vertical, 24)
        }
        .background(StitchColors.background)
        .navigationTitle("正在充电")
        .navigationBarTitleDisplayMode(.large)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Button {
                    // TODO(data-layer): trigger live refresh
                } label: {
                    Image(systemName: "arrow.clockwise")
                        .foregroundColor(StitchColors.primary)
                }
            }
        }
    }

    // MARK: - Section 2: Charging status card

    private var chargingStatusCard: some View {
        VStack(spacing: 16) {
            ZStack {
                Circle()
                    .stroke(ringTrack, lineWidth: 8)
                Circle()
                    .trim(from: 0, to: CGFloat(soc) / 100)
                    .stroke(StitchColors.onSurface,
                            style: StrokeStyle(lineWidth: 8, lineCap: .round))
                    .rotationEffect(.degrees(-90))
                Text("\(soc)%")
                    .font(StitchFont.displayLg())
                    .monospacedDigit()
                    .foregroundColor(StitchColors.onSurface)
            }
            .frame(width: 192, height: 192)

            Text("\(startSoc)% → \(targetSoc)%")
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)

            Text("充电中")
                .font(.system(size: 10, weight: .bold))
                .tracking(1.5)
                .foregroundColor(.white)
                .padding(.horizontal, 12)
                .padding(.vertical, 4)
                .background(brandOrange)
                .clipShape(Capsule())
        }
        .frame(maxWidth: .infinity)
        .padding(24)
        .background(StitchColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Section 3: Metric cell

    private func metricCell(value: String, unit: String, label: String,
                            valueColor: Color) -> some View {
        VStack(spacing: 8) {
            VStack(spacing: 0) {
                Text(value)
                    .font(StitchFont.dataLg())
                    .foregroundColor(valueColor)
                Text(unit)
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            Text(label)
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity)
        .padding(16)
        .background(StitchColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Section 4: Charging info list

    private var chargingInfoList: some View {
        VStack(spacing: 0) {
            HStack {
                Text("充电信息")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
            }
            .padding(16)
            .background(StitchColors.surfaceContainerLow)
            divider
            infoRow("充电类型", chargeType, mono: false)
            divider
            infoRow("充电站", stationName, mono: false)
            divider
            infoRow("接入时间", pluggedTime, mono: true)
            divider
            infoRow("已充时间", elapsed, mono: true)
            divider
            infoRow("累计电量", String(format: "%.1f kWh", energyAdded), mono: true)
        }
        .background(StitchColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    private func infoRow(_ label: String, _ value: String, mono: Bool) -> some View {
        HStack {
            Text(label)
                .font(StitchFont.bodySm())
                .foregroundColor(StitchColors.onSurfaceVariant)
            Spacer()
            Text(value)
                .font(mono ? StitchFont.dataMd() : StitchFont.bodyLg())
                .fontWeight(mono ? .regular : .semibold)
                .foregroundColor(StitchColors.onSurface)
        }
        .padding(16)
    }

    // MARK: - Section 5: Estimated completion

    private var estimatedCompletionCard: some View {
        HStack(spacing: 0) {
            Rectangle()
                .fill(brandGreen)
                .frame(width: 4)
            VStack(alignment: .leading, spacing: 16) {
                Text("预计完成")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                HStack(alignment: .bottom) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(etaClock)
                            .font(StitchFont.displayLg())
                            .foregroundColor(StitchColors.onSurface)
                        Text(etaRemaining)
                            .font(StitchFont.bodySm())
                            .fontWeight(.semibold)
                            .foregroundColor(brandGreen)
                    }
                    Spacer()
                    VStack(alignment: .trailing, spacing: 2) {
                        Text("增程预计")
                            .font(StitchFont.bodySm())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                        Text(etaEnergy)
                            .font(StitchFont.dataMd())
                            .foregroundColor(StitchColors.onSurface)
                    }
                }
                // Progress bar (3/4)
                GeometryReader { geo in
                    ZStack(alignment: .leading) {
                        Capsule()
                            .fill(StitchColors.surfaceContainerHigh)
                        Capsule()
                            .fill(brandGreen)
                            .frame(width: geo.size.width * 0.75)
                    }
                }
                .frame(height: 6)
            }
            .padding(24)
        }
        .background(StitchColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Section 6: Real-time power curve

    private var powerCurveCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            HStack {
                Text("功率曲线 · 实时")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                HStack(spacing: 8) {
                    Circle()
                        .fill(brandOrange)
                        .frame(width: 8, height: 8)
                    Text(String(format: "%.1f kW", powerKw))
                        .font(StitchFont.dataMd())
                        .foregroundColor(brandOrange)
                }
            }
            PowerCurveChart(samples: powerCurve,
                            lineColor: StitchColors.onSurface,
                            headColor: brandOrange)
                .frame(height: 128)
        }
        .padding(24)
        .background(StitchColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Section 7: Charging phases

    private var chargingPhasesCard: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("充电阶段")
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
            // Segmented bar: 80% CC / 15% CV / 5% trickle
            GeometryReader { geo in
                let w = geo.size.width
                HStack(spacing: 4) {
                    RoundedRectangle(cornerRadius: 6)
                        .fill(brandOrange)
                        .frame(width: max(0, (w - 8) * 0.80))
                    RoundedRectangle(cornerRadius: 6)
                        .fill(StitchColors.surfaceContainerHigh)
                        .frame(width: max(0, (w - 8) * 0.15))
                    RoundedRectangle(cornerRadius: 6)
                        .fill(StitchColors.surfaceContainerHigh)
                        .frame(width: max(0, (w - 8) * 0.05))
                }
            }
            .frame(height: 12)
            HStack {
                Text("恒流段 (0-80%)")
                    .font(.system(size: 10, weight: .bold))
                    .foregroundColor(brandOrange)
                Spacer()
                Text("恒压段 (80-95%)")
                    .font(.system(size: 10))
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                Text("涓流段")
                    .font(.system(size: 10))
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
        }
        .padding(24)
        .background(StitchColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Section 8: Battery temp

    private var batteryTempCard: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("电池温度")
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(StitchColors.onSurfaceVariant)
            HStack(alignment: .bottom, spacing: 2) {
                Text("\(batteryTemp)")
                    .font(StitchFont.dataLg())
                    .foregroundColor(StitchColors.onSurface)
                Text("°C")
                    .font(.system(size: 14))
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            Text("正常")
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(brandGreen)
                .padding(.horizontal, 6)
                .padding(.vertical, 2)
                .background(brandGreen.opacity(0.1))
                .clipShape(RoundedRectangle(cornerRadius: 4))
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(StitchColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Section 9: Cost estimate

    private var costEstimateCard: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("费用预估")
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(StitchColors.onSurfaceVariant)
            HStack {
                Text("已产生")
                    .font(.system(size: 10))
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                Text(String(format: "¥%.2f", costSoFar))
                    .font(StitchFont.dataMd())
                    .foregroundColor(StitchColors.onSurface)
            }
            divider
            HStack {
                Text("预计")
                    .font(.system(size: 10))
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                Text(String(format: "¥%.2f", costEstimate))
                    .font(StitchFont.dataMd())
                    .foregroundColor(brandGold)
            }
            Text(String(format: "均价 ¥%.2f/kWh", avgPrice))
                .font(.system(size: 9))
                .foregroundColor(StitchColors.onSurfaceVariant)
                .frame(maxWidth: .infinity, alignment: .trailing)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(StitchColors.surface)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Section 10: Control button

    private func controlButton(text: String, icon: String, color: Color,
                               action: @escaping () -> Void) -> some View {
        Button(action: action) {
            HStack(spacing: 8) {
                Image(systemName: icon)
                Text(text)
                    .font(StitchFont.headlineMd())
            }
            .foregroundColor(color)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 16)
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(color, lineWidth: 2))
        }
    }

    // MARK: - Helpers

    private var divider: some View {
        Rectangle()
            .fill(StitchColors.border)
            .frame(height: 1)
    }
}

// MARK: - Power Curve Chart

private struct PowerCurveChart: View {
    let samples: [CGFloat]
    let lineColor: Color
    let headColor: Color

    var body: some View {
        GeometryReader { geo in
            let w = geo.size.width
            let h = geo.size.height
            let count = samples.count
            ZStack {
                if count >= 2 {
                    Path { path in
                        let stepX = w / CGFloat(count - 1)
                        func px(_ i: Int) -> CGFloat { stepX * CGFloat(i) }
                        // 0 at bottom, 1 at top, 5% top/bottom padding
                        func py(_ v: CGFloat) -> CGFloat {
                            h - (min(max(v, 0), 1) * h * 0.9) - h * 0.05
                        }
                        path.move(to: CGPoint(x: px(0), y: py(samples[0])))
                        for i in 1..<count {
                            path.addLine(to: CGPoint(x: px(i), y: py(samples[i])))
                        }
                    }
                    .stroke(lineColor, style: StrokeStyle(lineWidth: 2, lineCap: .round))

                    // Live head marker
                    let stepX = w / CGFloat(count - 1)
                    let hx = stepX * CGFloat(count - 1)
                    let hy = h - (min(max(samples[count - 1], 0), 1) * h * 0.9) - h * 0.05
                    Circle()
                        .fill(headColor)
                        .frame(width: 8, height: 8)
                        .position(x: hx, y: hy)
                }
            }
        }
    }
}
