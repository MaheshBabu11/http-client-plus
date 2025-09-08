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

package dev.maheshbabu11.httpclientplus.service

data class HttpRequestData(
    val method: String,
    val url: String,
    val headers: List<Pair<String, String>> = emptyList(),
    val body: String? = null,
    val name: String? = null,
    val runImmediately: Boolean = true,
    // Multipart support
    val multipartBoundary: String? = null,
    val multipartParts: List<MultipartPart> = emptyList(),
    // Optional: directory where to save the .http file
    val saveDirPath: String? = null,
    // Settings / directives
    val noRedirect: Boolean = false,
    val noCookieJar: Boolean = false,
    val noAutoEncoding: Boolean = false,
    // HTTP version to append to request line (e.g., "HTTP/2"); null = default (no version emitted)
    val httpVersion: String? = null,
    // Optional pre-execution script to append before the request (e.g., "{% request.variables.set(...) %} ")
    val preExecutionScript: String? = null,
    // Optional response handler script to append after the request (e.g., "> {% client.global.set(...) %}")
    val postExecutionScript: String? = null,
    val responseSavePath: String? = null,
    val forceSave: Boolean = false
)

// Represents a single multipart/form-data part
data class MultipartPart(
    val name: String,
    val isFile: Boolean,
    val filename: String? = null,
    val contentType: String? = null,
    val value: String? = null,       // for text parts
    val filePath: String? = null     // for file parts; relative or absolute
)
