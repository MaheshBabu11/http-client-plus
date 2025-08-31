package dev.maheshbabu11.httpclientplus.ui

import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object UrlUtils {
    fun buildUrlWithParams(
        baseUrl: String,
        params: List<Pair<String, String>>,
        autoEncode: Boolean
    ): String {
        if (params.isEmpty()) return baseUrl
        val query = params
            .filter { it.first.isNotBlank() }
            .joinToString("&") { (k0, v0) ->
                val k = if (autoEncode) URLEncoder.encode(k0, StandardCharsets.UTF_8) else k0
                val v = if (autoEncode) URLEncoder.encode(v0, StandardCharsets.UTF_8) else v0
                "$k=$v"
            }
            .trim('&')
        if (query.isEmpty()) return baseUrl
        val separator = if (baseUrl.contains('?')) "&" else "?"
        return baseUrl + separator + query
    }

    fun variableizeHost(url: String): String {
        if (url.contains("{{")) return url // already variableized
        return try {
            val u = URI(url)
            val scheme = u.scheme ?: return url
            val host = u.host ?: return url
            val portPart = if (u.port != -1) ":${u.port}" else ""
            val path = (u.rawPath ?: "").ifBlank { "/" }
            val query = u.rawQuery?.let { "?$it" } ?: ""
            val fragment = u.rawFragment?.let { "#$it" } ?: ""
            "$scheme://{{host}}$portPart$path$query$fragment"
        } catch (_: Exception) {
            url
        }
    }

    fun extractParams(url: String): List<Pair<String, String>> {
        return try {
            val uri = URI(url)
            val query = uri.rawQuery ?: return emptyList()
            query.split("&")
                .mapNotNull { part ->
                    if (part.isBlank()) return@mapNotNull null
                    val idx = part.indexOf('=')
                    if (idx >= 0) {
                        val k = part.substring(0, idx)
                        val v = part.substring(idx + 1)
                        // Decode percent-encoded
                        val key = java.net.URLDecoder.decode(k, StandardCharsets.UTF_8)
                        val value = java.net.URLDecoder.decode(v, StandardCharsets.UTF_8)
                        key to value
                    } else {
                        val key = java.net.URLDecoder.decode(part, StandardCharsets.UTF_8)
                        key to ""
                    }
                }
        } catch (_: Exception) {
            emptyList()
        }
    }

}
