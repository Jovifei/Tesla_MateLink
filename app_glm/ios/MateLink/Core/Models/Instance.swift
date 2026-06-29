import Foundation

struct Instance: Codable, Identifiable, Equatable {
    let id: String
    var name: String
    var serverUrl: String
    var apiToken: String
    var carId: Int = 1
}
