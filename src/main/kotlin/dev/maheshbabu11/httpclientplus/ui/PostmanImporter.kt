
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

package dev.maheshbabu11.httpclientplus.ui

import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.intellij.openapi.project.Project
import dev.maheshbabu11.httpclientplus.service.HttpRequestData
import dev.maheshbabu11.httpclientplus.service.MultipartPart
import dev.maheshbabu11.httpclientplus.utils.UrlUtils
import java.io.File
import java.io.InputStream
import java.util.Base64
import kotlin.text.replace

class PostmanImporter(private val project: Project) {
    private val gson = Gson()

    fun importFromFile(file: File): List<HttpRequestData> {
        return parseCollection(file.readText())
    }

    fun importFromStream(inputStream: InputStream): List<HttpRequestData> {
        return parseCollection(inputStream.bufferedReader().use { it.readText() })
    }

    private fun parseCollection(json: String): List<HttpRequestData> {
        return try {
            val collection = gson.fromJson(json, PostmanCollection::class.java)
            flattenItems(collection.item).mapNotNull { convertToHttpRequest(it, collection.info.name) }
        } catch (e: JsonSyntaxException) {
            emptyList()
        }
    }

    private fun flattenItems(items: List<PostmanItem>): List<PostmanItem> {
        return items.flatMap { item ->
            if (item.item != null) {
                flattenItems(item.item)
            } else {
                listOf(item)
            }
        }
    }

    private fun convertToHttpRequest(item: PostmanItem, name: String): HttpRequestData? {
        val request = item.request ?: return null
        val urlParts = request.url.raw.split("?")
        val baseUrl = urlParts.firstOrNull() ?: return null
        val collectionName = if (name.isBlank()) "Postman_Collection" else
            name.replace(" ", "_")

        val params = request.url.query?.map { it.key to it.value } ?: emptyList()
        val headers = (request.header ?: emptyList()).map { it.key to it.value }.toMutableList()
        // Handle authentication
        request.auth?.let { auth ->
            when (auth.type) {
                "bearer" -> {
                    val token = auth.bearer?.find { it.key == "token" }?.value
                    token?.let {
                        headers.add("Authorization" to "Bearer $it")
                    }
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
            "file" -> null to emptyList() // Handle file uploads if needed
            else -> null to emptyList()
        }

        return HttpRequestData(
            method = request.method,
            url = if (params.isNotEmpty()) UrlUtils.buildUrlWithParams(baseUrl, params, true) else baseUrl,
            headers = headers,
            saveDirPath = "http-client-plus/collections/$collectionName",
            body = body,
            name = item.name,
            runImmediately = false,
            multipartParts = parts
        )
    }

    private fun handleFormData(formData: List<PostmanFormData>?): Pair<String?, List<MultipartPart>> {
        if (formData.isNullOrEmpty()) return null to emptyList()

        val parts = formData.map { data ->
            if (data.src != null) {
                MultipartPart(
                    name = data.key,
                    isFile = true,
                    filename = data.src.substringAfterLast('/'),
                    contentType = data.type ?: "application/octet-stream",
                    filePath = data.src
                )
            } else {
                MultipartPart(
                    name = data.key,
                    isFile = false,
                    value = data.value ?: "",
                    contentType = data.type ?: "text/plain"
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
}
