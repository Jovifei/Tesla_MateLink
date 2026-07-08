import SwiftUI

struct ChargeListView: View {
    @EnvironmentObject var state: AppState
    @State private var charges: [Charge] = []
    @State private var loading = true

    var body: some View {
        NavigationStack {
            Group {
                if loading {
                    VStack(spacing: 16) {
                        ProgressView()
                            .tint(StitchColors.onSurface)
                        Text("加载中...")
                            .font(StitchFont.bodySm())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if charges.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "bolt.fill")
                            .font(.system(size: 40))
                            .foregroundColor(StitchColors.outlineVariant)
                        Text("暂无充电记录")
                            .font(StitchFont.headlineMd())
                            .foregroundColor(StitchColors.onSurface)
                        Text("连接充电桩开始记录")
                            .font(StitchFont.bodySm())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 16) {
                            // Top Stats Bento (2-column)
                            HStack(spacing: 16) {
                                BentoStatCard(
                                    label: "总电量",
                                    suffix: "本月",
                                    value: String(format: "%.0f", currentMonthEnergy),
                                    unit: "kWh",
                                    valueColor: StitchColors.accent
                                )
                                BentoStatCard(
                                    label: "总费用",
                                    suffix: "本月",
                                    value: String(format: "%.2f", currentMonthCost),
                                    prefix: "¥",
                                    valueColor: StitchColors.onSurface
                                )
                            }

                            // 历史明细 section header
                            Text("历史明细")
                                .font(StitchFont.labelCaps())
                                .foregroundColor(StitchColors.onSurfaceVariant)

                            // Monthly groups
                            ForEach(sortedMonthKeys, id: \.self) { key in
                                if let group = grouped[key] {
                                    Text(monthTitle(key))
                                        .font(StitchFont.bodyLg())
                                        .fontWeight(.semibold)
                                        .foregroundColor(StitchColors.onSurface)
                                        .padding(.top, 4)

                                    ForEach(group) { ch in
                                        NavigationLink(destination: ChargeDetailView(charge: ch)) {
                                            chargeItemCard(ch)
                                        }
                                        .buttonStyle(.plain)
                                    }
                                }
                            }
                            Spacer(minLength: 16)
                        }
                        .padding(.horizontal, 24)
                    }
                }
            }
            .background(StitchColors.background)
            .navigationTitle("充电历史")
            .navigationBarTitleDisplayMode(.large)
            .toolbar {
                ToolbarItem(placement: .topBarTrailing) {
                    NavigationLink(destination: CurrentChargeView()) {
                        Image(systemName: "bolt.fill")
                            .foregroundColor(StitchColors.accent)
                    }
                }
            }
            .refreshable { await load() }
            .task { await load() }
        }
    }

    // MARK: - Derived data

    private var grouped: [String: [Charge]] {
        Dictionary(grouping: charges) { monthKey($0.startDate) }
    }

    private var sortedMonthKeys: [String] {
        grouped.keys.sorted().reversed()
    }

    private var currentMonthEnergy: Double {
        (grouped[sortedMonthKeys.first ?? ""] ?? []).reduce(0) { $0 + $1.chargeEnergyAdded }
    }

    private var currentMonthCost: Double {
        (grouped[sortedMonthKeys.first ?? ""] ?? []).reduce(0) { $0 + $1.cost }
    }

    private func monthKey(_ iso: String?) -> String {
        guard let iso = iso, iso.count >= 7 else { return "未知" }
        return String(iso.prefix(7))
    }

    private func monthTitle(_ key: String) -> String {
        let parts = key.split(separator: "-")
        guard parts.count == 2, let m = Int(parts[1]) else { return key }
        return "\(parts[0])年\(m)月"
    }

    // MARK: - Bento Stat Card

    private func BentoStatCard(label: String, suffix: String, value: String,
                               unit: String = "", prefix: String = "",
                               valueColor: Color) -> some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Text(label)
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                Text(suffix)
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            HStack(alignment: .lastTextBaseline, spacing: 2) {
                if !prefix.isEmpty {
                    Text(prefix)
                        .font(StitchFont.labelCaps())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
                Text(value)
                    .font(StitchFont.dataLg())
                    .foregroundColor(valueColor)
                if !unit.isEmpty {
                    Text(unit)
                        .font(StitchFont.labelCaps())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(24)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }

    // MARK: - Charge List Item Card

    @ViewBuilder
    private func chargeItemCard(_ ch: Charge) -> some View {
        let isDC = ch.chargeType == "DC"
        let startPct = ch.startBatteryLevel ?? 0
        let endPct = ch.endBatteryLevel ?? 0

        VStack(alignment: .leading, spacing: 0) {
            // Top segment
            VStack(alignment: .leading, spacing: 16) {
                HStack(alignment: .top) {
                    VStack(alignment: .leading, spacing: 2) {
                        Text(ch.address)
                            .font(StitchFont.bodyLg())
                            .fontWeight(.bold)
                            .foregroundColor(StitchColors.onSurface)
                            .lineLimit(2)
                    }
                    Spacer(minLength: 16)
                    HStack(alignment: .lastTextBaseline, spacing: 4) {
                        Text(String(format: "+%.1f", ch.chargeEnergyAdded))
                            .font(StitchFont.dataLg())
                            .foregroundColor(StitchColors.accent)
                        Text("kWh")
                            .font(StitchFont.labelCaps())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                    }
                }
                // Battery range row
                HStack(spacing: 8) {
                    Text("\(startPct)%")
                        .font(StitchFont.bodySm())
                        .monospacedDigit()
                        .foregroundColor(StitchColors.onSurfaceVariant)
                    BatteryRangeBar(start: startPct, end: endPct)
                        .frame(height: 4)
                    Text("\(endPct)%")
                        .font(StitchFont.bodySm())
                        .monospacedDigit()
                        .fontWeight(.bold)
                        .foregroundColor(StitchColors.onSurface)
                    Text(formatDuration(ch))
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
            }
            .padding(24)

            // Divider
            Rectangle()
                .fill(StitchColors.border)
                .frame(height: 1)

            // Footer (grey background)
            HStack(spacing: 12) {
                Text(formatShortDate(ch.startDate))
                    .font(.custom("JetBrainsMono-Medium", size: 13))
                    .foregroundColor(StitchColors.onSurfaceVariant)
                DcAcTag(isDc: isDC)
                Spacer()
                Text(String(format: "¥%.2f", ch.cost))
                    .font(StitchFont.dataMd())
                    .fontWeight(.bold)
                    .foregroundColor(StitchColors.onSurface)
                Image(systemName: "chevron.right")
                    .font(.system(size: 14))
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 12)
            .background(Color(hex: "F8FAFC"))
        }
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
        .overlay(alignment: .leading) {
            // DC orange left bar (4px); AC has no marker
            if isDC {
                Rectangle()
                    .fill(Color(hex: "F59E0B"))
                    .frame(width: 4)
            }
        }
    }

    // MARK: - Battery Range Bar

    private struct BatteryRangeBar: View {
        let start: Int
        let end: Int

        var body: some View {
            GeometryReader { geo in
                let w = geo.size.width
                let startF = CGFloat(max(0, min(100, start))) / 100
                let rangeF = CGFloat(max(0, min(100, end - start))) / 100
                ZStack(alignment: .leading) {
                    Rectangle()
                        .foregroundColor(StitchColors.outlineVariant)
                    Rectangle()
                        .foregroundColor(StitchColors.accent)
                        .frame(width: max(0, w * rangeF))
                        .offset(x: w * startF)
                }
            }
        }
    }

    // MARK: - DC/AC Tag

    private struct DcAcTag: View {
        let isDc: Bool

        var body: some View {
            let color = isDc ? Color(hex: "F59E0B") : Color(hex: "3B82F6")
            Text(isDc ? "DC 直流" : "AC 交流")
                .font(.system(size: 10, weight: .bold))
                .foregroundColor(color)
                .padding(.horizontal, 8)
                .padding(.vertical, 2)
                .background(
                    RoundedRectangle(cornerRadius: 2)
                        .stroke(color, lineWidth: 1)
                )
        }
    }

    // MARK: - Helpers

    func load() async {
        loading = true
        let carId = state.currentCarId
        if let api = state.real, let cached = await api.getCachedCharges(carId: carId) { charges = cached }
        if state.isMockMode {
            charges = await state.mock.getCharges(carId)
        } else if let api = state.real {
            do {
                let fresh: [Charge] = try await api.fetch("api/v1/cars/\(carId)/charges")
                charges = fresh; await api.cacheCharges(fresh, carId: carId)
            } catch { /* stale cache stays visible */ }
        }
        loading = false
    }

    private func formatDuration(_ ch: Charge) -> String {
        guard let endStr = ch.endDate,
              let start = ISO8601Parser.parse(ch.startDate),
              let end = ISO8601Parser.parse(endStr) else {
            return "—"
        }
        let mins = Int(end.timeIntervalSince(start) / 60)
        let h = mins / 60
        let m = mins % 60
        return h > 0 ? "\(h)h \(m)m" : "\(m)m"
    }

    /** ISO "2025-07-22T14:30:..." -> "07-22 14:30" */
    private func formatShortDate(_ iso: String?) -> String {
        guard let iso = iso, iso.count >= 16 else { return "—" }
        let s = String(iso.prefix(16)).replacingOccurrences(of: "T", with: " ")
        return String(s.dropFirst(5))
    }
}
