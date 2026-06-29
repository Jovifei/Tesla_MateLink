import Foundation

enum ISO8601Parser {
    private static let fractionalFormatter: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withInternetDateTime, .withFractionalSeconds]
        return f
    }()

    private static let basicFormatter: ISO8601DateFormatter = {
        let f = ISO8601DateFormatter()
        f.formatOptions = [.withInternetDateTime]
        return f
    }()

    static func parse(_ string: String) -> Date? {
        fractionalFormatter.date(from: string) ?? basicFormatter.date(from: string)
    }

    static func todayString() -> String {
        String(fractionalFormatter.string(from: Date()).prefix(10))
    }
}
