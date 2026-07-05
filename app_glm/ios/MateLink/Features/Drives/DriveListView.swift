import SwiftUI

struct DriveListView: View {
    @EnvironmentObject var state: AppState
    @State private var drives: [Drive] = []; @State private var loading = true

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
                } else if drives.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "road.lanes")
                            .font(.system(size: 40))
                            .foregroundColor(StitchColors.outlineVariant)
                        Text("No Drives Yet")
                            .font(StitchFont.headlineMd())
                            .foregroundColor(StitchColors.onSurface)
                        Text("Go for a drive!")
                            .font(StitchFont.bodySm())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ScrollView {
                        LazyVStack(alignment: .leading, spacing: 12) {
                            ForEach(groupedKeys(), id: \.self) { label in
                                // Month header
                                Text(label)
                                    .font(StitchFont.headlineMd())
                                    .foregroundColor(StitchColors.onSurface)
                                    .padding(.top, 16)
                                    .padding(.bottom, 8)

                                ForEach(drivesForGroup(label)) { d in
                                    NavigationLink(destination: DriveDetailView(drive: d)) {
                                        driveCard(d)
                                    }
                                    .buttonStyle(.plain)
                                }
                            }
                            Spacer(minLength: 16)
                        }
                        .padding(.horizontal, 24)
                    }
                }
            }
            .background(StitchColors.background)
            .navigationTitle("Drive History")
            .navigationBarTitleDisplayMode(.large)
            .refreshable { await load() }
            .task { await load() }
        }
    }

    // MARK: - Drive Card

    @ViewBuilder
    private func driveCard(_ d: Drive) -> some View {
        VStack(alignment: .leading, spacing: 0) {
            // Top row: start → end + distance
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(d.startAddress)
                        .font(StitchFont.bodyLg())
                        .foregroundColor(StitchColors.onSurface)
                    Text("→ \(d.endAddress)")
                        .font(StitchFont.bodySm())
                        .foregroundColor(StitchColors.onSurfaceVariant)
                }
                Spacer(minLength: 12)
                Text(String(format: "%.1f km", d.distanceKm))
                    .font(StitchFont.dataMd())
                    .foregroundColor(StitchColors.onSurface)
            }

            Spacer(minLength: 16)

            // Data rows
            HStack {
                Text("时长")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                Text(formatDuration(d.durationMin))
                    .font(StitchFont.bodyLg())
                    .foregroundColor(StitchColors.onSurface)
            }
            Spacer(minLength: 8)
            HStack {
                Text("能耗")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                Text("\(d.efficiency) Wh/km")
                    .font(StitchFont.bodyLg())
                    .foregroundColor(StitchColors.onSurface)
            }
            Spacer(minLength: 8)
            HStack {
                Text("电量")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                Text("\(d.startBatteryLevel)% → \(d.endBatteryLevel)%")
                    .font(StitchFont.bodyLg())
                    .foregroundColor(StitchColors.onSurface)
            }

            // Efficiency chip
            if d.efficiency > 0 {
                Spacer(minLength: 12)
                efficiencyChip(d.efficiency)
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

    // MARK: - Efficiency Chip

    @ViewBuilder
    private func efficiencyChip(_ efficiency: Int) -> some View {
        let (text, bgColor, fgColor): (String, Color, Color) = {
            switch efficiency {
            case ...150: return ("高效", StitchColors.statusOnlineBg, StitchColors.statusOnline)
            case ...200: return ("正常", StitchColors.statusChargingBg, StitchColors.statusCharging)
            default:     return ("偏高", StitchColors.statusErrorBg, StitchColors.statusError)
            }
        }()

        Text(text)
            .font(.system(size: 11, weight: .bold))
            .foregroundColor(fgColor)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(bgColor)
            .clipShape(Capsule())
    }

    // MARK: - Helpers

    func load() async {
        loading = true
        let carId = state.currentCarId
        if let api = state.real, let cached = await api.getCachedDrives(carId: carId) { drives = cached }
        if state.isMockMode {
            drives = await state.mock.getDrives(carId)
        } else if let api = state.real {
            do {
                let fresh: [Drive] = try await api.fetch("api/v1/cars/\(carId)/drives")
                drives = fresh; await api.cacheDrives(fresh, carId: carId)
            } catch { /* stale cache stays visible */ }
        }
        loading = false
    }

    func groupedKeys() -> [String] {
        let today = ISO8601Parser.todayString()
        return Array(Set(drives.map { String($0.startDate.prefix(10)) == today ? "Today" : String($0.startDate.prefix(10)) })).sorted(by: >)
    }

    func drivesForGroup(_ label: String) -> [Drive] {
        let today = ISO8601Parser.todayString()
        return drives.filter { d in let k = String(d.startDate.prefix(10)); return (label == "Today" && k == today) || label == k }
    }

    private func formatDuration(_ minutes: Int) -> String {
        let h = minutes / 60
        let m = minutes % 60
        return h > 0 ? "\(h)h \(m)m" : "\(m)m"
    }
}
