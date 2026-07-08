import SwiftUI

// MARK: - Static Data (matches Stitch design spec)

private struct VehicleInfo: Identifiable {
    let id = UUID()
    let label: String
    let value: String
}

private let vehicleInfoList: [VehicleInfo] = [
    VehicleInfo(label: "Vehicle Model", value: "Model 3 2022"),
    VehicleInfo(label: "VIN", value: "5YJ3E1EA7NF"),
    VehicleInfo(label: "Odometer", value: "12,450 km"),
    VehicleInfo(label: "TeslaMate API", value: "v1.32.0")
]

private struct TechStackItem: Identifiable {
    let id = UUID()
    let icon: String
    let title: String
    let desc: String
}

private let techStackList: [TechStackItem] = [
    TechStackItem(icon: "iphone", title: "iOS", desc: "SwiftUI / Swift Charts"),
    TechStackItem(icon: "candybarphone", title: "Android", desc: "Kotlin Compose / Hilt"),
    TechStackItem(icon: "globe", title: "Web", desc: "React + Vite / Recharts")
]

private struct FeatureRow: Identifiable {
    let id = UUID()
    let feature: String
    let android: Bool
    let ios: Bool
    let web: Bool
}

private let featureMatrix: [FeatureRow] = [
    FeatureRow(feature: "Real-time Stats", android: true, ios: true, web: true),
    FeatureRow(feature: "Charging History", android: true, ios: true, web: true),
    FeatureRow(feature: "Widgets", android: true, ios: true, web: false),
    FeatureRow(feature: "Live Maps", android: true, ios: false, web: true)
]

private struct LicenseItem: Identifiable {
    let id = UUID()
    let name: String
    let version: String
}

private let licenses: [LicenseItem] = [
    LicenseItem(name: "Retrofit", version: "v2.9.0 / Apache 2.0"),
    LicenseItem(name: "OkHttp", version: "v4.9.3 / Apache 2.0"),
    LicenseItem(name: "Moshi", version: "v1.13.0 / Apache 2.0"),
    LicenseItem(name: "Hilt", version: "v2.44 / Apache 2.0"),
    LicenseItem(name: "Recharts", version: "v2.1.9 / MIT"),
    LicenseItem(name: "Leaflet", version: "v1.8.0 / BSD"),
    LicenseItem(name: "WidgetKit", version: "iOS Native"),
    LicenseItem(name: "AMap SDK", version: "JS API v2.0")
]

// MARK: - AboutView

struct AboutView: View {
    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 32) {
                BrandIdentity()
                VehicleInfoCard()
                TechStackCard()
                DataSourceCard()
                FunctionMatrixCard()
                LicensesCard()
                ContactCard()
                Footer()
            }
            .padding(.horizontal, 24)
            .padding(.bottom, 128)
        }
        .background(StitchColors.background)
        .navigationTitle("关于")
        .navigationBarTitleDisplayMode(.inline)
    }
}

// MARK: - Brand Identity

private struct BrandIdentity: View {
    var body: some View {
        VStack(spacing: 16) {
            // Circular icon with heavy border
            ZStack {
                Circle()
                    .stroke(StitchColors.onSurface, lineWidth: 1)
                Image(systemName: "bolt.car")
                    .font(.system(size: 36, weight: .bold))
                    .foregroundColor(StitchColors.onSurface)
            }
            .frame(width: 96, height: 96)

            VStack(spacing: 4) {
                Text("MateLink")
                    .font(StitchFont.displayLg())
                    .foregroundColor(StitchColors.onSurface)
                Text("Your Tesla Data Companion")
                    .font(StitchFont.bodyLg())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                HStack(spacing: 8) {
                    VersionChip(text: "v0.1.0-alpha")
                    VersionChip(text: "2025.07.02")
                }
                .padding(.top, 4)
            }
        }
        .frame(maxWidth: .infinity)
        .padding(.top, 8)
    }
}

private struct VersionChip: View {
    let text: String
    var body: some View {
        Text(text)
            .font(StitchFont.labelCaps())
            .foregroundColor(StitchColors.onSurface)
            .padding(.horizontal, 8)
            .padding(.vertical, 4)
            .background(StitchColors.surfaceContainerHigh)
            .clipShape(RoundedRectangle(cornerRadius: 4))
    }
}

// MARK: - Vehicle Info Card

private struct VehicleInfoCard: View {
    var body: some View {
        StitchCard {
            StitchLabel("已连接车辆")
            Spacer().frame(height: 16)
            VStack(spacing: 0) {
                ForEach(Array(vehicleInfoList.enumerated()), id: \.element.id) { index, info in
                    HStack {
                        Text(info.label)
                            .font(StitchFont.bodyLg())
                            .foregroundColor(StitchColors.onSurface)
                        Spacer()
                        Text(info.value)
                            .font(StitchFont.dataMd())
                            .foregroundColor(StitchColors.onSurface)
                    }
                    .padding(.vertical, 16)
                    if index < vehicleInfoList.count - 1 {
                        Divider()
                            .frame(height: 1)
                            .background(StitchColors.border)
                    }
                }
            }
        }
    }
}

// MARK: - Tech Stack Card

private struct TechStackCard: View {
    var body: some View {
        StitchCard {
            StitchLabel("技术栈")
            Spacer().frame(height: 16)
            HStack(spacing: 16) {
                ForEach(techStackList) { item in
                    VStack(alignment: .leading, spacing: 8) {
                        Image(systemName: item.icon)
                            .font(.system(size: 24))
                            .foregroundColor(StitchColors.secondary)
                        Text(item.title)
                            .font(.system(size: 14, weight: .bold))
                            .foregroundColor(StitchColors.onSurface)
                        Text(item.desc)
                            .font(StitchFont.bodySm())
                            .foregroundColor(StitchColors.onSurfaceVariant)
                    }
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .padding(16)
                    .background(StitchColors.surfaceContainerLow)
                    .clipShape(RoundedRectangle(cornerRadius: 4))
                }
            }
        }
    }
}

// MARK: - Data Source Card

private struct DataSourceCard: View {
    var body: some View {
        HStack(alignment: .top, spacing: 16) {
            // Shield icon with green tint
            Image(systemName: "lock.shield.fill")
                .font(.system(size: 20))
                .foregroundColor(Color(hex: "059669"))
                .padding(8)
                .background(Color(hex: "D1FAE5"))
                .clipShape(RoundedRectangle(cornerRadius: 4))

            VStack(alignment: .leading, spacing: 4) {
                StitchLabel("数据来源")
                Text("TeslaMate Self-hosted + TeslaMateApi")
                    .font(.system(size: 16, weight: .bold))
                    .foregroundColor(StitchColors.onSurface)
                Text("所有数据来自您的 TeslaMate 服务器。MateLink 不会收集、存储或上传您的车辆位置、驾驶习惯或任何敏感凭证。您的隐私受自建服务器保护。")
                    .font(StitchFont.bodySm())
                    .foregroundColor(StitchColors.onSurfaceVariant)
            }
        }
        .padding(24)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }
}

// MARK: - Function Matrix Card

private struct FunctionMatrixCard: View {
    var body: some View {
        VStack(spacing: 0) {
            // Header
            HStack {
                StitchLabel("功能支持矩阵")
                Spacer()
            }
            .padding(24)
            .background(StitchColors.surfaceContainerLow)

            Divider()
                .frame(height: 1)
                .background(StitchColors.border)

            // Table header
            HStack {
                Text("Feature")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                    .frame(maxWidth: .infinity, alignment: .leading)
                Text("Android")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                    .frame(maxWidth: .infinity)
                Text("iOS")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                    .frame(maxWidth: .infinity)
                Text("Web")
                    .font(StitchFont.labelCaps())
                    .foregroundColor(StitchColors.onSurfaceVariant)
                    .frame(maxWidth: .infinity)
            }
            .padding(.horizontal, 24)
            .padding(.vertical, 16)
            .background(StitchColors.surfaceContainerLow)

            Divider()
                .frame(height: 1)
                .background(StitchColors.border)

            // Rows
            ForEach(Array(featureMatrix.enumerated()), id: \.element.id) { index, row in
                HStack {
                    Text(row.feature)
                        .font(StitchFont.bodyLg())
                        .foregroundColor(StitchColors.onSurface)
                        .frame(maxWidth: .infinity, alignment: .leading)
                    FeatureCheck(supported: row.android)
                        .frame(maxWidth: .infinity)
                    FeatureCheck(supported: row.ios)
                        .frame(maxWidth: .infinity)
                    FeatureCheck(supported: row.web)
                        .frame(maxWidth: .infinity)
                }
                .padding(.horizontal, 24)
                .padding(.vertical, 16)

                if index < featureMatrix.count - 1 {
                    Divider()
                        .frame(height: 1)
                        .background(StitchColors.border)
                }
            }
        }
        .background(StitchColors.white)
        .clipShape(RoundedRectangle(cornerRadius: 8))
        .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))
    }
}

private struct FeatureCheck: View {
    let supported: Bool
    var body: some View {
        Image(systemName: supported ? "checkmark.circle.fill" : "xmark.circle")
            .font(.system(size: 20))
            .foregroundColor(supported ? Color(hex: "059669") : StitchColors.onSurfaceVariant)
    }
}

// MARK: - Licenses Card

private struct LicensesCard: View {
    var body: some View {
        StitchCard {
            StitchLabel("开源依赖")
            Spacer().frame(height: 16)
            // 2-column grid
            let columns = Array(licenses.chunked(2))
            ForEach(Array(columns.enumerated()), id: \.offset) { _, row in
                HStack(spacing: 16) {
                    ForEach(row) { item in
                        VStack(alignment: .leading, spacing: 4) {
                            Text(item.name)
                                .font(.system(size: 14, weight: .bold))
                                .foregroundColor(StitchColors.onSurface)
                            Text(item.version)
                                .font(.custom("JetBrainsMono-Medium", size: 10))
                                .foregroundColor(StitchColors.onSurfaceVariant)
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                    }
                    // Fill empty slot if odd count
                    if row.count == 1 {
                        Spacer()
                    }
                }
                Spacer().frame(height: 16)
            }
        }
    }
}

// MARK: - Contact Card

private struct ContactCard: View {
    var body: some View {
        VStack(spacing: 16) {
            // Contact info
            VStack(alignment: .leading, spacing: 12) {
                StitchLabel("反馈与联系")
                ContactRow(icon: "chevron.left.forwardslash.chevron.right", text: "GitHub Repository")
                ContactRow(icon: "envelope", text: "contact@jovif.dev")
            }
            .padding(24)
            .frame(maxWidth: .infinity, alignment: .leading)
            .background(StitchColors.white)
            .clipShape(RoundedRectangle(cornerRadius: 8))
            .overlay(RoundedRectangle(cornerRadius: 8).stroke(StitchColors.border, lineWidth: 1))

            // Submit feedback button
            HStack(spacing: 8) {
                Image(systemName: "bubble.left.and.bubble.right")
                    .font(.system(size: 20))
                Text("提交反馈")
                    .font(StitchFont.labelCaps())
            }
            .foregroundColor(StitchColors.white)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 24)
            .background(StitchColors.primary)
            .clipShape(RoundedRectangle(cornerRadius: 8))
        }
    }
}

private struct ContactRow: View {
    let icon: String
    let text: String
    var body: some View {
        HStack(spacing: 8) {
            Image(systemName: icon)
                .font(.system(size: 20))
                .foregroundColor(StitchColors.onSurface)
            Text(text)
                .font(StitchFont.bodyLg())
                .foregroundColor(StitchColors.onSurface)
        }
    }
}

// MARK: - Footer

private struct Footer: View {
    var body: some View {
        VStack(spacing: 4) {
            Text("© 2025 JoviF · MIT License")
                .font(StitchFont.dataMd())
                .foregroundColor(StitchColors.onSurfaceVariant)
            Text("Made with ❤️ for Tesla owners")
                .font(StitchFont.bodySm())
                .foregroundColor(StitchColors.onSurfaceVariant)
        }
        .frame(maxWidth: .infinity)
    }
}
