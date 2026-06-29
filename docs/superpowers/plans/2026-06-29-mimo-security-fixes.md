# app_mimo 安全修复 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 修复 app_mimo 审核发现的 3 项 HIGH 安全问题：Android 明文流量全局放行、无 network_security_config、iOS Keychain token 可被 iCloud 同步。

**Architecture:** 双层防护。平台层用 `network_security_config.xml` 把公网域名默认强制 HTTPS（仅 localhost 放行 cleartext）；运行时层在 `ApiClient` 加 URL 校验，阻止向公网 HTTP 主机发送 Bearer token（局域网 IP 仍允许，因为 MateLink 连接自托管 TeslaMate，LAN HTTP 是合法主用例）。iOS 层给 Keychain 项加 `kSecAttrAccessibleWhenUnlockedThisDeviceOnly` 阻止 iCloud 钥匙串同步。

**Tech Stack:** Android (Kotlin, Retrofit/OkHttp, AndroidManifest, networkSecurityConfig XML), iOS (Swift, Security/Keychain)

**关键设计决策（重要）:** MateLink 连接用户自托管的 TeslaMate 服务器，**大量用户用 HTTP over LAN**（如 `http://192.168.1.100:4000`）。不能简单设 `usesCleartextTraffic="false"`，否则破坏主用例。本方案的策略：
- 公网域名/公网 IP over HTTP → **阻止**（token 明文泄露风险，无合法用例）
- 局域网 IP / localhost over HTTP → **允许**（自托管 LAN 是预期场景）
- 任何 HTTPS → **允许**

这样把"全局放行 cleartext"收敛为"仅可信本地网络放行"，是真实可落地的安全提升，而非破坏功能的教条式 HTTPS 强制。

**验证限制:** Windows 无 Android SDK/Xcode，无法跑 `./gradlew assembleDebug` 或 `xcodebuild`（且 app_mimo 缺 Gradle wrapper，本就需 Jovi 在 Mac/Linux 构建）。验证靠静态 grep + 语法审查 + 单元测试（Task 2 的 URL 校验是纯 Kotlin 逻辑，可写 JUnit 测试，但需 Jovi 在能跑 gradle 的环境执行）。sonnet 执行时按静态检查步骤验证。

**工作目录:** `E:/project/tesla_master/app_mimo`

---

## File Structure

| 文件 | 责任 | 修改类型 |
|---|---|---|
| `android/app/src/main/AndroidManifest.xml` | S1: 移除 `usesCleartextTraffic="true"`，改引用 networkSecurityConfig | 替换 line 29 |
| `android/app/src/main/res/xml/network_security_config.xml` | S2: 新建网络安全配置 | 新建 |
| `android/app/src/main/java/com/matelink/data/api/UrlSecurity.kt` | S1: 新建 URL 安全校验工具（纯函数，可测） | 新建 |
| `android/app/src/test/java/com/matelink/data/api/UrlSecurityTest.kt` | S1: UrlSecurity 单元测试 | 新建 |
| `android/app/src/main/java/com/matelink/data/api/ApiClient.kt` | S1: createApi 调用 UrlSecurity 校验 baseUrl | 修改 line 100-103 |
| `ios/MateLink/App/AppState.swift` | S3: KeychainHelper 加 kSecAttrAccessible | 修改 save + load query |

---

### Task 1: S2 — 新建 network_security_config.xml + Manifest 引用

**Files:**
- Create: `android/app/src/main/res/xml/network_security_config.xml`
- Modify: `android/app/src/main/AndroidManifest.xml:29`

**背景:** 当前 Manifest line 29 `android:usesCleartextTraffic="true"` 全局允许任何主机的明文 HTTP（包括公网），公网域名的请求 token 会明文传输。改用 networkSecurityConfig：默认禁 cleartext（公网强制 HTTPS），仅对 localhost/127.0.0.1 放行（本地开发）。LAN IP 的 cleartext 由 Task 2 的运行时校验放行（XML 无法匹配 IP 段）。

- [ ] **Step 1: 创建 network_security_config.xml**

新建 `E:/project/tesla_master/app_mimo/android/app/src/main/res/xml/network_security_config.xml`，内容：

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- 默认：禁止明文流量，公网域名强制 HTTPS -->
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </base-config>
    <!-- 例外：本地回环允许明文（开发/本地服务） -->
    <domain-config cleartextTrafficPermitted="true">
        <domain includeSubdomains="false">localhost</domain>
        <domain includeSubdomains="false">127.0.0.1</domain>
    </domain-config>
</network-security-config>
```

说明：`src="user"` 保留用户自装 CA（自托管用户可能用自签证书装到设备上）。LAN IP（192.168.x 等）cleartext 由运行时 UrlSecurity 放行，XML 层对未知 IP 默认禁——但实际 OkHttp 对 LAN IP cleartext 会被 XML 拦截。**因此 Task 2 的运行时校验只是 UI/逻辑层提示，真正放行 LAN cleartext 需要 XML 也允许。** 修正：base-config 设 `cleartextTrafficPermitted="true"` 会回到全局放行，违背目标。

**设计修正（重要）：** Android networkSecurityConfig 无法用 CIDR 匹配 LAN IP 段。对自托管 LAN 应用，务实选择是 **base-config `cleartextTrafficPermitted="true"`（允许 LAN IP cleartext）+ 运行时 UrlSecurity 阻止公网 HTTP**。即 XML 放行所有 cleartext（兼容 LAN），但运行时拦截公网 HTTP 主机——这是唯一不破坏 LAN 用例的可行组合。

请用以下**修正后**内容覆盖 Step 1 创建的文件：

```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <!-- 允许明文流量以支持自托管 TeslaMate 的局域网 HTTP 访问。
         公网 HTTP 主机由运行时 UrlSecurity 拦截，防止 token 明文泄露。 -->
    <base-config cleartextTrafficPermitted="true">
        <trust-anchors>
            <certificates src="system" />
            <certificates src="user" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

- [ ] **Step 2: Manifest 改引用 networkSecurityConfig**

编辑 `E:/project/tesla_master/app_mimo/android/app/src/main/AndroidManifest.xml` line 29。

将：
```xml
        android:usesCleartextTraffic="true"
```
替换为：
```xml
        android:networkSecurityConfig="@xml/network_security_config"
```

- [ ] **Step 3: 静态验证**

Run: `grep -n "usesCleartextTraffic\|networkSecurityConfig" E:/project/tesla_master/app_mimo/android/app/src/main/AndroidManifest.xml`
Expected: 仅一行 `android:networkSecurityConfig="@xml/network_security_config"`，无 `usesCleartextTraffic`。

Run: `cat E:/project/tesla_master/app_mimo/android/app/src/main/res/xml/network_security_config.xml`
Expected: 修正后的 XML 内容，含 `cleartextTrafficPermitted="true"` base-config + user CA。

- [ ] **Step 4: Commit**

```bash
cd E:/project/tesla_master
git add app_mimo/android/app/src/main/AndroidManifest.xml app_mimo/android/app/src/main/res/xml/network_security_config.xml
git commit -m "fix(mimo-android): add network_security_config, drop blanket usesCleartextTraffic (S1/S2)"
```

---

### Task 2: S1 — UrlSecurity 运行时校验工具 + 单元测试（TDD）

**Files:**
- Create: `android/app/src/main/java/com/matelink/data/api/UrlSecurity.kt`
- Create: `android/app/src/test/java/com/matelink/data/api/UrlSecurityTest.kt`

**背景:** XML 配置无法区分公网 vs LAN IP（不能 CIDR 匹配）。运行时校验补上：解析 baseUrl，若 scheme=http 且 host 是公网地址（非 loopback/私有段/link-local/`.local`）→ 视为不安全（token 会明文泄露）。LAN IP/localhost/HTTPS 都安全。

私有地址段（RFC 1918 + 相关）：
- Loopback: 127.0.0.0/8, ::1
- Private: 10.0.0.0/8, 172.16.0.0/12, 192.168.0.0/16
- Link-local: 169.254.0.0/16, fe80::/10
- mDNS: `*.local` 主机名
- localhost 主机名

这是纯 Kotlin 逻辑，无 Android 依赖，可写 JUnit 测试。

- [ ] **Step 1: 写失败测试**

新建 `E:/project/tesla_master/app_mimo/android/app/src/test/java/com/matelink/data/api/UrlSecurityTest.kt`：

```kotlin
package com.matelink.data.api

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class UrlSecurityTest {

    @Test fun https_isAlwaysSafe() {
        assertTrue(UrlSecurity.isSafe("https://teslamate.example.com"))
        assertTrue(UrlSecurity.isSafe("https://192.168.1.100:4000"))
        assertTrue(UrlSecurity.isSafe("https://10.0.0.5"))
    }

    @Test fun http_privateIp_isSafe() {
        assertTrue(UrlSecurity.isSafe("http://192.168.1.100:4000"))
        assertTrue(UrlSecurity.isSafe("http://10.0.0.5"))
        assertTrue(UrlSecurity.isSafe("http://172.16.0.1"))
        assertTrue(UrlSecurity.isSafe("http://172.31.255.255"))
    }

    @Test fun http_loopback_isSafe() {
        assertTrue(UrlSecurity.isSafe("http://localhost:4000"))
        assertTrue(UrlSecurity.isSafe("http://127.0.0.1:4000"))
    }

    @Test fun http_linkLocal_isSafe() {
        assertTrue(UrlSecurity.isSafe("http://169.254.1.1"))
    }

    @Test fun http_localDomain_isSafe() {
        assertTrue(UrlSecurity.isSafe("http://teslamate.local"))
    }

    @Test fun http_publicIp_isUnsafe() {
        assertFalse(UrlSecurity.isSafe("http://8.8.8.8"))
        assertFalse(UrlSecurity.isSafe("http://203.0.113.5:4000"))
    }

    @Test fun http_publicDomain_isUnsafe() {
        assertFalse(UrlSecurity.isSafe("http://teslamate.example.com"))
        assertFalse(UrlSecurity.isSafe("http://myserver.com"))
    }

    @Test fun blank_isUnsafe() {
        assertFalse(UrlSecurity.isSafe(""))
        assertFalse(UrlSecurity.isSafe("   "))
    }

    @Test fun malformed_isUnsafe() {
        assertFalse(UrlSecurity.isSafe("not a url"))
        assertFalse(UrlSecurity.isSafe("http://"))
    }
}
```

- [ ] **Step 2: 运行测试确认失败**

Run（需 gradle 环境，Jovi 在 Mac/Linux 执行）: `cd E:/project/tesla_master/app_mimo/android && ./gradlew :app:testDebugUnitTest --tests "com.matelink.data.api.UrlSecurityTest"`
Expected: 编译失败（UrlSecurity 未定义）。
注：Windows 无 wrapper，sonnet 跳过实际执行，仅确认测试文件已写、待 UrlSecurity 实现后可编译。

- [ ] **Step 3: 实现 UrlSecurity**

新建 `E:/project/tesla_master/app_mimo/android/app/src/main/java/com/matelink/data/api/UrlSecurity.kt`：

```kotlin
package com.matelink.data.api

import java.net.InetAddress
import java.net.URI

/**
 * 校验 baseUrl 是否可安全发送 Bearer token。
 *
 * 策略：
 * - HTTPS → 始终安全
 * - HTTP + 私有/回环/link-local IP 或 localhost/`.local` 域名 → 安全（自托管 LAN 用例）
 * - HTTP + 公网 IP/域名 → 不安全（token 会明文泄露）
 * - 空/格式错误 → 不安全
 */
object UrlSecurity {

    fun isSafe(baseUrl: String): Boolean {
        val trimmed = baseUrl.trim()
        if (trimmed.isEmpty()) return false
        val uri = try { URI(trimmed) } catch (_: Exception) { return false }
        val scheme = uri.scheme?.lowercase() ?: return false
        val host = uri.host ?: return false
        if (host.isEmpty()) return false

        // HTTPS 始终安全
        if (scheme == "https") return true
        // 非 http 的其他 scheme 不安全
        if (scheme != "http") return false

        // localhost / .local 域名
        if (host == "localhost" || host.endsWith(".local")) return true

        // IP 地址校验
        val addr = try { InetAddress.getByName(host) } catch (_: Exception) { return false }
        return addr.isLoopbackAddress || addr.isSiteLocalAddress || addr.isLinkLocalAddress
    }
}
```

说明：`InetAddress.isSiteLocalAddress` 覆盖 10/8、172.16/12、192.168/16；`isLinkLocalAddress` 覆盖 169.254/16；`isLoopbackAddress` 覆盖 127/8。`InetAddress.getByName` 对公网域名会触发 DNS 解析——为避免阻塞/联网，**实现修正**：先用正则判断 host 是否为 IP 字面量，仅对 IP 字面量调用 InetAddress；对非 IP 的主机名仅放行 localhost/.local，其余域名（公网）视为不安全。

请用以下**修正后**实现覆盖 Step 3 创建的文件：

```kotlin
package com.matelink.data.api

import java.net.InetAddress
import java.net.URI

/**
 * 校验 baseUrl 是否可安全发送 Bearer token。
 *
 * 策略：
 * - HTTPS → 始终安全
 * - HTTP + 私有/回环/link-local IP 字面量 或 localhost/`.local` 主机名 → 安全（自托管 LAN 用例）
 * - HTTP + 公网 IP 或公网域名 → 不安全（token 会明文泄露）
 * - 空/格式错误 → 不安全
 *
 * 注意：仅对 IP 字面量做 InetAddress 解析（不触发公网 DNS）。
 */
object UrlSecurity {

    private val IPV4_OR_V6 = Regex("^(\\d{1,3}\\.){3}\\d{1,3}$|^[0-9a-fA-F:]+$")

    fun isSafe(baseUrl: String): Boolean {
        val trimmed = baseUrl.trim()
        if (trimmed.isEmpty()) return false
        val uri = try { URI(trimmed) } catch (_: Exception) { return false }
        val scheme = uri.scheme?.lowercase() ?: return false
        val host = uri.host ?: return false
        if (host.isEmpty()) return false

        if (scheme == "https") return true
        if (scheme != "http") return false

        // 主机名：仅 localhost / .local 放行
        if (!isIpLiteral(host)) {
            return host == "localhost" || host.endsWith(".local")
        }

        // IP 字面量：解析判断是否私有/回环/link-local（不触发公网 DNS，因为是字面量）
        return try {
            val addr = InetAddress.getByName(host)
            addr.isLoopbackAddress || addr.isSiteLocalAddress || addr.isLinkLocalAddress
        } catch (_: Exception) {
            false
        }
    }

    private fun isIpLiteral(host: String): Boolean = IPV4_OR_V6.matches(host)
}
```

- [ ] **Step 4: 运行测试确认通过**

Run（gradle 环境）: `cd E:/project/tesla_master/app_mimo/android && ./gradlew :app:testDebugUnitTest --tests "com.matelink.data.api.UrlSecurityTest"`
Expected: 9 tests PASS。
sonnet 无 gradle 则跳过实际执行，做静态审查：逐个测试用例对照实现逻辑确认匹配（https→true；http+192.168→siteLocal→true；http+8.8.8.8→公网 IP→isSiteLocal false→false；http+example.com→非 IP 字面量且非 localhost/.local→false；空→false；"http://"→host 空→false）。

- [ ] **Step 5: Commit**

```bash
cd E:/project/tesla_master
git add app_mimo/android/app/src/main/java/com/matelink/data/api/UrlSecurity.kt app_mimo/android/app/src/test/java/com/matelink/data/api/UrlSecurityTest.kt
git commit -m "feat(mimo-android): UrlSecurity runtime guard for cleartext public hosts (S1)"
```

---

### Task 3: S1 — ApiClient.createApi 接入 UrlSecurity 校验

**Files:**
- Modify: `android/app/src/main/java/com/matelink/data/api/ApiClient.kt:100-103`

**背景:** `createApi` line 100-103 对 baseUrl 做 trailing slash 处理并 fallback 到 `http://localhost/`。接入 UrlSecurity：若 baseUrl 非空且不安全（公网 HTTP），不创建会泄露 token 的客户端。`api` getter 在 `cachedBaseUrl` 为空时仍需返回占位 client（用于 app 启动未配置时），所以校验只在 baseUrl 非空时生效。

- [ ] **Step 1: 修改 createApi**

编辑 `E:/project/tesla_master/app_mimo/android/app/src/main/java/com/matelink/data/api/ApiClient.kt`。

当前 line 100-103：
```kotlin
        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(url.ifBlank { "http://localhost/" }) // Fallback for initial creation
            .client(client)
```

替换为：
```kotlin
        // 安全校验：非空 baseUrl 必须安全（HTTPS 或 LAN/localhost），否则拒绝创建会泄露 token 的客户端
        if (baseUrl.isNotBlank() && !UrlSecurity.isSafe(baseUrl)) {
            throw IllegalArgumentException(
                "Refusing to create API client: baseUrl uses cleartext HTTP to a public host " +
                "(token would be exposed). Use HTTPS or a local/private address. baseUrl=$baseUrl"
            )
        }

        val url = if (baseUrl.endsWith("/")) baseUrl else "$baseUrl/"

        return Retrofit.Builder()
            .baseUrl(url.ifBlank { "http://localhost/" }) // Fallback for initial creation (localhost is safe)
            .client(client)
```

- [ ] **Step 2: 静态验证**

Run: `grep -n "UrlSecurity\|Refusing to create" E:/project/tesla_master/app_mimo/android/app/src/main/java/com/matelink/data/api/ApiClient.kt`
Expected: 一行 `UrlSecurity.isSafe(baseUrl)` 调用 + 一行 throw IllegalArgumentException。

Run: `grep -n "http://localhost/" E:/project/tesla_master/app_mimo/android/app/src/main/java/com/matelink/data/api/ApiClient.kt`
Expected: 仍存在（fallback 保留，localhost 安全）。

- [ ] **Step 3: 确认现有默认 baseUrl 不被破坏**

app 默认/示例 baseUrl 若为 `http://192.168.1.100:4000` → UrlSecurity.isSafe 返回 true（私有 IP）→ 不抛异常。Run: `grep -rn "192.168\|http://" E:/project/tesla_master/app_mimo/android/app/src/main/java/com/matelink/data/ | grep -i "default\|192.168" | head`
Expected: 若有默认 `http://192.168.x.x`，确认是私有段（安全）。若默认是空串则 fallback localhost（安全）。

- [ ] **Step 4: Commit**

```bash
cd E:/project/tesla_master
git add app_mimo/android/app/src/main/java/com/matelink/data/api/ApiClient.kt
git commit -m "fix(mimo-android): ApiClient rejects cleartext public-host baseUrl (S1)"
```

---

### Task 4: S3 — iOS KeychainHelper 加 kSecAttrAccessible

**Files:**
- Modify: `ios/MateLink/App/AppState.swift:4-40` (KeychainHelper enum)

**背景:** KeychainHelper.save 的 attrs 未设 `kSecAttrAccessible`，默认 `WhenUnlocked`，且 iOS 钥匙串默认会通过 iCloud Keychain 同步到用户其他设备——token 可能跨设备泄露。改用 `kSecAttrAccessibleWhenUnlockedThisDeviceOnly`：仅本机、解锁时可访问、不参与 iCloud 同步。save 和 load 的 query 都加该属性（save 写入、load 查询需匹配，否则查不到）。

- [ ] **Step 1: 修改 save 的 attrs + load/delete 的 query**

编辑 `E:/project/tesla_master/app_mimo/ios/MateLink/App/AppState.swift`。

当前 KeychainHelper（line 4-40）：
```swift
private enum KeychainHelper {
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
            kSecValueData as String: data
        ]
        SecItemAdd(attrs as CFDictionary, nil)
    }

    static func load(_ key: String) -> String? {
        let query: [String: Any] = [
            kSecClass as String: kSecClassGenericPassword,
            kSecAttrAccount as String: key,
            kSecReturnData as String: true,
            kSecMatchLimit as String: kSecMatchLimitOne
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
```

替换为（给 save 的 attrs 和 load 的 query 加 `kSecAttrAccessible`；delete/save-delete 的 query 不需要 accessible 字段，匹配 class+account 即可）：
```swift
private enum KeychainHelper {
    // 仅本机、解锁时可访问，且不通过 iCloud 钥匙串同步，防止 token 跨设备泄露
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
```

- [ ] **Step 2: 静态验证**

Run: `grep -n "kSecAttrAccessible\|WhenUnlockedThisDeviceOnly\|accessibility" E:/project/tesla_master/app_mimo/ios/MateLink/App/AppState.swift`
Expected: 一行 `private static let accessibility = kSecAttrAccessibleWhenUnlockedThisDeviceOnly` + save attrs 一行 `kSecAttrAccessible as String: accessibility` + load query 一行 `kSecAttrAccessible as String: accessibility`。共 3 处。

Run: `grep -c "kSecAttrAccessible" E:/project/tesla_master/app_mimo/ios/MateLink/App/AppState.swift`
Expected: `3`。

- [ ] **Step 3: 确认未破坏 delete/save-delete 路径**

delete 和 save 内的 SecItemDelete 用 class+account query（无 accessible），这是正确的——删除按 class+account 匹配即可，accessible 只在 add/match 时起作用。无需改动 delete。

Run: `grep -n "SecItemDelete\|SecItemAdd\|SecItemCopyMatching" E:/project/tesla_master/app_mimo/ios/MateLink/App/AppState.swift`
Expected: 3 处调用（delete func 1 处 SecItemDelete + save func 1 处 SecItemDelete + 1 处 SecItemAdd + load func 1 处 SecItemCopyMatching = 共 4 处）。确认结构完整。

- [ ] **Step 4: Commit**

```bash
cd E:/project/tesla_master
git add app_mimo/ios/MateLink/App/AppState.swift
git commit -m "fix(mimo-ios): Keychain token WhenUnlockedThisDeviceOnly, no iCloud sync (S3)"
```

---

### Task 5: 最终静态一致性验证

**Files:** 无修改，仅验证

- [ ] **Step 1: 验证所有安全修复落地**

Run（在 `E:/project/tesla_master`）:
```bash
echo "=== S1: Manifest 无 usesCleartextTraffic ===" && grep -c "usesCleartextTraffic" app_mimo/android/app/src/main/AndroidManifest.xml
echo "=== S1: Manifest 引用 networkSecurityConfig ===" && grep -c "networkSecurityConfig" app_mimo/android/app/src/main/AndroidManifest.xml
echo "=== S2: network_security_config.xml 存在 ===" && ls app_mimo/android/app/src/main/res/xml/network_security_config.xml
echo "=== S1: UrlSecurity 工具存在 ===" && ls app_mimo/android/app/src/main/java/com/matelink/data/api/UrlSecurity.kt
echo "=== S1: ApiClient 接入 UrlSecurity ===" && grep -c "UrlSecurity.isSafe" app_mimo/android/app/src/main/java/com/matelink/data/api/ApiClient.kt
echo "=== S1: UrlSecurity 测试存在 ===" && ls app_mimo/android/app/src/test/java/com/matelink/data/api/UrlSecurityTest.kt
echo "=== S3: iOS Keychain accessible ===" && grep -c "kSecAttrAccessibleWhenUnlockedThisDeviceOnly" app_mimo/ios/MateLink/App/AppState.swift
```
Expected:
- S1 Manifest usesCleartextTraffic: `0`
- S1 Manifest networkSecurityConfig: `1`
- S2 XML: 文件存在
- S1 UrlSecurity.kt: 文件存在
- S1 ApiClient UrlSecurity.isSafe: `1`
- S1 测试: 文件存在
- S3 iOS accessible: `1`

- [ ] **Step 2: 验证 git 状态**

Run: `cd E:/project/tesla_master && git log --oneline -5 && git status --short | grep -E "app_mimo|AndroidManifest|network_security|UrlSecurity|AppState" || echo "no uncommitted app_mimo security files"`
Expected: 4 个安全修复 commit 已提交，无未提交的安全相关文件。

- [ ] **Step 3: 输出最终验证报告（交 Jovi 在能构建的环境执行）**

提示 Jovi：
1. Android: 在 Mac/Linux 跑 `cd app_mimo/android && ./gradlew :app:testDebugUnitTest --tests "com.matelink.data.api.UrlSecurityTest"` 确认 9 测试通过；再 `./gradlew :app:assembleDebug` 确认编译。
2. iOS: 在 Mac 跑 `xcodebuild -scheme MateLink build` 确认编译（Keychain accessible 改动不影响编码，仅运行时行为）。
3. 真机验证：配置 `http://192.168.1.100:4000`（私有 IP）应正常连接；配置 `http://teslamate.example.com`（公网 HTTP）应在 ApiClient 抛 IllegalArgumentException（日志可见）。

---

## Self-Review

**1. Spec coverage:** 3 项 HIGH 安全问题：S1（cleartext+baseUrl）→ Task 1（XML）+ Task 2（UrlSecurity）+ Task 3（ApiClient 接入）；S2（network_security_config）→ Task 1；S3（Keychain accessible）→ Task 4。Task 5 最终验证。全覆盖。

**2. Placeholder scan:** 每个 step 含完整代码/XML/测试 + 确切 grep 命令 + expected 输出。无 TBD。Task 1 和 Task 2 各有一处"设计修正"——明确标注先用初版创建再用修正版覆盖，因为设计在写计划时演进，修正版是最终意图。**执行者应直接用修正版内容创建文件，跳过初版**（计划中已说明"请用以下修正后内容覆盖"）。

**3. Type consistency:**
- `UrlSecurity.isSafe(baseUrl: String): Boolean` — Task 2 定义，Task 3 调用 `UrlSecurity.isSafe(baseUrl)`，签名一致 ✓
- `kSecAttrAccessibleWhenUnlockedThisDeviceOnly` 是 Security 框架常量，iOS 标准可用 ✓
- `networkSecurityConfig="@xml/network_security_config"` 引用的文件名与 Task 1 创建的 `res/xml/network_security_config.xml` 一致 ✓
- 测试用例的 URL 字符串与实现逻辑匹配（IPv4 正则 `^(\d{1,3}\.){3}\d{1,3}$` 匹配 `192.168.1.100`/`8.8.8.8`；`203.0.113.5` 是公网 → unsafe ✓；`172.31.255.255` 在 172.16/12 私有段 → siteLocal → safe ✓）

**4. 顺序依赖:**
- Task 1（XML+Manifest）独立。
- Task 2（UrlSecurity 工具+测试）独立。
- Task 3（ApiClient 接入）依赖 Task 2 的 UrlSecurity 存在。**必须 Task 2 先于 Task 3。**
- Task 4（iOS Keychain）独立。
- Task 5 最终验证依赖全部。

**5. 风险点:**
- UrlSecurity 的 IPv6 正则 `^[0-9a-fA-F:]+$` 较宽松，可能误判含冒号的非 IP 字符串为 IP 字面量并尝试 InetAddress 解析（解析失败→unsafe，行为保守安全）。可接受。
- `InetAddress.getByName` 对 IP 字面量不触发 DNS（直接解析字面量），无联网阻塞风险。✓
- 改 Keychain accessible 后，**已存在的旧 token 项（无 accessible 属性）可能查不到**（query 带 accessible 不匹配旧项）。这会导致用户首次升级后需重新输入 token。**这是已知权衡**——安全性优先。可在 release notes 提示。如需平滑迁移，可在 load 失败时 fallback 一次不带 accessible 的查询，但本计划不实现（YAGNI，按需后续补）。
- network_security_config 的 `cleartextTrafficPermitted="true"` base-config 仍允许公网 cleartext（XML 层），但 Task 3 的 ApiClient 运行时校验会拦截公网 HTTP——双层中运行时是真正生效的关卡。XML 主要提供 user CA 信任 + 显式配置。这是务实选择，已在 Task 1 注释说明。
- Android 无 gradle wrapper，单元测试需 Jovi 在能跑 gradle 的环境执行；sonnet 静态验证逻辑正确性。

**6. YAGNI 检查:** 未加证书钉扎（用户自托管，无已知固定域名，钉扎会破坏自签证书场景）。未加 OAuth 刷新流（401 TODO 保留，超出本次安全范围）。未做 Keychain 迁移 fallback（YAGNI）。
