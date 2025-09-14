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

package dev.maheshbabu11.httpclientplus.importer

import com.intellij.openapi.project.Project
import dev.maheshbabu11.httpclientplus.service.HttpRequestData
import dev.maheshbabu11.httpclientplus.service.MultipartPart

class CurlImporter : RequestImporter {
    override val name = "cURL Command"

    override fun import(project: Project, input: Any): List<HttpRequestData> {
        val curl = input as? String ?: return emptyList()
        return listOfNotNull(parseCurl(curl))
    }

    private fun parseCurl(curl: String): HttpRequestData? {
        val tokens = tokenizeCurl(curl)
        if (tokens.isEmpty() || !tokens[0].equals("curl", ignoreCase = true)) return null

        var url: String? = null
        val headers = mutableListOf<Pair<String, String>>()
        var method = "GET"
        var body: String? = null
        val parts = mutableListOf<MultipartPart>()

        var i = 1
        while (i < tokens.size) {
            when (tokens[i]) {
                "-X", "--request" -> method = tokens.getOrNull(++i) ?: method
                "-H", "--header" -> {
                    val header = tokens.getOrNull(++i)?.trimQuotes()
                    header?.split(":", limit = 2)?.let {
                        if (it.size == 2) headers.add(it[0].trim() to it[1].trim())
                    }
                }

                "-d", "--data", "--data-raw", "--data-binary" -> {
                    body = tokens.getOrNull(++i)?.trimQuotes()
                    if (method == "GET") method = "POST"
                }

                "-u", "--user" -> {
                    val creds = tokens.getOrNull(++i)?.trimQuotes() ?: ""
                    val encoded = java.util.Base64.getEncoder().encodeToString(creds.toByteArray())
                    headers.add("Authorization" to "Basic $encoded")
                }

                "-F", "--form" -> {
                    val form = tokens.getOrNull(++i)?.trimQuotes() ?: continue
                    val (k, vRaw) = form.split("=", limit = 2).let { it[0] to it.getOrElse(1) { "" } }
                    val v = vRaw.trimQuotes()
                    if (v.startsWith("@")) {
                        val path = v.removePrefix("@")
                        parts.add(
                            MultipartPart(
                                name = k,
                                isFile = true,
                                filename = path.substringAfterLast('/'),
                                contentType = "application/octet-stream",
                                filePath = path
                            )
                        )
                    } else {
                        parts.add(
                            MultipartPart(
                                name = k,
                                isFile = false,
                                value = v,
                                contentType = "text/plain"
                            )
                        )
                    }
                    if (method == "GET") method = "POST"
                }

                else -> {
                    if (url == null && tokens[i].looksLikeUrl()) {
                        url = tokens[i].trimQuotes()
                    }
                }
            }
            i++
        }

        url ?: return null

        return HttpRequestData(
            method = method,
            url = url,
            headers = headers,
            saveDirPath = "http-client-plus/collections/Curl_Imports",
            body = if (parts.isEmpty()) body else null,
            name = url.substringAfterLast('/').ifBlank { "Curl_Request" },
            runImmediately = false,
            multipartParts = parts
        )
    }

    private fun String.trimQuotes(): String =
        if ((startsWith("\"") && endsWith("\"")) || (startsWith("'") && endsWith("'"))) substring(
            1,
            length - 1
        ) else this

    private fun String.looksLikeUrl(): Boolean =
        trimQuotes().startsWith("http://") || trimQuotes().startsWith("https://")

    private fun tokenizeCurl(curl: String): List<String> {
        val normalized = curl.lines().joinToString(" ") { it.trim().removeSuffix("\\") }.trim()
        val regex = Regex("""("(?:\\.|[^"])*"|'(?:\\.|[^'])*'|\S+)""")
        return regex.findAll(normalized).map { it.value }.toList()
    }
}
