import SwiftUI

struct Destination: Identifiable {
    let id = UUID()
    let name: String
    let count: Int
    let totalKm: Int
    let avgEff: Int
}

enum DestSort: String, CaseIterable { case count = "Visits", km = "Distance", eff = "Efficiency" }

func effColor(_ eff: Int) -> Color {
    eff < 150 ? Color(hex: "22C55E") : eff < 200 ? Color(hex: "F59E0B") : Color(hex: "EF4444")
}

// MARK: - Region grouping

enum Region: String, CaseIterable, Identifiable {
    case northAmerica = "North America"
    case europe = "Europe"
    case asia = "Asia"
    case australia = "Australia"
    case other = "Other"

    var id: String { rawValue }
    var icon: String {
        switch self {
        case .northAmerica: return "globe.americas.fill"
        case .europe: return "globe.europe.africa.fill"
        case .asia: return "globe.asia.australia.fill"
        case .australia: return "globe.asia.australia.fill"
        case .other: return "globe"
        }
    }
}

struct RegionStat: Identifiable {
    let id = UUID()
    let region: Region
    var visitCount: Int
    var totalKm: Double
}

private let countryRegionMap: [String: Region] = {
    var m: [String: Region] = [:]
    for c in ["US", "CA", "MX"] { m[c] = .northAmerica }
    for c in ["GB", "DE", "FR", "IT", "ES", "NL", "BE", "AT", "CH", "SE", "NO", "DK", "FI", "PT", "IE", "PL", "CZ", "HU", "RO", "GR", "HR", "SK", "SI", "BG", "LT", "LV", "EE", "LU"] { m[c] = .europe }
    for c in ["CN", "JP", "KR", "TW", "HK", "SG", "IN", "TH", "VN", "MY", "PH", "ID"] { m[c] = .asia }
    for c in ["AU", "NZ"] { m[c] = .australia }
    return m
}()

private func regionFromAddress(_ address: String) -> Region {
    let upper = address.uppercased()
    for (code, region) in countryRegionMap {
        if upper.contains(code) { return region }
    }
    // Fallback: check common keywords
    if upper.contains("USA") || upper.contains("UNITED STATES") || upper.contains("CANADA") { return .northAmerica }
    if upper.contains("UNITED KINGDOM") || upper.contains("GERMANY") || upper.contains("FRANCE") || upper.contains("EUROPE") { return .europe }
    if upper.contains("CHINA") || upper.contains("JAPAN") || upper.contains("KOREA") { return .asia }
    if upper.contains("AUSTRALIA") { return .australia }
    return .other
}

struct DestinationsView: View {
    @EnvironmentObject var state: AppState
    @State private var destinations: [Destination] = []
    @State private var regionStats: [RegionStat] = []
    @State private var sort: DestSort = .count

    var sorted: [Destination] {
        switch sort {
        case .count: return destinations.sorted { $0.count > $1.count }
        case .km: return destinations.sorted { $0.totalKm > $1.totalKm }
        case .eff: return destinations.sorted { $0.avgEff < $1.avgEff }
        }
    }

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 16) {
                    // Header + Sort Picker
                    HStack {
                        Text("Top Destinations").font(.title2).bold()
                        Spacer()
                        Picker("Sort", selection: $sort) {
                            ForEach(DestSort.allCases, id: \.self) { s in
                                Text(s.rawValue).tag(s)
                            }
                        }
                        .pickerStyle(.segmented)
                        .frame(width: 240)
                    }
                    .padding(.horizontal)

                    // Destination List
                    if sorted.isEmpty {
                        ContentUnavailableView("No Destinations",
                            systemImage: "mappin.slash",
                            description: Text("Complete drives to build your destination history."))
                            .padding(.top, 60)
                    } else {
                        VStack(spacing: 0) {
                            ForEach(Array(sorted.prefix(20).enumerated()), id: \.element.id) { i, d in
                                HStack(spacing: 12) {
                                    // Rank
                                    Text("\(i + 1)")
                                        .font(.title3.weight(.bold))
                                        .foregroundColor(.secondary.opacity(0.5))
                                        .frame(width: 32)

                                    Image(systemName: "mappin.circle.fill")
                                        .font(.title2).foregroundColor(.accentColor)

                                    // Info
                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(d.name).font(.subheadline.weight(.medium)).lineLimit(1)
                                        Text("\(d.count) visits").font(.caption2).foregroundColor(.secondary)
                                    }
                                    .frame(maxWidth: .infinity, alignment: .leading)

                                    // Distance
                                    VStack(alignment: .trailing, spacing: 1) {
                                        Text("\(d.totalKm)").font(.subheadline.weight(.medium))
                                        Text(UnitFormatter.distanceUnit(state.unitSystem)).font(.caption2).foregroundColor(.secondary)
                                    }
                                    .frame(width: 60)

                                    // Efficiency
                                    VStack(alignment: .trailing, spacing: 1) {
                                        Text("\(d.avgEff)").font(.subheadline.weight(.bold))
                                            .foregroundColor(effColor(d.avgEff))
                                        Text("Wh/km").font(.caption2).foregroundColor(.secondary)
                                    }
                                    .frame(width: 60)
                                }
                                .padding(.horizontal).padding(.vertical, 10)
                                if i < min(sorted.count, 20) - 1 { Divider() }
                            }
                        }
                        .background(.regularMaterial)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                        .padding(.horizontal)
                    }
                    // Visited Regions
                    if !regionStats.isEmpty {
                        VStack(alignment: .leading, spacing: 8) {
                            Text("Visited Regions").font(.title3).bold()
                            ForEach(regionStats) { rs in
                                HStack(spacing: 12) {
                                    Image(systemName: rs.region.icon)
                                        .font(.title2)
                                        .foregroundColor(.accentColor)
                                        .frame(width: 32)

                                    VStack(alignment: .leading, spacing: 2) {
                                        Text(rs.region.rawValue)
                                            .font(.subheadline.weight(.medium))
                                        Text("\(rs.visitCount) visits")
                                            .font(.caption2)
                                            .foregroundColor(.secondary)
                                    }
                                    .frame(maxWidth: .infinity, alignment: .leading)

                                    Text("\(Int(rs.totalKm.rounded()))")
                                        .font(.subheadline.weight(.medium))
                                    Text("km")
                                        .font(.caption2)
                                        .foregroundColor(.secondary)
                                }
                                .padding(.horizontal)
                                .padding(.vertical, 10)
                            }
                        }
                        .padding(.horizontal)
                    }
                }
                .padding(.vertical)
            }
            .navigationBarTitleDisplayMode(.inline)
            .task { await loadData() }
        }
    }

    func loadData() async {
        let drives: [Drive] = await {
            if state.isMockMode {
                return await state.mock.getDrives(state.currentCarId)
            } else if let api = state.real {
                return (try? await api.fetch("/api/v1/cars/\(state.currentCarId)/drives")) ?? []
            }
            return []
        }()

        // Compute region + destination grouping on background thread
        let (newDests, newRegions) = await withCheckedContinuation { cont in
            DispatchQueue.global(qos: .userInitiated).async {
                var map: [String: (count: Int, totalKm: Double, effSum: Int)] = [:]
                for d in drives {
                    for addr in [d.startAddress, d.endAddress] {
                        guard addr.count >= 2 else { continue }
                        var entry = map[addr] ?? (0, 0, 0)
                        entry.count += 1
                        entry.totalKm += d.distanceKm
                        entry.effSum += d.efficiency
                        map[addr] = entry
                    }
                }
                let newDests = map.map { name, v in
                    Destination(name: name, count: v.count, totalKm: Int(v.totalKm.rounded()), avgEff: v.effSum / v.count)
                }.sorted { $0.count > $1.count }

                var rMap: [Region: (count: Int, km: Double)] = [:]
                for d in drives {
                    let region = regionFromAddress(d.endAddress)
                    var entry = rMap[region] ?? (0, 0)
                    entry.count += 1
                    entry.km += d.distanceKm
                    rMap[region] = entry
                }
                let newRegions = rMap.map { RegionStat(region: $0.key, visitCount: $0.value.count, totalKm: $0.value.km) }
                    .sorted { $0.visitCount > $1.visitCount }

                cont.resume(returning: (newDests, newRegions))
            }
        }

        destinations = newDests
        regionStats = newRegions
    }
}
