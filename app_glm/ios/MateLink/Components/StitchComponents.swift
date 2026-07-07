import SwiftUI

// MARK: - Stitch Card

struct StitchCard<Content: View>: View {
    @ViewBuilder let content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 0) {
            content
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(24)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(
            RoundedRectangle(cornerRadius: 8)
                .stroke(StitchColors.border, lineWidth: 1)
        )
    }
}

// MARK: - Stitch Label (section header)

struct StitchLabel: View {
    let text: String
    let color: Color

    init(_ text: String, color: Color = StitchColors.onSurfaceVariant) {
        self.text = text
        self.color = color
    }

    var body: some View {
        Text(text)
            .font(StitchFont.labelCaps())
            .foregroundColor(color)
    }
}

// MARK: - Stitch Data Column

struct StitchDataColumn: View {
    let label: String
    let value: String
    var alignment: HorizontalAlignment = .leading

    var body: some View {
        VStack(alignment: alignment, spacing: 4) {
            Text(label)
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
            Text(value)
                .font(StitchFont.dataMd())
                .foregroundColor(StitchColors.onSurface)
        }
    }
}

// MARK: - Stitch Data Row

struct StitchDataRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label)
                .font(StitchFont.labelCaps())
                .foregroundColor(StitchColors.onSurfaceVariant)
            Spacer()
            Text(value)
                .font(StitchFont.bodyLg())
                .foregroundColor(StitchColors.onSurface)
        }
    }
}

// MARK: - Stitch Status Chip

struct StitchStatusChip: View {
    let text: String
    var isOnline: Bool = false
    var isCharging: Bool = false

    private var backgroundColor: Color {
        if isOnline { return StitchColors.statusOnlineBg }
        if isCharging { return StitchColors.statusChargingBg }
        return StitchColors.statusOfflineBg
    }

    private var textColor: Color {
        if isOnline { return StitchColors.statusOnline }
        if isCharging { return StitchColors.statusCharging }
        return StitchColors.statusOffline
    }

    var body: some View {
        Text(text)
            .font(.system(size: 11, weight: .bold))
            .foregroundColor(textColor)
            .padding(.horizontal, 10)
            .padding(.vertical, 4)
            .background(backgroundColor)
            .clipShape(Capsule())
    }
}
