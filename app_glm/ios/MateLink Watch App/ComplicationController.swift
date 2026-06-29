import ClockKit
import SwiftUI

// T-203: ClockKit complication for watch face
// Shows battery level and range on the watch face

class ComplicationController: NSObject, CLKComplicationDataSource {

    // MARK: - Timeline Configuration

    func getSupported Families(for complication: CLKComplication, withHandler handler: @escaping (Set<CLKComplicationFamily>) -> Void) {
        handler([.graphicCircular, .graphicRectangular, .modularSmall, .modularLarge])
    }

    // MARK: - Timeline Population

    func getCurrentTimelineEntry(for complication: CLKComplication, withHandler handler: @escaping (CLKComplicationTimelineEntry?) -> Void) {
        let battery = UserDefaults(suiteName: "group.com.teslamatelink")?.integer(forKey: "batteryLevel") ?? 0
        let range = UserDefaults(suiteName: "group.com.teslamatelink")?.integer(forKey: "range") ?? 0

        let entry: CLKComplicationTimelineEntry?
        switch complication.family {
        case .graphicCircular:
            let template = CLKComplicationTemplateGraphicCircularView(
                CircularComplicationView(battery: battery)
            )
            entry = CLKComplicationTimelineEntry(date: Date(), complicationTemplate: template)

        case .graphicRectangular:
            let template = CLKComplicationTemplateGraphicRectangularFullView(
                RectangularComplicationView(battery: battery, range: range)
            )
            entry = CLKComplicationTimelineEntry(date: Date(), complicationTemplate: template)

        case .modularSmall:
            let template = CLKComplicationTemplateModularSmallRingText()
            template.textProvider = CLKSimpleTextProvider(text: "\(battery)%")
            template.fillFraction = Float(battery) / 100.0
            template.ringStyle = .closed
            entry = CLKComplicationTimelineEntry(date: Date(), complicationTemplate: template)

        case .modularLarge:
            let template = CLKComplicationTemplateModularLargeTable()
            template.headerTextProvider = CLKSimpleTextProvider(text: "Tesla")
            template.row1Column1TextProvider = CLKSimpleTextProvider(text: "Battery")
            template.row1Column2TextProvider = CLKSimpleTextProvider(text: "\(battery)%")
            template.row2Column1TextProvider = CLKSimpleTextProvider(text: "Range")
            template.row2Column2TextProvider = CLKSimpleTextProvider(text: "\(range) km")
            entry = CLKComplicationTimelineEntry(date: Date(), complicationTemplate: template)

        default:
            entry = nil
        }
        handler(entry)
    }

    func getTimelineEndDate(for complication: CLKComplication, withHandler handler: @escaping (Date?) -> Void) {
        handler(Date().addingTimeInterval(3600))
    }

    func getPrivacyBehavior(for complication: CLKComplication, withHandler handler: @escaping (CLKComplicationPrivacyBehavior) -> Void) {
        handler(.showOnLockScreen)
    }
}

// MARK: - SwiftUI Complication Views

private struct CircularComplicationView: View {
    let battery: Int

    private var ringColor: Color {
        if battery > 60 { return .green }
        if battery > 20 { return .yellow }
        return .red
    }

    var body: some View {
        ZStack {
            ProgressView(value: Float(battery) / 100)
                .progressViewStyle(.circular)
                .tint(ringColor)
            Text("\(battery)%")
                .font(.system(size: 12, weight: .bold))
        }
    }
}

private struct RectangularComplicationView: View {
    let battery: Int
    let range: Int
    var body: some View {
        HStack {
            CircularComplicationView(battery: battery)
                .frame(width: 30, height: 30)
            VStack(alignment: .leading, spacing: 2) {
                Text("Tesla")
                    .font(.system(size: 10, weight: .semibold))
                Text("\(range) km")
                    .font(.system(size: 10))
            }
        }
    }
}
