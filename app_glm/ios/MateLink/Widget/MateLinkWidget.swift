import WidgetKit
import SwiftUI

// MARK: - Timeline Entry

struct MateLinkWidgetEntry: TimelineEntry {
    let date: Date
    let batteryLevel: Int
    let rangeKm: Int
    let state: String
    // T-102: status fields
    let locked: Bool
    let sentryMode: Bool
    let pluggedIn: Bool
    let insideTemp: Double?
    // T-103: charging fields
    let chargerVoltage: Int?
    let chargerActualCurrent: Int?
    let chargePhases: Int?
    let chargeLimitSoc: Int?
    // T-101: car image
    let carImageData: Data?
}

// MARK: - Provider

private enum WidgetConstants {
    static let refreshInterval: TimeInterval = 15 * 60  // 900s
}

struct Provider: TimelineProvider {
    func placeholder(in context: Context) -> MateLinkWidgetEntry {
        MateLinkWidgetEntry(
            date: Date(), batteryLevel: 78, rangeKm: 312, state: "online",
            locked: true, sentryMode: false, pluggedIn: false, insideTemp: 24.5,
            chargerVoltage: nil, chargerActualCurrent: nil, chargePhases: nil, chargeLimitSoc: 90,
            carImageData: nil
        )
    }

    func getSnapshot(in context: Context, completion: @escaping (MateLinkWidgetEntry) -> Void) {
        completion(placeholder(in: context))
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<MateLinkWidgetEntry>) -> Void) {
        let defaults = UserDefaults(suiteName: "group.com.teslamatelink")
        let entry = MateLinkWidgetEntry(
            date: Date(),
            batteryLevel: defaults?.integer(forKey: "widget_battery") ?? 78,
            rangeKm: defaults?.integer(forKey: "widget_range") ?? 312,
            state: defaults?.string(forKey: "widget_state") ?? "online",
            locked: defaults?.bool(forKey: "widget_locked") ?? true,
            sentryMode: defaults?.bool(forKey: "widget_sentry") ?? false,
            pluggedIn: defaults?.bool(forKey: "widget_pluggedIn") ?? false,
            insideTemp: defaults?.object(forKey: "widget_insideTemp") as? Double,
            chargerVoltage: defaults?.object(forKey: "widget_chargerVoltage") as? Int,
            chargerActualCurrent: defaults?.object(forKey: "widget_chargerActualCurrent") as? Int,
            chargePhases: defaults?.object(forKey: "widget_chargePhases") as? Int,
            chargeLimitSoc: defaults?.object(forKey: "widget_chargeLimitSoc") as? Int,
            carImageData: defaults?.data(forKey: "carImageData")
        )
        let timeline = Timeline(entries: [entry], policy: .after(Date().addingTimeInterval(WidgetConstants.refreshInterval)))
        completion(timeline)
    }
}

// MARK: - Widget Views

struct MateLinkWidgetEntryView: View {
    var entry: MateLinkWidgetEntry
    @Environment(\.widgetFamily) var family

    var body: some View {
        switch family {
        case .systemSmall:
            smallView
        case .systemMedium:
            mediumView
        case .accessoryCircular, .accessoryRectangular:
            lockScreenView
        default:
            mediumView
        }
    }

    // MARK: - T-101: Car Image Background

    @ViewBuilder
    private var carImageBackground: some View {
        if let data = entry.carImageData, let uiImage = UIImage(data: data) {
            Image(uiImage: uiImage)
                .resizable()
                .aspectRatio(contentMode: .fill)
                .opacity(0.15)
                .clipped()
        } else {
            Text("Tesla")
                .font(.system(size: 48, weight: .heavy))
                .foregroundColor(.secondary.opacity(0.08))
        }
    }

    // MARK: - T-102: Status Icon Row

    private var statusIconRow: some View {
        HStack(spacing: 10) {
            // Lock
            Image(systemName: entry.locked ? "lock.fill" : "lock.open.fill")
                .font(.caption2)
                .foregroundColor(entry.locked ? .green : .red)
            // Sentry
            Image(systemName: "shield.fill")
                .font(.caption2)
                .foregroundColor(entry.sentryMode ? .blue : .gray.opacity(0.3))
            // Plug
            Image(systemName: "bolt.car.fill")
                .font(.caption2)
                .foregroundColor(entry.pluggedIn ? .yellow : .gray.opacity(0.3))
            // Temperature
            if let temp = entry.insideTemp {
                HStack(spacing: 2) {
                    Image(systemName: "thermometer")
                        .font(.caption2)
                    Text("\(Int(temp))°")
                        .font(.system(size: 10, weight: .medium))
                }
                .foregroundColor(.orange)
            }
        }
    }

    // MARK: - T-103: Charging Detail Row

    @ViewBuilder
    private var chargingDetailRow: some View {
        if entry.state == "charging" {
            VStack(spacing: 3) {
                // "N V / N A / N phase"
                HStack(spacing: 4) {
                    if let v = entry.chargerVoltage, let a = entry.chargerActualCurrent {
                        Text("\(v) V")
                        Text("/")
                        Text("\(a) A")
                        if let phases = entry.chargePhases, phases > 1 {
                            Text("/")
                            Text("\(phases)\u{76F8}") // 相
                        }
                    }
                }
                .font(.system(size: 10, weight: .medium, design: .monospaced))
                .foregroundColor(.green)

                // Progress bar: batteryLevel / chargeLimitSoc
                if let limit = entry.chargeLimitSoc, limit > 0 {
                    GeometryReader { geo in
                        ZStack(alignment: .leading) {
                            Capsule()
                                .fill(Color.gray.opacity(0.2))
                            Capsule()
                                .fill(Color.green)
                                .frame(width: geo.size.width * CGFloat(entry.batteryLevel) / CGFloat(limit))
                        }
                    }
                    .frame(height: 4)
                }
            }
        }
    }

    // MARK: Small Widget

    private var smallView: some View {
        ZStack {
            carImageBackground
            VStack(spacing: 4) {
                Image(systemName: "car.fill")
                    .font(.title3)
                    .foregroundColor(.blue)
                Text("\(entry.batteryLevel)%")
                    .font(.system(size: 36, weight: .bold))
                    .foregroundColor(.primary)
                Text("\(entry.rangeKm) km")
                    .font(.caption)
                    .foregroundColor(.secondary)
                statusIconRow
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .containerBackground(.fill.tertiary, for: .widget)
    }

    // MARK: Medium Widget

    private var mediumView: some View {
        ZStack {
            carImageBackground
            HStack {
                VStack(alignment: .leading, spacing: 4) {
                    HStack(spacing: 6) {
                        Image(systemName: "car.fill")
                            .foregroundColor(.blue)
                        Text("MateLink")
                            .font(.caption)
                            .foregroundColor(.secondary)
                    }
                    Text("\(entry.batteryLevel)%")
                        .font(.title.bold())
                        .foregroundColor(.primary)
                    Text("\(entry.rangeKm) km range")
                        .font(.caption)
                        .foregroundColor(.secondary)
                    statusIconRow
                    chargingDetailRow
                }
                Spacer()
                VStack(alignment: .trailing, spacing: 4) {
                    Text(statusLabel)
                        .font(.caption)
                        .padding(.horizontal, 8)
                        .padding(.vertical, 4)
                        .background(statusColor.opacity(0.2))
                        .foregroundColor(statusColor)
                        .clipShape(Capsule())
                    Spacer()
                }
            }
            .padding()
        }
        .containerBackground(.fill.tertiary, for: .widget)
    }

    // MARK: - T-104: Lock Screen View

    private var lockScreenView: some View {
        VStack(spacing: 2) {
            // Battery circle
            ZStack {
                Circle()
                    .stroke(Color.gray.opacity(0.3), lineWidth: 4)
                Circle()
                    .trim(from: 0, to: CGFloat(entry.batteryLevel) / 100.0)
                    .stroke(batteryColor, style: StrokeStyle(lineWidth: 4, lineCap: .round))
                    .rotationEffect(.degrees(-90))
                Text("\(entry.batteryLevel)%")
                    .font(.system(size: 14, weight: .bold, design: .rounded))
            }
            .frame(width: 44, height: 44)

            Text("\(entry.rangeKm) km")
                .font(.system(size: 11, weight: .medium))

            Text(statusLabel)
                .font(.system(size: 9, weight: .medium))
                .foregroundColor(.secondary)
        }
    }

    private var batteryColor: Color {
        if entry.batteryLevel >= 60 { return .green }
        if entry.batteryLevel >= 20 { return .yellow }
        return .red
    }

    private var statusColor: Color {
        switch entry.state {
        case "online", "driving": return .blue
        case "charging": return .orange
        case "asleep": return .gray
        default: return .secondary
        }
    }

    private var statusLabel: String {
        entry.state.capitalized
    }
}

// MARK: - Widget Definition

struct MateLinkWidget: Widget {
    let kind = "MateLinkWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: Provider()) { entry in
            MateLinkWidgetEntryView(entry: entry)
        }
        .configurationDisplayName("MateLink")
        .description("Tesla battery & range at a glance")
        .supportedFamilies([.systemSmall, .systemMedium, .accessoryCircular, .accessoryRectangular])
    }
}

// MARK: - Preview

#if DEBUG
#Preview("Small", as: .systemSmall) {
    MateLinkWidget()
} timeline: {
    MateLinkWidgetEntry(
        date: .now, batteryLevel: 78, rangeKm: 312, state: "online",
        locked: true, sentryMode: true, pluggedIn: false, insideTemp: 24.5,
        chargerVoltage: nil, chargerActualCurrent: nil, chargePhases: nil, chargeLimitSoc: 90,
        carImageData: nil
    )
}

#Preview("Medium", as: .systemMedium) {
    MateLinkWidget()
} timeline: {
    MateLinkWidgetEntry(
        date: .now, batteryLevel: 45, rangeKm: 180, state: "charging",
        locked: false, sentryMode: false, pluggedIn: true, insideTemp: 31.0,
        chargerVoltage: 230, chargerActualCurrent: 16, chargePhases: 3, chargeLimitSoc: 90,
        carImageData: nil
    )
}

#Preview("Lock Screen", as: .accessoryRectangular) {
    MateLinkWidget()
} timeline: {
    MateLinkWidgetEntry(
        date: .now, batteryLevel: 65, rangeKm: 250, state: "online",
        locked: true, sentryMode: false, pluggedIn: false, insideTemp: nil,
        chargerVoltage: nil, chargerActualCurrent: nil, chargePhases: nil, chargeLimitSoc: 90,
        carImageData: nil
    )
}
#endif
