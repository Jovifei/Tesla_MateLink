import SwiftUI

enum CarColor: String, CaseIterable {
    case deepBlue, redMultiCoat, pearlWhite, midnightSilver, solidBlack, stealthGrey
    var accent: Color {
        switch self {
        case .deepBlue: return Color(hex: "1E3A8A")
        case .redMultiCoat: return Color(hex: "B91C1C")
        case .pearlWhite, .midnightSilver, .stealthGrey: return Color(hex: "4B5563")
        case .solidBlack: return Color(hex: "18181B")
        }
    }
    static func from(_ colorName: String) -> CarColor {
        switch colorName.lowercased() {
        case "deepblue": return .deepBlue; case "redmulticoat": return .redMultiCoat
        case "pearlwhite": return .pearlWhite; case "midnightsilver": return .midnightSilver
        case "solidblack": return .solidBlack; case "stealthgrey": return .stealthGrey
        default: return .midnightSilver
        }
    }
}

enum StateColor {
    static func forState(_ state: CarState) -> Color {
        switch state { case .online: return .green; case .driving: return .blue; case .charging: return .orange; case .asleep: return .gray; case .offline: return Color(hex: "616161") }
    }
    static func label(_ state: CarState) -> String {
        switch state { case .online: return "Online"; case .driving: return "Driving"; case .charging: return "Charging"; case .asleep: return "Asleep"; case .offline: return "Offline" }
    }
}

extension Color {
    init(hex: String) {
        let s = hex.trimmingCharacters(in: .alphanumerics.inverted)
        let v = UInt64(Int(s, radix: 16) ?? 0)
        self.init(red: Double((v>>16)&0xFF)/255, green: Double((v>>8)&0xFF)/255, blue: Double(v&0xFF)/255)
    }
}

// MARK: - Stitch Colors (White-Minimal Design System)

enum StitchColors {
    // Background & Surface
    static let background       = Color(hex: "fdf8f8")
    static let surface          = Color(hex: "fdf8f8")

    // Text
    static let onSurface        = Color(hex: "1c1b1b")
    static let onSurfaceVariant = Color(hex: "444748")

    // Outline & Borders
    static let outline          = Color(hex: "747878")
    static let outlineVariant   = Color(hex: "c4c7c7")
    static let border           = Color(hex: "E5E5E5")

    // Brand
    static let primary          = Color(hex: "000000")
    static let accent           = Color(hex: "A16207")  // gold for active tab
    static let secondary        = Color(hex: "895200")

    // Feedback
    static let error            = Color(hex: "ba1a1a")

    // Neutral
    static let white            = Color(hex: "ffffff")

    // Status
    static let statusOnline      = Color(hex: "059669")
    static let statusOnlineBg    = Color(hex: "d1fae5")
    static let statusOffline     = Color(hex: "747878")
    static let statusOfflineBg   = Color(hex: "f3f4f6")
    static let statusCharging    = Color(hex: "D97706")
    static let statusChargingBg  = Color(hex: "fef3c7")
    static let statusError       = Color(hex: "ba1a1a")
    static let statusErrorBg     = Color(hex: "ffdad6")
}

// MARK: - Stitch Typography

// TODO: bundle JetBrainsMono.ttf and use .custom("JetBrainsMono-Medium", size:) once added
struct StitchFont {
    static func displayLg() -> Font { .system(size: 32, weight: .bold) }
    static func headlineMd() -> Font { .system(size: 24, weight: .semibold) }
    static func bodyLg() -> Font { .system(size: 16, weight: .regular) }
    static func bodySm() -> Font { .system(size: 14, weight: .regular) }
    static func labelCaps() -> Font { .system(size: 12, weight: .bold) }
    static func dataLg() -> Font { .system(size: 24, weight: .medium, design: .monospaced) }
    static func dataMd() -> Font { .system(size: 16, weight: .medium, design: .monospaced) }
}
