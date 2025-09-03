package dev.maheshbabu11.httpclientplus.ui

import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

object UrlUtils {
    private fun shouldSkipEncoding(s: String): Boolean {
        // Skip encoding for templated placeholders like {{var}}
        return s.contains("{{")
    }

    private fun encodeIfNeeded(s: String, autoEncode: Boolean): String {
        if (!autoEncode) return s
        if (shouldSkipEncoding(s)) return s
        return URLEncoder.encode(s, StandardCharsets.UTF_8)
    }

    fun buildUrlWithParams(
        baseUrl: String,
        params: List<Pair<String, String>>,
        autoEncode: Boolean
    ): String {
        if (params.isEmpty()) return baseUrl
        val query = params
            .filter { it.first.isNotBlank() }
            .joinToString("&") { (k0, v0) ->
                val k = encodeIfNeeded(k0, autoEncode)
                val v = encodeIfNeeded(v0, autoEncode)
                "$k=$v"
            }
            .trim('&')
        if (query.isEmpty()) return baseUrl
        val separator = if (baseUrl.contains('?')) "&" else "?"
        return baseUrl + separator + query
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
