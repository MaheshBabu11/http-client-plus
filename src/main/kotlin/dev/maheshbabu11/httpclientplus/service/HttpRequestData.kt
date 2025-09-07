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
    // Optional response handler script to append after the request (e.g., "> {% client.global.set(...) %}")
    val responseHandlerScript: String? = null,
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
