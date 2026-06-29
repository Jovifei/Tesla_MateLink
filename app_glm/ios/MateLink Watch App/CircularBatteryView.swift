import SwiftUI

// T-202: Circular battery gauge for Watch

struct CircularBatteryView: View {
    let level: Int

    private var ringColor: Color {
        if level > 60 { return .green }
        if level > 20 { return .yellow }
        return .red
    }

    var body: some View {
        ZStack {
            Circle()
                .stroke(Color.gray.opacity(0.3), lineWidth: 8)
            Circle()
                .trim(from: 0, to: CGFloat(level) / 100)
                .stroke(
                    ringColor,
                    style: StrokeStyle(lineWidth: 8, lineCap: .round)
                )
                .rotationEffect(.degrees(-90))
                .animation(.easeInOut(duration: 0.6), value: level)
            Text("\(level)%")
                .font(.system(size: 16, weight: .bold, design: .rounded))
                .foregroundColor(.white)
        }
        .frame(width: 60, height: 60)
    }
}

#Preview {
    ZStack {
        Color.black
        CircularBatteryView(level: 72)
    }
}
