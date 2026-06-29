import Foundation

enum ApiError: LocalizedError {
    case networkUnreachable(String), unauthorized, serverError(Int, String), timeout, decodingError(String), mockDataMissing
    var errorDescription: String? {
        switch self {
        case .networkUnreachable(let msg): return "Cannot reach server: \(msg)"
        case .unauthorized: return "Invalid token (401)"
        case .serverError(let code, let body): return "Server error \(code): \(body)"
        case .timeout: return "Connection timeout"
        case .decodingError(let msg): return "Data error: \(msg)"
        case .mockDataMissing: return "Mock data file not found"
        }
    }
}

actor TeslaMateAPI {
    private let baseURL: String; private let token: String?
    private let session: URLSession

    init(baseURL: String, token: String?) {
        self.baseURL = baseURL.hasSuffix("/") ? String(baseURL.dropLast()) : baseURL
        self.token = token
        let config = URLSessionConfiguration.default; config.timeoutIntervalForRequest = 10
        self.session = URLSession(configuration: config)
    }

    private func url(for path: String) throws -> URL {
        let normalizedPath = path.hasPrefix("/") ? path : "/" + path
        guard let url = URL(string: "\(baseURL)\(normalizedPath)") else { throw ApiError.networkUnreachable("Invalid URL") }
        return url
    }

    func fetch<T: Decodable>(_ path: String) async throws -> T {
        var req = URLRequest(url: try url(for: path))
        if let token = token { req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization") }
        do {
            let (data, resp) = try await session.data(for: req)
            guard let http = resp as? HTTPURLResponse else { throw ApiError.serverError(0, "Not HTTP") }
            guard (200...299).contains(http.statusCode) else { throw ApiError.serverError(http.statusCode, String(data: data, encoding: .utf8) ?? "") }
            do { return try JSONDecoder().decode(T.self, from: data) }
            catch { throw ApiError.decodingError(error.localizedDescription) }
        } catch let e as ApiError { throw e }
        catch let e as URLError where e.code == .timedOut { throw ApiError.timeout }
        catch { throw ApiError.networkUnreachable(error.localizedDescription) }
    }

    /// HTTP status‑only fetch (no decoding) — used for ping / readyz probes.
    func checkStatus(_ path: String) async throws {
        var req = URLRequest(url: try url(for: path))
        if let token = token { req.setValue("Bearer \(token)", forHTTPHeaderField: "Authorization") }
        do {
            let (_, resp) = try await session.data(for: req)
            guard let http = resp as? HTTPURLResponse else { throw ApiError.serverError(0, "Not HTTP") }
            guard (200...299).contains(http.statusCode) else { throw ApiError.serverError(http.statusCode, "Status \(http.statusCode)") }
        } catch let e as ApiError { throw e }
        catch let e as URLError where e.code == .timedOut { throw ApiError.timeout }
        catch { throw ApiError.networkUnreachable(error.localizedDescription) }
    }
}

// MARK: - Mock Client (for preview / development)
actor MockAPI {
    private let data: MockData?
    init() { self.data = try? MockData.load() }

    func mockStatus(_ carId: Int) -> CarStatus {
        guard let data else { return CarStatus(carId: carId, state: .offline, since: "", healthy: true, odometer: 0, batteryLevel: 0, usableBatteryLevel: 0, chargeEnergyAdded: 0, chargeLimitSoc: 0, idealBatteryRangeKm: 0, estBatteryRangeKm: 0, chargerPower: 0, chargerActualCurrent: 0, chargerVoltage: 0, chargePortDoorOpen: false, timeToFullCharge: 0, insideTemp: 0, outsideTemp: 0, isClimateOn: false, latitude: 0, longitude: 0, heading: 0, speed: 0, shiftState: "", locked: false, sentryMode: false, carVersion: "", tirePressure: nil) }
        var s = data.status[String(carId)] ?? CarStatus(carId: carId, state: .offline, since: "", healthy: true, odometer: 0, batteryLevel: 0, usableBatteryLevel: 0, chargeEnergyAdded: 0, chargeLimitSoc: 0, idealBatteryRangeKm: 0, estBatteryRangeKm: 0, chargerPower: 0, chargerActualCurrent: 0, chargerVoltage: 0, chargePortDoorOpen: false, timeToFullCharge: 0, insideTemp: 0, outsideTemp: 0, isClimateOn: false, latitude: 0, longitude: 0, heading: 0, speed: 0, shiftState: "", locked: false, sentryMode: false, carVersion: "", tirePressure: nil)
        s.batteryLevel = min(100, max(10, Int(s.batteryLevel) + (Int.random(in: -1...1))))
        return s
    }

    func getCars() -> [CarRaw] { data?.cars ?? [] }
    func getCarStatus(_ carId: Int) -> CarStatus { data?.status[String(carId)] ?? CarStatus(carId: carId, state: .offline, since: "", healthy: true, odometer: 0, batteryLevel: 0, usableBatteryLevel: 0, chargeEnergyAdded: 0, chargeLimitSoc: 0, idealBatteryRangeKm: 0, estBatteryRangeKm: 0, chargerPower: 0, chargerActualCurrent: 0, chargerVoltage: 0, chargePortDoorOpen: false, timeToFullCharge: 0, insideTemp: 0, outsideTemp: 0, isClimateOn: false, latitude: 0, longitude: 0, heading: 0, speed: 0, shiftState: "", locked: false, sentryMode: false, carVersion: "", tirePressure: nil) }
    func getDrives(_ carId: Int) -> [Drive] { data?.drives.filter { $0.carId == carId } ?? [] }
    func getCharges(_ carId: Int) -> [Charge] { data?.charges.filter { $0.carId == carId } ?? [] }
    func getBatteryHealth(_ carId: Int) -> BatteryHealth { data?.batteryHealth[String(carId)] ?? BatteryHealth(carId: carId, originalCapacityKwh: 0, currentCapacityKwh: 0, capacityDegradationPercent: 0, originalRangeKm: 0, currentRangeKm: 0, rangeLossPercent: 0, mileageKm: 0, history: []) }
    func getUpdates(_ carId: Int) -> [UpdateItem] { data?.updates[String(carId)] ?? [] }
}

class MockData: Codable {
    let cars: [CarRaw]; let status: [String: CarStatus]; let drives: [Drive]; let charges: [Charge]
    let batteryHealth: [String: BatteryHealth]; let updates: [String: [UpdateItem]]

    static func load() throws -> MockData {
        guard let url = Bundle.main.url(forResource: "mock_data", withExtension: "json"),
              let d = try? Data(contentsOf: url) else {
            throw ApiError.mockDataMissing
        }
        return try JSONDecoder().decode(MockData.self, from: d)
    }

    enum CodingKeys: String, CodingKey {
        case cars, status, drives, charges; case batteryHealth = "batteryHealth"; case updates
    }
}

// MARK: - JSON‑file cache (F‑015)
actor APICache {
    static let shared = APICache()
    private let cachesDir: URL

    init() {
        let fm = FileManager.default
        cachesDir = fm.urls(for: .cachesDirectory, in: .userDomainMask)[0]
            .appendingPathComponent("MateLinkCache", isDirectory: true)
        try? fm.createDirectory(at: cachesDir, withIntermediateDirectories: true)
    }

    private func url(for key: String) -> URL {
        cachesDir.appendingPathComponent("\(key).json")
    }

    func write<T: Encodable>(_ value: T, key: String) {
        guard let data = try? JSONEncoder().encode(value) else { return }
        try? data.write(to: url(for: key), options: .atomic)
    }

    func read<T: Decodable>(_ type: T.Type, key: String, ttl: TimeInterval = 86400) -> T? {
        let u = url(for: key)
        guard let data = try? Data(contentsOf: u) else { return nil }
        // Check TTL — return nil if cache is too old
        if let attrs = try? FileManager.default.attributesOfItem(atPath: u.path),
           let modDate = attrs[.modificationDate] as? Date,
           Date().timeIntervalSince(modDate) > ttl { return nil }
        return try? JSONDecoder().decode(type, from: data)
    }
}

extension TeslaMateAPI {
    func cacheDrives(_ drives: [Drive], carId: Int) async {
        await APICache.shared.write(drives, key: "cache_drives_\(carId)")
    }
    func getCachedDrives(carId: Int) async -> [Drive]? {
        await APICache.shared.read([Drive].self, key: "cache_drives_\(carId)")
    }
    func cacheCharges(_ charges: [Charge], carId: Int) async {
        await APICache.shared.write(charges, key: "cache_charges_\(carId)")
    }
    func getCachedCharges(carId: Int) async -> [Charge]? {
        await APICache.shared.read([Charge].self, key: "cache_charges_\(carId)")
    }
}
