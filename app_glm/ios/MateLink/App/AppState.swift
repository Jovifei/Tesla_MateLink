import SwiftUI
import Security

private enum KeychainHelper {
    private static let accessibility = kSecAttrAccessibleWhenUnlockedThisDeviceOnly

    static func save(_ value: String, key: String) {
        let data = Data(value.utf8)
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(query as CFDictionary)
        let attrs: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecValueData as String: data,
            kSecAttrAccessible as String: accessibility
        ]
        SecItemAdd(attrs as CFDictionary, nil)
    }

    static func load(_ key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne,
            kSecAttrAccessible as String: accessibility
        ]
        var item: CFTypeRef?
        guard SecItemCopyMatching(query as CFDictionary, &item) == errSecSuccess,
              let data = item as? Data else { return nil }
        return String(data: data, encoding: .utf8)
    }

    static func delete(_ key: String) {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key
        ]
        SecItemDelete(query as CFDictionary)
    }
}

@MainActor
class AppState: ObservableObject {
    static weak var shared: AppState?

    @Published var cars: [Car] = []
    @Published var currentCarId: Int = 1
    @Published var isMockMode: Bool = true
    @Published var isDarkMode: Bool = false
    @Published var unitSystem: UnitSystem = .metric
    @Published var onboardingDone: Bool = false
    @Published var serverURL: String = ""
    @Published var apiToken: String = ""
    @Published var selectedTab: Tab = .dashboard
    @Published var instances: [Instance] = []
    @Published var activeInstanceId: String?

    var activeInstance: Instance? { instances.first { $0.id == activeInstanceId } }

    let mock = MockAPI()
    var real: TeslaMateAPI?

    enum Tab: String, CaseIterable { case dashboard, drives, charges, more
        var icon: String { switch self { case .dashboard: return "car.fill"; case .drives: return "road.lanes"; case .charges: return "bolt.fill"; case .more: return "ellipsis.circle" } }
        var label: String { switch self { case .dashboard: return "Vehicle"; case .drives: return "Drives"; case .charges: return "Charges"; case .more: return "More" } }
    }

    var currentCar: Car? { cars.first { $0.id == currentCarId } }
    var carAccent: Color { CarColor.from(currentCar?.color ?? "").accent }

    private let instancesKey = "instances"
    private let activeInstanceIdKey = "activeInstanceId"
    private let sharedDefaults = UserDefaults(suiteName: "group.com.teslamatelink")

    init() {
        AppState.shared = self
        apiToken = KeychainHelper.load("apiToken") ?? ""
        loadInstances()
        loadCars()
        PhoneWCSessionManager.shared.activate()
    }

    private func loadInstances() {
        guard let data = sharedDefaults?.data(forKey: instancesKey),
              let decoded = try? JSONDecoder().decode([Instance].self, from: data) else { return }
        instances = decoded
        activeInstanceId = sharedDefaults?.string(forKey: activeInstanceIdKey)
        if let active = activeInstance {
            serverURL = active.serverUrl
            apiToken = active.apiToken
            currentCarId = active.carId
        }
    }

    func saveInstances() {
        if let data = try? JSONEncoder().encode(instances) {
            sharedDefaults?.set(data, forKey: instancesKey)
        }
        sharedDefaults?.set(activeInstanceId, forKey: activeInstanceIdKey)
    }

    func switchInstance(_ instance: Instance) {
        activeInstanceId = instance.id
        serverURL = instance.serverUrl
        apiToken = instance.apiToken
        currentCarId = instance.carId
        saveInstances()
        KeychainHelper.save(instance.apiToken, key: "token_\(instance.id)")
        loadCars()
    }

    func loadCars() {
        Task {
            if isMockMode {
                let raw = await mock.getCars(); cars = raw.map(Car.init(from:))
            } else if let api = real {
                let resp: CarApiResponse = (try? await api.fetch("/api/v1/cars")) ?? CarApiResponse(data: .init(cars: []))
                cars = resp.data.cars.map(Car.init(from:))
            }
            if let first = cars.first, currentCarId == 1 { currentCarId = first.id }
        }
    }

    func connect(url: String, token: String) async throws {
        let api = TeslaMateAPI(baseURL: url, token: token.isEmpty ? nil : token)
        let resp: CarApiResponse = try await api.fetch("/api/v1/cars")
        self.real = api; self.serverURL = url; self.apiToken = token; self.onboardingDone = true
        KeychainHelper.save(token, key: "apiToken")
        if let id = activeInstanceId {
            KeychainHelper.save(token, key: "token_\(id)")
        }
        cars = resp.data.cars.map(Car.init(from:))
    }
}
