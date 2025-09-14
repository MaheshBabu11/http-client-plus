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

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.maheshbabu11.httpclientplus.service.HttpRequestData
import dev.maheshbabu11.httpclientplus.service.MultipartPart
import dev.maheshbabu11.httpclientplus.ui.PostmanCollection
import dev.maheshbabu11.httpclientplus.ui.PostmanFormData
import dev.maheshbabu11.httpclientplus.ui.PostmanItem
import dev.maheshbabu11.httpclientplus.ui.PostmanUrl
import dev.maheshbabu11.httpclientplus.ui.PostmanUrlEncoded
import dev.maheshbabu11.httpclientplus.utils.UrlUtils
import java.io.File
import java.io.InputStream
import java.util.Base64

class PostmanImporter : RequestImporter {
    override val name = "Postman Collection"
    private val gson = Gson()

    override fun import(project: Project, input: Any): List<HttpRequestData> {
        val text = when (input) {
            is File -> input.readText()
            is InputStream -> input.bufferedReader().use { it.readText() }
            is VirtualFile -> input.inputStream.bufferedReader().use { it.readText() }
            is String -> input
            else -> return emptyList()
        }
        return parseCollection(text)
    }

    private fun parseCollection(json: String): List<HttpRequestData> {
        return try {
            val collection = gson.fromJson(json, PostmanCollection::class.java)
            flattenItems(collection.item)
                .mapNotNull { convertToHttpRequest(it, collection.info.name) }
        } catch (_: JsonSyntaxException) {
            emptyList()
        }
    }

    private fun flattenItems(items: List<PostmanItem>): List<PostmanItem> =
        items.flatMap { if (it.item != null) flattenItems(it.item) else listOf(it) }

    private fun convertToHttpRequest(item: PostmanItem, name: String): HttpRequestData? {
        val request = item.request ?: return null
        val urlObj = request.url ?: return null

        val raw = urlObj.raw?.takeIf { it.isNotBlank() }
        val baseUrl = raw ?: buildUrlFromParts(urlObj) ?: return null

        val params = urlObj.query?.map { it.key to it.value } ?: emptyList()
        val headers = (request.header ?: emptyList()).map { it.key to it.value }.toMutableList()

        // Authentication
        request.auth?.let { auth ->
            when (auth.type) {
                "bearer" -> {
                    val token = auth.bearer?.find { it.key == "token" }?.value
                    token?.let { headers.add("Authorization" to "Bearer $it") }
                }

                "basic" -> {
                    val username = auth.basic?.find { it.key == "username" }?.value ?: ""
                    val password = auth.basic?.find { it.key == "password" }?.value ?: ""
                    if (username.isNotBlank() || password.isNotBlank()) {
                        val credentials = "$username:$password"
                        val encoded = Base64.getEncoder().encodeToString(credentials.toByteArray())
                        headers.add("Authorization" to "Basic $encoded")
                    }
                }
            }
        }

        val (body, parts) = when (request.body?.mode) {
            "raw" -> request.body.raw to emptyList()
            "formdata" -> handleFormData(request.body.formdata)
            "urlencoded" -> handleUrlEncoded(request.body.urlencoded)
            "file" -> null to emptyList()
            else -> null to emptyList()
        }

        return HttpRequestData(
            method = request.method,
            url = if (params.isNotEmpty())
                UrlUtils.buildUrlWithParams(baseUrl, params, true)
            else baseUrl,
            headers = headers,
            saveDirPath = "http-client-plus/collections/${
                if (name.isBlank()) "Postman_Collection" else name.replace(
                    " ",
                    "_"
                )
            }",
            body = body,
            name = item.name,
            runImmediately = false,
            multipartParts = parts
        )
    }

    private fun handleFormData(formData: List<PostmanFormData>?): Pair<String?, List<MultipartPart>> {
        if (formData.isNullOrEmpty()) return null to emptyList()
        val parts = mutableListOf<MultipartPart>()

        for (d in formData) {
            val isFile = d.type.equals("file", ignoreCase = true) || d.src != null
            if (isFile) {
                val firstPath = when {
                    d.src?.isJsonArray == true -> d.src.asJsonArray.firstOrNull()?.asString
                    d.src?.isJsonPrimitive == true && d.src.asJsonPrimitive.isString -> d.src.asString
                    else -> null
                }
                if (!firstPath.isNullOrBlank()) {
                    parts.add(
                        MultipartPart(
                            name = d.key,
                            isFile = true,
                            filename = firstPath.substringAfterLast('/'),
                            contentType = d.contentType ?: "application/octet-stream",
                            filePath = firstPath
                        )
                    )
                }
            } else {
                parts.add(
                    MultipartPart(
                        name = d.key,
                        isFile = false,
                        value = d.value ?: "",
                        contentType = d.contentType ?: "text/plain"
                    )
                )
            }
        }
        return null to parts
    }

    private fun handleUrlEncoded(urlEncoded: List<PostmanUrlEncoded>?): Pair<String?, List<MultipartPart>> {
        if (urlEncoded.isNullOrEmpty()) return null to emptyList()
        val body = urlEncoded.joinToString("&") { "${it.key}=${it.value}" }
        return body to emptyList()
    }

    private fun buildUrlFromParts(url: PostmanUrl): String? {
        val protocol = url.protocol?.takeIf { it.isNotBlank() }
        val host = url.host?.filter { it.isNotBlank() }?.joinToString(".")?.takeIf { it.isNotBlank() }
        val path = url.path?.filter { it.isNotBlank() }?.joinToString("/")?.takeIf { it.isNotBlank() }
        val base = buildString {
            if (protocol != null) append(protocol).append("://")
            if (host != null) append(host)
            if (path != null) {
                if (host != null && !path.startsWith('/')) append('/')
                append(path)
            }
        }
        return base.ifBlank { null }
    }
}
