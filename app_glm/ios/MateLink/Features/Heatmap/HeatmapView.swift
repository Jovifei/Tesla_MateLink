import SwiftUI

// MARK: - Heatmap-specific Stitch tokens
// Cell intensity scale mirrors the Stitch legend (少 → 多).

private let CellEmpty = Color(hex: "E5E2E1")  // surface-variant
private let CellLow = Color(hex: "5F5E5E")    // surface-tint
private let CellMid = Color(hex: "C8C6C5")    // primary-fixed-dim
private let CellHigh = Color(hex: "000000")   // primary
private let SparkStroke = Color(hex: "171717")

private let gridCols = 13
private let gridRows = 7

// MARK: - Mock data (matches Stitch design spec; data layer unchanged)
// TODO(data): wire heatmap grid + routes to driving stats via repository.

private struct RouteRank: Identifiable {
    let id = UUID()
    let from: String
    let to: String
    let count: String
    let spark: [CGPoint] // points in 0..100 x, 0..20 y space
}

private let routes: [RouteRank] = [
    RouteRank(
        from: "Home", to: "Office", count: "42 次",
        spark: [CGPoint(x: 0, y: 15), CGPoint(x: 20, y: 10), CGPoint(x: 40, y: 18),
                CGPoint(x: 60, y: 5), CGPoint(x: 80, y: 12), CGPoint(x: 100, y: 8)]
    ),
    RouteRank(
        from: "Office", to: "Gym", count: "18 次",
        spark: [CGPoint(x: 0, y: 10), CGPoint(x: 20, y: 15), CGPoint(x: 40, y: 8),
                CGPoint(x: 60, y: 18), CGPoint(x: 80, y: 10), CGPoint(x: 100, y: 5)]
    ),
    RouteRank(
        from: "Home", to: "Supermarket", count: "12 次",
        spark: [CGPoint(x: 0, y: 12), CGPoint(x: 20, y: 18), CGPoint(x: 40, y: 5),
                CGPoint(x: 60, y: 15), CGPoint(x: 80, y: 8), CGPoint(x: 100, y: 12)]
    )
]

private let segments = ["30天", "90天", "全年"]

// Deterministic pseudo-intensity grid 0..3 (matches Stitch density feel).
private let gridData: [[Int]] = (0..<gridCols).map { col in
    (0..<gridRows).map { row in
        let v = (col * 7 + row * 3) % 11
        switch v {
        case 0...2: return 0
        case 3...5: return 1
        case 6...8: return 2
        default: return 3
        }
    }
}

// MARK: - HeatmapView

struct HeatmapView: View {
    @State private var selectedSegment = 1

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 32) {
                segmentedControl
                heatmapCard
                dataCardsRow
                routeRankingSection
            }
            .padding(.horizontal, 24)
            .padding(.top, 24)
            .padding(.bottom, 128)
        }
        .background(StitchColors.background)
        .navigationTitle("热力图")
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .topBarTrailing) {
                Image(systemName: "calendar")
                    .foregroundColor(StitchColors.onSurface)
            }
        }
    }

    // MARK: - Segmented Control (30天 / 90天 / 全年)

    private var segmentedControl: some View {
        HStack(spacing: 0) {
            ForEach(Array(segments.enumerated()), id: \.offset) { index, label in
                let active = index == selectedSegment
                Button {
                    selectedSegment = index
                } label: {
                    Text(label)
                        .font(active ? .system(size: 14, weight: .bold) : StitchFont.bodySm())
                        .foregroundColor(active ? StitchColors.primary : StitchColors.onSurfaceVariant)
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 12)
                        .background(active ? StitchColors.surfaceContainerLow : StitchColors.white)
                }
                if index < segments.count - 1 {
                    Rectangle()
                        .fill(StitchColors.surfaceContainerHigh)
                        .frame(width: 1)
                }
            }
        }
        .frame(height: 44)
        .clipShape(RoundedRectangle(cornerRadius: 4))
        .overlay(
            RoundedRectangle(cornerRadius: 4)
                .stroke(StitchColors.surfaceContainerHigh, lineWidth: 1)
        )
    }

    // MARK: - Main Heatmap Card

    private var heatmapCard: some View {
        StitchCard {
            Text("驾驶热力分布")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(StitchColors.primary)
                .padding(.bottom, 16)

            // Contribution grid: 13 columns × 7 rows, 16pt cells, 4pt gaps.
            ScrollView(.horizontal, showsIndicators: false) {
                HStack(alignment: .top, spacing: 4) {
                    ForEach(0..<gridCols, id: \.self) { col in
                        VStack(spacing: 4) {
                            ForEach(0..<gridRows, id: \.self) { row in
                                RoundedRectangle(cornerRadius: 2)
                                    .fill(levelColor(gridData[col][row]))
                                    .frame(width: 16, height: 16)
                            }
                        }
                    }
                }
                .padding(.bottom, 4)
            }

            // Legend: 少 [scale] 多
            HStack {
                Text("少")
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                Spacer()
                HStack(spacing: 4) {
                    ForEach([CellEmpty, CellLow, CellMid, CellHigh], id: \.self) { c in
                        RoundedRectangle(cornerRadius: 2)
                            .fill(c)
                            .frame(width: 16, height: 16)
                    }
                }
                Spacer()
                Text("多")
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            .padding(.top, 16)
        }
    }

    private func levelColor(_ level: Int) -> Color {
        switch level {
        case 0: return CellEmpty
        case 1: return CellLow
        case 2: return CellMid
        default: return CellHigh
        }
    }

    // MARK: - Data Cards (1×2): 高频时段 / 最常目的地

    private var dataCardsRow: some View {
        HStack(spacing: 16) {
            DataCard(icon: "clock", label: "高频时段", value: "08:00 - 10:00", valueMono: true)
            DataCard(icon: "location.fill", label: "最常目的地", value: "Office", valueMono: false)
        }
    }

    // MARK: - Route Ranking (常用路线排行)

    private var routeRankingSection: some View {
        VStack(alignment: .leading, spacing: 0) {
            Text("常用路线排行")
                .font(.system(size: 16, weight: .bold))
                .foregroundColor(StitchColors.primary)
                .padding(.bottom, 16)

            VStack(spacing: 0) {
                ForEach(Array(routes.enumerated()), id: \.element.id) { index, route in
                    RouteRow(route: route)
                    if index < routes.count - 1 {
                        Rectangle()
                            .fill(StitchColors.surfaceContainerHigh)
                            .frame(height: 1)
                    }
                }
            }
            .background(StitchColors.white)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .overlay(
                RoundedRectangle(cornerRadius: 8)
                    .stroke(StitchColors.border, lineWidth: 1)
            )
        }
    }
}

// MARK: - Data Card

private struct DataCard: View {
    let icon: String
    let label: String
    let value: String
    let valueMono: Bool

    var body: some View {
        StitchCard {
            Image(systemName: icon)
                .font(.system(size: 24))
                .foregroundColor(StitchColors.primary)
                .padding(.bottom, 8)
            Text(label)
                .font(StitchFont.bodySm())
                .foregroundColor(StitchColors.onSurfaceVariant)
                .padding(.bottom, 4)
            if valueMono {
                Text(value)
                    .font(StitchFont.dataLg())
                    .foregroundColor(StitchColors.primary)
            } else {
                Text(value)
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(StitchColors.primary)
            }
        }
    }
}

// MARK: - Route Row

private struct RouteRow: View {
    let route: RouteRank

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 4) {
                HStack(spacing: 8) {
                    Text(route.from)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(StitchColors.primary)
                    Image(systemName: "arrow.right")
                        .font(.system(size: 12))
                        .foregroundColor(StitchColors.onSurfaceVariant)
                    Text(route.to)
                        .font(.system(size: 16, weight: .bold))
                        .foregroundColor(StitchColors.primary)
                }
                Text(route.count)
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
            Spacer()
            Sparkline(points: route.spark)
                .frame(width: 96, height: 32)
                .background(StitchColors.surfaceContainerHigh)
                .clipShape(RoundedRectangle(cornerRadius: 4))
        }
        .padding(16)
    }
}

// MARK: - Sparkline

private struct Sparkline: View {
    let points: [CGPoint]

    var body: some View {
        GeometryReader { geo in
            Path { path in
                guard points.count >= 2 else { return }
                let sx = geo.size.width / 100
                let sy = geo.size.height / 20
                path.move(to: CGPoint(x: points[0].x * sx, y: points[0].y * sy))
                for p in points.dropFirst() {
                    path.addLine(to: CGPoint(x: p.x * sx, y: p.y * sy))
                }
            }
            .stroke(SparkStroke, style: StrokeStyle(lineWidth: 2, lineCap: .round))
        }
    }
}

// MARK: - Preview

#Preview("Heatmap") {
    NavigationStack {
        HeatmapView()
    }
}
