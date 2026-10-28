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

import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import dev.maheshbabu11.httpclientplus.service.HttpRequestData
import dev.maheshbabu11.httpclientplus.utils.UrlUtils
import java.io.File
import java.io.InputStream

class OpenApiImporter : RequestImporter {
    override val name = "OpenAPI / Swagger (JSON)"

    override fun import(project: Project, input: Any): List<HttpRequestData> {
        val text = when (input) {
            is File -> input.readText()
            is InputStream -> input.bufferedReader().use { it.readText() }
            is VirtualFile -> input.inputStream.bufferedReader().use { it.readText() }
            is String -> input
            else -> throw IllegalArgumentException("Unsupported OpenAPI input")
        }
        return parseDocument(text)
    }

    private fun parseDocument(text: String): List<HttpRequestData> {
        val document = try {
            JsonParser.parseString(text).asJsonObject
        } catch (e: Exception) {
            throw IllegalArgumentException("The selected file is not valid JSON", e)
        }
        val openApi3 = document.string("openapi")?.startsWith("3.") == true
        val swagger2 = document.string("swagger") == "2.0"
        require(openApi3 || swagger2) { "Only OpenAPI 3.x and Swagger 2.0 JSON documents are supported" }

        val title = document.objectAt("info")?.string("title")
            ?.takeIf { it.isNotBlank() } ?: "OpenAPI_Import"
        val baseUrl = if (openApi3) openApi3BaseUrl(document) else swagger2BaseUrl(document)
        val paths = document.objectAt("paths") ?: return emptyList()
        val usedNames = mutableSetOf<String>()

        return buildList {
            paths.entrySet().forEach { (path, pathItemElement) ->
                val pathItem = pathItemElement.asJsonObject
                val pathParameters = pathItem.arrayAt("parameters").orEmpty()
                pathItem.entrySet()
                    .filter { (key, value) -> key.lowercase() in HTTP_METHODS && value.isJsonObject }
                    .forEach { (method, operationElement) ->
                        val operation = operationElement.asJsonObject
                        val fullPath = (baseUrl.trimEnd('/') + "/" + path.trimStart('/'))
                            .replace(Regex("""\{([^}/]+)}""")) { "{{${it.groupValues[1]}}}" }
                        val parameters = mergeParameters(pathParameters, operation.arrayAt("parameters").orEmpty())
                        val queryParams = mutableListOf<Pair<String, String>>()
                        val headers = mutableListOf<Pair<String, String>>()

                        parameters.forEach { parameter ->
                            val name = parameter.string("name") ?: return@forEach
                            val location = parameter.string("in") ?: return@forEach
                            val value = parameterValue(parameter)
                            when (location) {
                                "query" -> queryParams += name to value
                                "header" -> headers += name to value
                            }
                        }

                        val (body, contentType) = if (openApi3) {
                            openApi3Body(operation, document)
                        } else {
                            swagger2Body(operation, document)
                        }
                        contentType?.let { headers += "Content-Type" to it }

                        val rawName = operation.string("operationId")
                            ?.takeIf { it.isNotBlank() }
                            ?: operation.string("summary")?.takeIf { it.isNotBlank() }
                            ?: "${method.uppercase()}_${path.trim('/').replace('/', '_')}"
                        val requestName = uniqueName(rawName, usedNames)
                        add(
                            HttpRequestData(
                                method = method.uppercase(),
                                url = UrlUtils.buildUrlWithParams(fullPath, queryParams, true),
                                headers = headers,
                                body = body,
                                name = requestName,
                                runImmediately = false,
                                saveDirPath = "http-client-plus/collections/${safeName(title)}"
                            )
                        )
                    }
            }
        }
    }

    private fun openApi3BaseUrl(document: JsonObject): String {
        val server = document.arrayAt("servers")?.firstOrNull()?.asJsonObject ?: return ""
        var url = server.string("url").orEmpty()
        val variables = server.objectAt("variables")
        variables?.entrySet()?.forEach { (name, value) ->
            val defaultValue = value.asJsonObject.string("default")
            if (defaultValue != null) url = url.replace("{$name}", defaultValue)
        }
        return url
    }

    private fun swagger2BaseUrl(document: JsonObject): String {
        val scheme = document.arrayAt("schemes")?.firstOrNull()?.asString ?: "https"
        val host = document.string("host").orEmpty()
        val basePath = document.string("basePath").orEmpty()
        return if (host.isBlank()) basePath else "$scheme://$host${basePath.takeUnless { it == "/" }.orEmpty()}"
    }

    private fun mergeParameters(
        pathParameters: List<JsonElement>,
        operationParameters: List<JsonElement>
    ): List<JsonObject> {
        val merged = linkedMapOf<String, JsonObject>()
        (pathParameters + operationParameters).forEach { element ->
            val parameter = element.asJsonObject
            val key = "${parameter.string("in")}:${parameter.string("name")}"
            merged[key] = parameter
        }
        return merged.values.toList()
    }

    private fun parameterValue(parameter: JsonObject): String {
        val schema = parameter.objectAt("schema")
        val example = parameter.get("example")
            ?: schema?.get("example")
            ?: schema?.get("default")
            ?: parameter.get("default")
            ?: schema?.arrayAt("enum")?.firstOrNull()
        return example?.asText() ?: "{{${parameter.string("name").orEmpty()}}}"
    }

    private fun openApi3Body(operation: JsonObject, document: JsonObject): Pair<String?, String?> {
        val content = operation.objectAt("requestBody")
            ?.objectAt("content") ?: return null to null
        val mediaType = content.get("application/json")?.asJsonObject?.let { "application/json" to it }
            ?: content.entrySet().firstOrNull()?.let { it.key to it.value.asJsonObject }
            ?: return null to null
        val mediaObject = mediaType.second
        val example = mediaObject.get("example")
            ?: mediaObject.objectAt("examples")?.entrySet()?.firstOrNull()?.value
                ?.asJsonObject?.get("value")
        val body = example?.asText() ?: mediaObject.objectAt("schema")
            ?.let { sampleValue(it, document, mutableSetOf())?.toString() }
            ?: "{}"
        return body to mediaType.first
    }

    private fun swagger2Body(operation: JsonObject, document: JsonObject): Pair<String?, String?> {
        val bodyParameter = operation.arrayAt("parameters")
            ?.firstOrNull { it.asJsonObject.string("in") == "body" }
            ?.asJsonObject ?: return null to null
        val schema = bodyParameter.objectAt("schema") ?: return null to null
        val contentType = operation.arrayAt("consumes")?.firstOrNull()?.asString ?: "application/json"
        return (sampleValue(schema, document, mutableSetOf())?.toString() ?: "{}") to contentType
    }

    private fun sampleValue(
        schema: JsonObject,
        document: JsonObject,
        resolving: MutableSet<String>
    ): JsonElement? {
        schema.get("\$ref")?.asString?.let { ref ->
            if (!resolving.add(ref)) return null
            val resolved = resolveLocalReference(document, ref)
            val value = resolved?.let { sampleValue(it, document, resolving) }
            resolving.remove(ref)
            return value
        }
        schema.get("example")?.let { return it }
        schema.get("default")?.let { return it }

        return when (schema.string("type")) {
            "object" -> {
                val properties = schema.objectAt("properties") ?: return JsonObject()
                JsonObject().apply {
                    properties.entrySet().forEach { (name, property) ->
                        if (property.isJsonObject) {
                            sampleValue(property.asJsonObject, document, resolving)?.let { add(name, it) }
                        }
                    }
                }
            }
            "array" -> JsonArray().apply {
                schema.objectAt("items")?.let { sampleValue(it, document, resolving) }?.let(::add)
            }
            "integer", "number" -> com.google.gson.JsonPrimitive(0)
            "boolean" -> com.google.gson.JsonPrimitive(false)
            "string" -> com.google.gson.JsonPrimitive("")
            else -> if (schema.has("properties")) {
                sampleValue(JsonObject().apply {
                    addProperty("type", "object")
                    add("properties", schema.get("properties"))
                }, document, resolving)
            } else null
        }
    }

    private fun resolveLocalReference(document: JsonObject, reference: String): JsonObject? {
        if (!reference.startsWith("#/")) return null
        return reference.removePrefix("#/").split('/')
            .fold(document as JsonElement?) { current, segment ->
                current?.takeIf { it.isJsonObject }?.asJsonObject
                    ?.get(segment.replace("~1", "/").replace("~0", "~"))
            }
            ?.takeIf { it.isJsonObject }?.asJsonObject
    }

    private fun uniqueName(candidate: String, used: MutableSet<String>): String {
        val base = safeName(candidate).ifBlank { "Request" }
        var result = base
        var suffix = 2
        while (!used.add(result)) result = "${base}_${suffix++}"
        return result
    }

    private fun safeName(value: String): String =
        value.trim().replace(Regex("[^a-zA-Z0-9_-]"), "_").trim('_').ifBlank { "OpenAPI_Import" }

    private fun JsonObject.string(name: String): String? =
        get(name)?.takeIf { it.isJsonPrimitive && it.asJsonPrimitive.isString }?.asString

    private fun JsonObject.objectAt(name: String): JsonObject? =
        get(name)?.takeIf { it.isJsonObject }?.asJsonObject

    private fun JsonObject.arrayAt(name: String): List<JsonElement>? =
        get(name)?.takeIf { it.isJsonArray }?.asJsonArray?.toList()

    private fun JsonElement.asText(): String =
        if (isJsonPrimitive && asJsonPrimitive.isString) asString else toString()

    companion object {
        private val HTTP_METHODS = setOf("get", "post", "put", "patch", "delete", "head", "options", "trace")
    }
}
