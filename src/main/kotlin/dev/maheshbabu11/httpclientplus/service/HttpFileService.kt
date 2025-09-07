
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

package dev.maheshbabu11.httpclientplus.service

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiManager
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object HttpFileService {
    private val fileNameFormatter = DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")

    // Base: collections directory under project
    private fun collectionsDir(project: Project): Path? {
        val basePath = project.basePath ?: return null
        return Path.of(basePath, "http-client-plus", "collections")
    }

    // Overload: use saveDirPath if provided, otherwise default collections dir
    private fun collectionsDir(project: Project, saveDirPath: String?): Path? {
        val basePath = project.basePath ?: return null
        return if (!saveDirPath.isNullOrBlank()) {
            Path.of(basePath, saveDirPath)
        } else {
            collectionsDir(project)
        }
    }

    // Helper: sanitize a logical request name to a file-friendly stem
    private fun sanitize(name: String): String =
        name.trim().replace(Regex("[^a-zA-Z0-9-_]"), "_")

    // Resolve full path for a given logical name in default collection
    private fun resolvePathForName(project: Project, name: String): Path? {
        val dir = collectionsDir(project) ?: return null
        return dir.resolve("${sanitize(name)}.http")
    }

    // Resolve full path using custom dir
    private fun resolvePathForName(project: Project, name: String, saveDirPath: String?): Path? {
        val dir = collectionsDir(project, saveDirPath) ?: return null
        return dir.resolve("${sanitize(name)}.http")
    }

    // Find existing .http file by logical name
    @Suppress("unused")
    fun findRequestFileByName(project: Project, name: String): VirtualFile? {
        val path = resolvePathForName(project, name) ?: return null
        if (!Files.exists(path)) return null
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(path.toString())
    }

    // Overload with custom dir
    fun findRequestFileByName(project: Project, name: String, saveDirPath: String?): VirtualFile? {
        val path = resolvePathForName(project, name, saveDirPath) ?: return null
        if (!Files.exists(path)) return null
        return LocalFileSystem.getInstance().refreshAndFindFileByPath(path.toString())
    }

    // Create or update the .http file without opening or running it
    fun createRequestFile(project: Project, data: HttpRequestData): VirtualFile? =
        createOrUpdateHttpFile(project, data)

    // Update an existing .http file in place
    fun updateRequestFile(project: Project, vFile: VirtualFile, data: HttpRequestData): VirtualFile? {
        return try {
            val content = buildHttpContent(data)
            val path = Path.of(vFile.path)
            Files.writeString(path, content, StandardCharsets.UTF_8)
            LocalFileSystem.getInstance().refreshAndFindFileByPath(path.toString())?.also { refreshed ->
                ApplicationManager.getApplication().runReadAction {
                    PsiManager.getInstance(project).findFile(refreshed)
                }
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun createOrUpdateHttpFile(project: Project, data: HttpRequestData): VirtualFile? {
        val dirPath = collectionsDir(project, data.saveDirPath) ?: return null
        return try {
            Files.createDirectories(dirPath)
            val stem = data.name?.takeIf { it.isNotBlank() }?.let { sanitize(it) }
                ?: fileNameFormatter.format(LocalDateTime.now())
            val filePath = dirPath.resolve("$stem.http")
            val content = buildHttpContent(data)
            Files.writeString(filePath, content, StandardCharsets.UTF_8)
            LocalFileSystem.getInstance().refreshAndFindFileByPath(filePath.toString())?.also { vFile ->
                ApplicationManager.getApplication().runReadAction {
                    PsiManager.getInstance(project).findFile(vFile) // warm PSI
                }
            }
        } catch (_: Exception) {
            null
        }
    }


    private fun buildHttpContent(data: HttpRequestData): String {
        val sb = StringBuilder()
        val name = data.name?.takeIf { it.isNotBlank() }
        if (name != null) sb.append("### ").append(name).append('\n')

        // Directives
        if (data.noRedirect) sb.append("# @no-redirect\n")
        if (data.noCookieJar) sb.append("# @no-cookie-jar\n")
        if (data.noAutoEncoding) sb.append("# @no-auto-encoding\n")

        data.preExecutionScript?.takeIf { it.isNotBlank() }?.let { script ->
            if (!script.startsWith("<")) sb.append("< ")
            sb.append(script)
            if (sb.isEmpty() || sb[sb.length - 1] != '\n') sb.append('\n')
        }

        val method = data.method.trim().uppercase().ifBlank { "GET" }
        sb.append(method).append(' ').append(data.url)
        val httpVersionToken = data.httpVersion?.takeIf { it.isNotBlank() }
        if (httpVersionToken != null) sb.append(' ').append(httpVersionToken)
        sb.append('\n')

        // Prepare headers
        val headers: MutableList<Pair<String, String>> = data.headers.toMutableList()
        val isMultipart = data.multipartParts.isNotEmpty() || headers.any {
            it.first.equals("Content-Type", true) &&
                    it.second.contains("multipart/form-data", true)
        }
        if (method == "POST" && !isMultipart) {
            val hasContentType = headers.any { it.first.equals("Content-Type", ignoreCase = true) }
            if (!hasContentType && !data.body.isNullOrBlank()) {
                headers += ("Content-Type" to "application/json")
            }
        }

        // Multipart Content-Type
        val boundary: String? =
            if (isMultipart) (data.multipartBoundary?.takeIf { it.isNotBlank() } ?: "WebAppBoundary") else null
        if (isMultipart) {
            val idx = headers.indexOfFirst { it.first.equals("Content-Type", true) }
            val ctValue = "multipart/form-data; boundary=${boundary}"
            if (idx >= 0) headers[idx] = ("Content-Type" to ctValue)
            else headers += ("Content-Type" to ctValue)
        }

        for ((k, v) in headers) {
            sb.append(k).append(": ").append(v).append('\n')
        }
        sb.append('\n')

        if (isMultipart) {
            val effectiveBoundary = boundary ?: "WebAppBoundary"
            data.multipartParts.forEach { part ->
                sb.append("--").append(effectiveBoundary).append('\n')
                sb.append("Content-Disposition: form-data; name=\"")
                    .append(part.name).append("\"")
                if (part.isFile) {
                    val fn = (part.filename ?: part.filePath?.substringAfterLast('/') ?: "file")
                    sb.append("; filename=\"").append(fn).append("\"")
                }
                sb.append('\n')
                val pct = part.contentType?.takeIf { it.isNotBlank() }
                if (pct != null) sb.append("Content-Type: ").append(pct).append('\n')
                sb.append('\n')
                if (part.isFile) {
                    val p = part.filePath?.takeIf { it.isNotBlank() } ?: ""
                    sb.append("< ").append(p).append('\n')
                } else {
                    sb.append(part.value.orEmpty()).append('\n')
                }
            }
            sb.append("--").append(effectiveBoundary).append("--\n")
            data.postExecutionScript?.takeIf { it.isNotBlank() }?.let { script ->
                if (!script.startsWith(">")) sb.append("> ")
                sb.append(script)
                if (sb.isEmpty() || sb[sb.length - 1] != '\n') sb.append('\n')
            }
            return sb.toString()
        }

        if (!data.body.isNullOrBlank()) {
            sb.append(data.body).append('\n')
        }
        data.postExecutionScript?.takeIf { it.isNotBlank() }?.let { script ->
            if (!script.startsWith(">")) sb.append("> ")
            sb.append(script)
            if (sb.isEmpty() || sb[sb.length - 1] != '\n') sb.append('\n')
        }
        if (!data.responseSavePath.isNullOrBlank()) {
            if (data.forceSave) {
                sb.append(">>! ")
            } else {
                sb.append(">> ")
            }
            sb.append(data.responseSavePath).append('\n')
        }
        sb.append('\n')
        return sb.toString()
    }

    fun parseRequestFile(project: Project, vFile: VirtualFile): HttpRequestData? {
        return try {
            val lines = Files.readAllLines(Path.of(vFile.path), StandardCharsets.UTF_8)

            var name: String? = null
            var method = "GET"
            var url = ""
            var httpVersion: String? = null
            val headers = mutableListOf<Pair<String, String>>()
            val bodyLines = mutableListOf<String>()
            val multipartParts = mutableListOf<MultipartPart>()
            var preScript: String? = null
            var postScript: String? = null
            var noRedirect = false
            var noCookieJar = false
            var noAutoEncoding = false
            var responseFileName: String? = null
            var forceSave = false

            // Parsing state
            var seenRequestLine = false
            var inHeaders = false
            var inBody = false

            var collectingPre = false
            var collectingPost = false
            val preBuf = StringBuilder()
            val postBuf = StringBuilder()

            fun isMethodLine(t: String) =
                Regex("^(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS)\\s+", RegexOption.IGNORE_CASE).containsMatchIn(t)

            fun endOfScript(line: String): Boolean =
                line.trimEnd().endsWith("%}") || line.trim() == "%}"

            for (raw in lines) {
                val line = raw
                val t = line.trimStart()

                // If we're inside a script block, keep collecting until %} (inclusive)
                if (collectingPre) {
                    preBuf.appendLine(line)
                    if (endOfScript(line)) {
                        collectingPre = false
                        preScript = preBuf.toString().trimEnd()
                    }
                    continue
                }
                if (collectingPost) {
                    postBuf.appendLine(line)
                    if (endOfScript(line)) {
                        collectingPost = false
                        postScript = postBuf.toString().trimEnd()
                    }
                    continue
                }

                when {
                    t.startsWith("###") -> name = t.removePrefix("###").trim()

                    t.startsWith("# @no-redirect") -> noRedirect = true
                    t.startsWith("# @no-cookie-jar") -> noCookieJar = true
                    t.startsWith("# @no-auto-encoding") -> noAutoEncoding = true

                    isMethodLine(t) -> {
                        val parts = t.split(Regex("\\s+"))
                        method = parts[0].uppercase()
                        url = parts.getOrNull(1) ?: ""
                        httpVersion = parts.getOrNull(2)
                        seenRequestLine = true
                        inHeaders = true
                        inBody = false
                    }

                    t.startsWith(">>") -> {
                        val path = t.removePrefix(">>").trim()
                        forceSave = path.startsWith("!")
                        responseFileName = path
                            .removePrefix("!")
                            .substringAfterLast('/')
                            .substringBeforeLast('.')
                            .substringBeforeLast('-')
                    }

                    // Start of pre/post script blocks
                    t.startsWith("<") -> {
                        collectingPre = true
                        preBuf.setLength(0)
                        preBuf.appendLine(line)
                        if (endOfScript(line)) { // one-liner case
                            collectingPre = false
                            preScript = preBuf.toString().trimEnd()
                        }
                    }

                    t.startsWith(">") -> {
                        collectingPost = true
                        postBuf.setLength(0)
                        postBuf.appendLine(line)
                        if (endOfScript(line)) { // one-liner case
                            collectingPost = false
                            postScript = postBuf.toString().trimEnd()
                        }
                    }

                    // Blank-line only flips to body after we've seen the request line and are in headers
                    line.isBlank() -> {
                        if (seenRequestLine && inHeaders) {
                            inHeaders = false
                            inBody = true
                        }
                        // else: ignore stray blank lines (e.g., above request or inside scripts handled earlier)
                    }

                    // Headers (before first blank after method line)
                    inHeaders && !inBody -> {
                        val idx = line.indexOf(':')
                        if (idx > 0) {
                            headers += line.substring(0, idx).trim() to line.substring(idx + 1).trim()
                        }
                    }

                    // Body (after headers → body switch)
                    inBody -> bodyLines += line

                    // Anything else before the request line (comments/whitespace already handled): ignore
                    else -> Unit
                }
            }

            // EOF: if a script block never closed, still use the collected content
            if (collectingPre && preBuf.isNotEmpty()) preScript = preBuf.toString().trimEnd()
            if (collectingPost && postBuf.isNotEmpty()) postScript = postBuf.toString().trimEnd()

            val body = if (bodyLines.isNotEmpty()) bodyLines.joinToString("\n") else null

            // Detect boundary from Content-Type
            val ctHeader = headers.firstOrNull { it.first.equals("Content-Type", true) }?.second
            var boundary: String? = null
            if (ctHeader != null && ctHeader.contains("multipart/form-data", true)) {
                boundary = Regex("boundary=([^;]+)").find(ctHeader)?.groupValues?.get(1)?.trim()
            }

            // Multipart parsing (use body lines only)
            if (boundary != null && body != null) {
                val parts = mutableListOf<MultipartPart>()
                var currentName: String? = null
                var currentFilename: String? = null
                var currentContentType: String? = null
                val valueBuffer = StringBuilder()
                var isFile = false
                var filePath: String? = null
                var inPart = false
                var inPartHeaders = false
                var inPartBody = false

                fun flushPart() {
                    if (currentName != null) {
                        parts += MultipartPart(
                            name = currentName!!,
                            isFile = isFile,
                            filename = currentFilename,
                            contentType = currentContentType,
                            value = if (!isFile) valueBuffer.toString().trimEnd() else null,
                            filePath = if (isFile) filePath else null
                        )
                    }
                    currentName = null
                    currentFilename = null
                    currentContentType = null
                    valueBuffer.setLength(0)
                    isFile = false
                    filePath = null
                    inPartHeaders = false
                    inPartBody = false
                }

                for (line in bodyLines) {
                    when {
                        line.startsWith("--$boundary") -> {
                            if (inPart) flushPart()
                            if (line.trimEnd().endsWith("--")) {
                                inPart = false
                                break
                            }
                            inPart = true
                            inPartHeaders = true
                            inPartBody = false
                        }

                        inPart && inPartHeaders && line.startsWith("Content-Disposition:", ignoreCase = true) -> {
                            val nameMatch = Regex("name=\"([^\"]+)\"").find(line)
                            currentName = nameMatch?.groupValues?.get(1)
                            val fileMatch = Regex("filename=\"([^\"]+)\"").find(line)
                            if (fileMatch != null) {
                                currentFilename = fileMatch.groupValues[1]
                                isFile = true
                            }
                        }

                        inPart && inPartHeaders && line.startsWith("Content-Type:", ignoreCase = true) -> {
                            currentContentType = line.removePrefix("Content-Type:").trim()
                        }

                        inPart && inPartHeaders && line.isBlank() -> {
                            // End of part headers → body
                            inPartHeaders = false
                            inPartBody = true
                        }

                        inPart && inPartBody && line.startsWith("< ") -> {
                            // file path reference syntax
                            filePath = line.removePrefix("< ").trim()
                        }

                        inPart && inPartBody -> {
                            if (!isFile) valueBuffer.appendLine(line)
                        }
                    }
                }
                flushPart()
                multipartParts.addAll(parts)
            }

            // Compute relative saveDirPath
            val basePath = project.basePath ?: ""
            val parentPath = vFile.parent.path
            val relativeSaveDir = if (parentPath.startsWith(basePath)) {
                parentPath.removePrefix(basePath).trimStart('/', '\\')
            } else {
                parentPath
            }

            HttpRequestData(
                method = method,
                url = url,
                headers = headers,
                body = body,
                name = name,
                runImmediately = false,
                multipartBoundary = boundary,
                multipartParts = multipartParts,
                saveDirPath = relativeSaveDir,
                noRedirect = noRedirect,
                noCookieJar = noCookieJar,
                noAutoEncoding = noAutoEncoding,
                httpVersion = httpVersion,
                preExecutionScript = preScript,
                postExecutionScript = postScript,
                responseSavePath = responseFileName,
                forceSave = forceSave
            )
        } catch (_: Exception) {
            null
        }
    }

}
