import SwiftUI

struct ChargeListView: View {
    @EnvironmentObject var state: AppState; @State private var charges: [Charge] = []; @State private var loading = true

    var body: some View {
        NavigationStack {
            Group {
                if loading {
                    VStack(spacing: 16) {
                        ProgressView()
                            .tint(StitchColors.onSurface)
                        Text("Loading...")
                            .font(StitchFont.bodySm())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if charges.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "bolt.fill")
                            .font(.system(size: 40))
                            .foregroundColor(StitchColors.outlineVariant)
                        Text("No Charges Yet")
                            .font(StitchFont.headlineMd())
                            .foregroundColor(StitchColors.onSurface)
                        Text("Plug in to start charging!")
                            .font(StitchFont.bodySm())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 12) {
                            ForEach(charges) { ch in
                                NavigationLink(destination: ChargeDetailView(charge: ch)) {
                                    chargeCard(ch)
                                }
                                .buttonStyle(.plain)
                            }
                            Spacer(minLength: 16)
                        }
                        .padding(.horizontal, 24)
                    }
                }
            }
            .background(StitchColors.background)
            .navigationTitle("Charge History")
            .navigationBarTitleDisplayMode(.large)
            .refreshable { await load() }
            .task { await load() }
        }
    }

    // MARK: - Charge Card

    @ViewBuilder
    private func chargeCard(_ ch: Charge) -> some View {
        let isDC = ch.chargeType == "DC"

        VStack(alignment: .leading, spacing: 0) {
            // Top row: address + DC/AC chip
            HStack(alignment: .center) {
                Text(ch.address)
                    .font(StitchFont.bodyLg())
                    .foregroundColor(StitchColors.onSurface)
                    .lineLimit(2)
                Spacer(minLength: 8)
                Text(isDC ? "DC" : "AC")
                    .font(.system(size: 11, weight: .bold))
                    .foregroundColor(isDC ? StitchColors.statusCharging : StitchColors.statusOffline)
                    .padding(.horizontal, 10)
                    .padding(.vertical, 4)
                    .background(isDC ? StitchColors.statusChargingBg : StitchColors.statusOfflineBg)
                    .clipShape(Capsule())
            }

            Spacer(minLength: 16)

            // Energy + cost row
            HStack {
                Text(String(format: "%.1f kWh", ch.chargeEnergyAdded))
                    .font(StitchFont.dataMd())
                    .foregroundColor(StitchColors.onSurface)
                Spacer()
                if ch.cost > 0 {
                    Text(ch.cost.formatted(.currency(code: Locale.current.currency?.identifier ?? "USD")))
                        .font(StitchFont.dataMd())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
            }

            Spacer(minLength: 16)

            // Duration row
            HStack {
                Text("时长")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                Text(formatDuration(ch))
                    .font(StitchFont.bodyLg())
                    .foregroundColor(StitchColors.onSurface)
            }
            Spacer(minLength: 8)

            // Battery row
            HStack {
                Text("电量")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                Text("\(ch.startBatteryLevel)% → \(ch.endBatteryLevel ?? 0)%")
                    .font(StitchFont.bodyLg())
                    .foregroundColor(StitchColors.onSurface)
            }

            // Max power row (if available)
            if let fastType = ch.fastChargerType, !fastType.isEmpty {
                Spacer(minLength: 8)
                HStack {
                    Text("充电机")
                        .font(StitchFont.labelCaps())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                    Spacer()
                    Text(fastType)
                        .font(StitchFont.bodyLg())
                        .foregroundColor(StitchColors.onSurface)
                }
            }
        }
        .padding(24)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(StitchColors.border, lineWidth: 1)
        )
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
}
