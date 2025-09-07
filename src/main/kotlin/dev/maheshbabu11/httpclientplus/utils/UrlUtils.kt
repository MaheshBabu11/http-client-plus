
/*
 * Copyright 2025 Mahesh Babu (MaheshBabu11)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev.maheshbabu11.httpclientplus.utils

import java.net.URLDecoder
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
            val query = url.substringAfter("?", "")
            query.split("&")
                .mapNotNull { part ->
                    if (part.isBlank()) return@mapNotNull null
                    val idx = part.indexOf('=')
                    if (idx >= 0) {
                        val k = part.substring(0, idx)
                        val v = part.substring(idx + 1)
                        val key = URLDecoder.decode(k, StandardCharsets.UTF_8)
                        val value = URLDecoder.decode(v, StandardCharsets.UTF_8)
                        key to value
                    } else {
                        val key = URLDecoder.decode(part, StandardCharsets.UTF_8)
                        key to ""
                    }
                }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
