import SwiftUI

// MARK: - Reference Date for Normalizing Hour:Minute

/// A fixed reference date used to store time-of-day values (hour:minute only, date component is ignored).
private let referenceDate: Date = {
    Calendar.current.date(from: DateComponents(year: 2000, month: 1, day: 1))!
}()

/// Create a Date on the reference date representing the given hour and minute.
private func hm(_ hour: Int, _ minute: Int) -> Date {
    Calendar.current.date(bySettingHour: hour, minute: minute, second: 0, of: referenceDate)!
}

// MARK: - TimeRangeConfig

/// A single editable time range with start and end times (hour:minute only).
struct TimeRangeConfig: Codable, Equatable, Identifiable {
    var id = UUID()
    var startTime: Date
    var endTime: Date

    /// Human-readable representation, e.g. "10:00-15:00".
    var display: String {
        let f = DateFormatter()
        f.dateFormat = "HH:mm"
        f.timeZone = TimeZone(identifier: "Asia/Shanghai")
        return "\(f.string(from: startTime))-\(f.string(from: endTime))"
    }

    /// Duration in hours (handles cross-midnight ranges).
    var hours: Double {
        let cal = Calendar.current
        let sh = cal.component(.hour, from: startTime) + cal.component(.minute, from: startTime) / 60
        let eh = cal.component(.hour, from: endTime) + cal.component(.minute, from: endTime) / 60
        return eh > sh ? eh - sh : (24 - sh) + eh
    }
}

// MARK: - TariffConfig

/// Full time-of-use electricity pricing configuration, persisted as JSON in UserDefaults.
struct TariffConfig: Codable {
    enum CodingKeys: String, CodingKey {
        case isEnabled = "is_enabled"
        case peakRanges = "peak_ranges"
        case peakPrice = "peak_price"
        case flatRanges = "flat_ranges"
        case flatPrice = "flat_price"
        case valleyRanges = "valley_ranges"
        case valleyPrice = "valley_price"
    }

    var isEnabled = true

    // Peak (峰) — matches TS DEFAULT_TARIFF_PERIODS: [9,12) + [17,22)
    var peakRanges: [TimeRangeConfig] = [
        TimeRangeConfig(startTime: hm(9, 0), endTime: hm(12, 0)),
        TimeRangeConfig(startTime: hm(17, 0), endTime: hm(22, 0)),
    ]
    var peakPrice: Double = 1.0

    // Flat (平) — [7,9) + [12,17) + [22,24)
    var flatRanges: [TimeRangeConfig] = [
        TimeRangeConfig(startTime: hm(7, 0), endTime: hm(9, 0)),
        TimeRangeConfig(startTime: hm(12, 0), endTime: hm(17, 0)),
        TimeRangeConfig(startTime: hm(22, 0), endTime: hm(23, 0)),
    ]
    var flatPrice: Double = 0.7

    // Valley (谷) — [0,7) (no cross-midnight; flat covers [22,24) via else-default)
    var valleyRanges: [TimeRangeConfig] = [
        TimeRangeConfig(startTime: hm(0, 0), endTime: hm(7, 0)),
    ]
    var valleyPrice: Double = 0.3

    /// Returns the TOU price for a given hour of day (0-23).
    /// Falls back to flatPrice if no range matches.
    func priceForHour(_ hour: Int) -> Double {
        for range in peakRanges {
            let sh = Calendar.current.component(.hour, from: range.startTime)
            let eh = Calendar.current.component(.hour, from: range.endTime)
            if sh <= eh ? (sh..<eh).contains(hour) : hour >= sh || hour < eh {
                return peakPrice
            }
        }
        for range in valleyRanges {
            let sh = Calendar.current.component(.hour, from: range.startTime)
            let eh = Calendar.current.component(.hour, from: range.endTime)
            if sh <= eh ? (sh..<eh).contains(hour) : hour >= sh || hour < eh {
                return valleyPrice
            }
        }
        return flatPrice
    }
}

// MARK: - TariffConfigView

struct TariffConfigView: View {
    // MARK: Persistence

    /// JSON-encoded TariffConfig stored in UserDefaults.
    @AppStorage("tariffConfigJSON") private var storage: String = ""

    /// In-memory decoded config.
    @State private var config: TariffConfig = {
        let stored = UserDefaults.standard.string(forKey: "tariffConfigJSON") ?? ""
        guard let data = stored.data(using: .utf8),
              let decoded = try? JSONDecoder().decode(TariffConfig.self, from: data)
        else { return TariffConfig() }
        return decoded
    }()

    /// Encode current config and persist to UserDefaults.
    private func save() {
        guard let data = try? JSONEncoder().encode(config),
              let json = String(data: data, encoding: .utf8)
        else { return }
        storage = json
    }

    // MARK: Body

    var body: some View {
        List {
            enableSection
            if config.isEnabled {
                peakSection
                flatSection
                valleySection
                savingsPreview
                resetButton
            }
        }
        .navigationTitle(Text("tariff.config"))
        .navigationBarTitleDisplayMode(.inline)
    }

    // MARK: - Sections

    private var enableSection: some View {
        Section {
            Toggle(isOn: Binding(
                get: { config.isEnabled },
                set: { config.isEnabled = $0; save() }
            )) {
                Label(NSLocalizedString("tariff.toggle", comment: ""), systemImage: "clock.badge.checkmark")
            }
        } footer: {
            if config.isEnabled {
                Text(NSLocalizedString("tariff.toggle.footer", comment: ""))
            }
        }
    }

    // MARK: Peak

    private var peakSection: some View {
        Section {
            periodPriceRow(label: NSLocalizedString("tariff.peak.label", comment: ""), color: .red, price: Binding(
                get: { config.peakPrice },
                set: { config.peakPrice = $0; save() }
            ))
            ForEach(Array(config.peakRanges.enumerated()), id: \.element.id) { idx, range in
                timeRangeRow(range: Binding(
                    get: { config.peakRanges[idx] },
                    set: { config.peakRanges[idx] = $0; save() }
                ), onDelete: {
                    config.peakRanges.remove(at: idx); save()
                })
            }
            Button(action: {
                config.peakRanges.append(TimeRangeConfig(startTime: hm(0, 0), endTime: hm(1, 0)))
                save()
            }) {
                Label(NSLocalizedString("tariff.peak.add", comment: ""), systemImage: "plus.circle")
            }
        } header: {
            periodHeader(label: NSLocalizedString("tariff.peak.header", comment: ""), color: .red, systemImage: "sun.max.fill")
        }
    }

    // MARK: Flat

    private var flatSection: some View {
        Section {
            periodPriceRow(label: NSLocalizedString("tariff.flat.label", comment: ""), color: .orange, price: Binding(
                get: { config.flatPrice },
                set: { config.flatPrice = $0; save() }
            ))
            ForEach(Array(config.flatRanges.enumerated()), id: \.element.id) { idx, range in
                timeRangeRow(range: Binding(
                    get: { config.flatRanges[idx] },
                    set: { config.flatRanges[idx] = $0; save() }
                ), onDelete: {
                    config.flatRanges.remove(at: idx); save()
                })
            }
            Button(action: {
                config.flatRanges.append(TimeRangeConfig(startTime: hm(0, 0), endTime: hm(1, 0)))
                save()
            }) {
                Label(NSLocalizedString("tariff.flat.add", comment: ""), systemImage: "plus.circle")
            }
        } header: {
            periodHeader(label: NSLocalizedString("tariff.flat.header", comment: ""), color: .orange, systemImage: "sun.min.fill")
        }
    }

    // MARK: Valley

    private var valleySection: some View {
        Section {
            periodPriceRow(label: NSLocalizedString("tariff.valley.label", comment: ""), color: .blue, price: Binding(
                get: { config.valleyPrice },
                set: { config.valleyPrice = $0; save() }
            ))
            ForEach(Array(config.valleyRanges.enumerated()), id: \.element.id) { idx, range in
                timeRangeRow(range: Binding(
                    get: { config.valleyRanges[idx] },
                    set: { config.valleyRanges[idx] = $0; save() }
                ), onDelete: {
                    config.valleyRanges.remove(at: idx); save()
                })
            }
            Button(action: {
                config.valleyRanges.append(TimeRangeConfig(startTime: hm(0, 0), endTime: hm(1, 0)))
                save()
            }) {
                Label(NSLocalizedString("tariff.valley.add", comment: ""), systemImage: "plus.circle")
            }
        } header: {
            periodHeader(label: NSLocalizedString("tariff.valley.header", comment: ""), color: .blue, systemImage: "moon.stars.fill")
        }
    }

    // MARK: - Savings Preview

    private var savingsPreview: some View {
        let totalKwh: Double = 50
        let savings = computeSavings(totalKwh: totalKwh)
        let isSaving = savings > 0

        return Section {
            VStack(spacing: 12) {
                // Savings amount
                HStack(alignment: .firstBaseline, spacing: 4) {
                    Text(isSaving ? NSLocalizedString("tariff.savings.label", comment: "") : NSLocalizedString("tariff.cost.label", comment: ""))
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                    Text("\u{00A5}\(abs(savings).formatted(.number.precision(.fractionLength(2))))")
                        .font(.title.weight(.bold))
                        .foregroundColor(isSaving ? .green : .red)
                }

                // Assumption breakdown
                VStack(spacing: 4) {
                    HStack {
                        Text(String(format: NSLocalizedString("tariff.assumption", comment: ""), Int(totalKwh)))
                            .font(.caption)
                        Spacer()
                    }
                    ForEach(periodBreakdown(), id: \.label) { row in
                        HStack {
                            Circle().fill(row.color).frame(width: 8, height: 8)
                            Text(row.label)
                                .font(.caption2)
                                .foregroundColor(.secondary)
                            Spacer()
                            Text(row.detail)
                                .font(.caption2)
                                .foregroundColor(.secondary)
                        }
                    }
                }
                .padding(10)
                .background(Color(.systemGray6))
                .clipShape(RoundedRectangle(cornerRadius: 8))
            }
            .padding(.vertical, 4)
            .frame(maxWidth: .infinity)
        } header: {
            Label(NSLocalizedString("tariff.preview.header", comment: ""), systemImage: "yensign.circle")
        }
    }

    // MARK: - Reset

    private var resetButton: some View {
        Section {
            Button(role: .destructive, action: {
                config = TariffConfig()
                save()
            }) {
                Label(NSLocalizedString("tariff.reset", comment: ""), systemImage: "arrow.counterclockwise")
            }
        }
    }

    // MARK: - Reusable Row Builders

    private func periodHeader(label: String, color: Color, systemImage: String) -> some View {
        HStack(spacing: 6) {
            Image(systemName: systemImage)
                .foregroundColor(color)
            Text(label)
                .font(.headline)
        }
    }

    private func periodPriceRow(label: String, color: Color, price: Binding<Double>) -> some View {
        HStack {
            Text(NSLocalizedString("tariff.unit.price", comment: ""))
                .foregroundColor(.secondary)
            Spacer()
            HStack(spacing: 2) {
                Text("\u{00A5}")
                    .foregroundColor(.secondary)
                TextField("", value: price, format: .number.precision(.fractionLength(4)))
                    .keyboardType(.decimalPad)
                    .multilineTextAlignment(.trailing)
                    .frame(width: 80)
                Text("/kWh")
                    .foregroundColor(.secondary)
            }
        }
    }

    private func timeRangeRow(range: Binding<TimeRangeConfig>, onDelete: @escaping () -> Void) -> some View {
        HStack(spacing: 8) {
            DatePicker("", selection: range.startTime, displayedComponents: .hourAndMinute)
                .labelsHidden()
                .environment(\.locale, .current)
                .onChange(of: range.wrappedValue.startTime) { _ in save() }

            Text("→")
                .foregroundColor(.secondary)

            DatePicker("", selection: range.endTime, displayedComponents: .hourAndMinute)
                .labelsHidden()
                .environment(\.locale, .current)
                .onChange(of: range.wrappedValue.endTime) { _ in save() }

            Spacer(minLength: 4)

            Button(role: .destructive, action: onDelete) {
                Image(systemName: "trash")
                    .font(.caption)
            }
            .buttonStyle(.borderless)
        }
    }

    // MARK: - Helpers

    /// Computes the approximate monetary savings when using TOU pricing vs. a flat rate.
    /// Comparison flat rate is the simple average of the three configured prices.
    private func computeSavings(totalKwh: Double) -> Double {
        let peakH = config.peakRanges.reduce(0) { $0 + $1.hours }
        let flatH = config.flatRanges.reduce(0) { $0 + $1.hours }
        let valleyH = config.valleyRanges.reduce(0) { $0 + $1.hours }
        let totalH = peakH + flatH + valleyH
        guard totalH > 0 else { return 0 }

        let touCost = totalKwh * (
            peakH * config.peakPrice +
            flatH * config.flatPrice +
            valleyH * config.valleyPrice
        ) / totalH

        let flatRate = (config.peakPrice + config.flatPrice + config.valleyPrice) / 3.0
        let noTouCost = totalKwh * flatRate

        return max(noTouCost - touCost, 0)
    }

    private struct PeriodBreakdownRow: Identifiable {
        let id = UUID()
        let label: String
        let color: Color
        let detail: String
    }

    private func periodBreakdown() -> [PeriodBreakdownRow] {
        let peakH = config.peakRanges.reduce(0) { $0 + $1.hours }
        let flatH = config.flatRanges.reduce(0) { $0 + $1.hours }
        let valleyH = config.valleyRanges.reduce(0) { $0 + $1.hours }
        let totalH = peakH + flatH + valleyH
        guard totalH > 0 else { return [] }

        return [
            PeriodBreakdownRow(
                label: String(format: NSLocalizedString("tariff.period.price", comment: ""), "\u{00A5}\(config.peakPrice.formatted(.number.precision(.fractionLength(2))))"),
                color: .red,
                detail: String(format: NSLocalizedString("tariff.period.pct_hours", comment: ""), Int(round(peakH / totalH * 100)), Int(peakH))
            ),
            PeriodBreakdownRow(
                label: String(format: NSLocalizedString("tariff.period.price", comment: ""), "\u{00A5}\(config.flatPrice.formatted(.number.precision(.fractionLength(2))))"),
                color: .orange,
                detail: String(format: NSLocalizedString("tariff.period.pct_hours", comment: ""), Int(round(flatH / totalH * 100)), Int(flatH))
            ),
            PeriodBreakdownRow(
                label: String(format: NSLocalizedString("tariff.period.price", comment: ""), "\u{00A5}\(config.valleyPrice.formatted(.number.precision(.fractionLength(2))))"),
                color: .blue,
                detail: String(format: NSLocalizedString("tariff.period.pct_hours", comment: ""), Int(round(valleyH / totalH * 100)), Int(valleyH))
            ),
        ]
    }
}

// MARK: - Preview

#Preview {
    NavigationStack {
        TariffConfigView()
    }
}
