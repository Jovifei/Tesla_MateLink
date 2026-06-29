package com.teslamatelink.util

import java.net.InetAddress
import java.net.URI

/**
 * Checks if a baseUrl is safe for sending Bearer tokens.
 * - HTTPS -> always safe
 * - HTTP + private/loopback/link-local IP or localhost/.local -> safe (LAN use case)
 * - HTTP + public IP/hostname -> unsafe (token exposure risk)
 * - Empty/malformed -> unsafe
 */
object UrlSecurity {

    private val IP_LITERAL = Regex("^(\\d{1,3}\\.){3}\\d{1,3}$|^[0-9a-fA-F:]+$")

    fun isSafe(baseUrl: String): Boolean {
        val trimmed = baseUrl.trim()
        if (trimmed.isEmpty()) return false
        val uri = try { URI(trimmed) } catch (_: Exception) { return false }
        val scheme = uri.scheme?.lowercase() ?: return false
        val host = uri.host ?: return false
        if (host.isEmpty()) return false

        if (scheme == "https") return true
        if (scheme != "http") return false

        if (!isIpLiteral(host)) {
            return host == "localhost" || host.endsWith(".local")
        }

        return try {
            val addr = InetAddress.getByName(host)
            addr.isLoopbackAddress || addr.isSiteLocalAddress || addr.isLinkLocalAddress
        } catch (_: Exception) {
            false
        }
    }

    private fun isIpLiteral(host: String): Boolean = IP_LITERAL.matches(host)
}
