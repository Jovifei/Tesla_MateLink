import Foundation

enum UnitSystem: String, Codable {
    case metric, imperial
}

enum UnitFormatter {
    static func formatDistance(_ km: Double, system: UnitSystem) -> String {
        switch system {
        case .metric: return "\(Int(km.rounded())) km"
        case .imperial: return "\(Int((km * 0.621371).rounded())) mi"
        }
    }

    static func formatTemperature(_ celsius: Double, system: UnitSystem) -> String {
        switch system {
        case .metric: return String(format: "%.1f°C", celsius)
        case .imperial: return String(format: "%.1f°F", celsius * 9.0/5.0 + 32.0)
        }
    }

    static func formatPressure(_ bar: Double, system: UnitSystem) -> String {
        switch system {
        case .metric: return String(format: "%.1f bar", bar)
        case .imperial: return String(format: "%.1f psi", bar * 14.5038)
        }
    }

    static func formatRange(_ km: Double, system: UnitSystem) -> String {
        formatDistance(km, system: system)
    }

    static func distanceUnit(_ system: UnitSystem) -> String {
        system == .metric ? "km" : "mi"
    }

    static func temperatureUnit(_ system: UnitSystem) -> String {
        system == .metric ? "°C" : "°F"
    }

    static func pressureUnit(_ system: UnitSystem) -> String {
        system == .metric ? "bar" : "psi"
    }
}
