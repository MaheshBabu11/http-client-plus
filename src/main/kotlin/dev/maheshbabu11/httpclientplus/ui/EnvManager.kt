
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

import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import com.google.gson.reflect.TypeToken
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.LocalFileSystem
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path

/**
 * Manages public/private environment files.
 * Location: <project>/http-client-plus/requests/environments/
 *  - http-client.env.json (public)
 *  - http-client.private.env.json (private)
 *
 */
object EnvManager {
    private const val PUBLIC_FILE_NAME = "http-client.env.json"
    private const val PRIVATE_FILE_NAME = "http-client.private.env.json"

    private val gson = GsonBuilder().setPrettyPrinting().create()

    data class EnvFiles(val publicPath: Path, val privatePath: Path)

    private fun baseEnvDir(project: Project): Path? =
        project.basePath?.let { Path.of(it).resolve("http-client-plus").resolve("environments") }

    fun envFiles(project: Project): EnvFiles? {
        val dir = baseEnvDir(project) ?: return null
        return EnvFiles(dir.resolve(PUBLIC_FILE_NAME), dir.resolve(PRIVATE_FILE_NAME))
    }

    fun loadPublic(project: Project): MutableMap<String, MutableMap<String, Any>> =
        load(envFiles(project)?.publicPath)

    fun loadPrivate(project: Project): MutableMap<String, MutableMap<String, Any>> =
        load(envFiles(project)?.privatePath)

    fun loadMerged(project: Project): Map<String, Map<String, Any>> {
        val pub = loadPublic(project)
        val prv = loadPrivate(project)
        val merged = mutableMapOf<String, MutableMap<String, Any>>()
        (pub.keys + prv.keys).forEach { env ->
            val dst = mutableMapOf<String, Any>()
            pub[env]?.let { dst.putAll(it) }
            prv[env]?.let { dst.putAll(it) } // private overrides
            merged[env] = dst
        }
        return merged
    }

    private fun load(path: Path?): MutableMap<String, MutableMap<String, Any>> {
        if (path == null || !Files.exists(path)) return mutableMapOf()
        return try {
            val text = Files.readString(path, StandardCharsets.UTF_8)
            if (text.isBlank()) return mutableMapOf()
            val type = object : TypeToken<MutableMap<String, MutableMap<String, Any>>>() {}.type
            gson.fromJson(text, type) ?: mutableMapOf()
        } catch (_: Exception) {
            mutableMapOf()
        }
    }

    fun savePublic(project: Project, data: Map<String, Map<String, Any>>): Boolean =
        saveSingle(envFiles(project)?.publicPath, data)

    fun savePrivate(project: Project, data: Map<String, Map<String, Any>>): Boolean =
        saveSingle(envFiles(project)?.privatePath, data)

    private fun saveSingle(path: Path?, data: Map<String, Map<String, Any>>): Boolean {
        if (path == null) return false
        return try {
            val parent = path.parent
            if (parent != null) Files.createDirectories(parent)
            val json = gson.toJson(fromNestedMap(data))
            Files.writeString(path, json, StandardCharsets.UTF_8)
            // Refresh so changes appear immediately
            LocalFileSystem.getInstance().refreshAndFindFileByPath(path.toString())
            true
        } catch (_: Exception) {
            false
        }
    }

    private fun fromNestedMap(map: Map<String, Map<String, Any>>): JsonObject {
        val root = JsonObject()
        map.forEach { (env, vars) ->
            val obj = JsonObject()
            vars.forEach { (k, v) ->
                when (v) {
                    is Number -> obj.addProperty(k, v)
                    is Boolean -> obj.addProperty(k, v)
                    else -> obj.addProperty(k, v.toString())
                }
            }
            root.add(env, obj)
        }
        return root
    }

    fun getEnvNames(project: Project): List<String> = loadMerged(project).keys.sorted()

    fun hasHost(project: Project, envName: String): Boolean =
        (loadMerged(project)[envName] ?: emptyMap()).containsKey("host")

    fun getEnvVar(project: Project, envName: String, key: String): Any? =
        loadMerged(project)[envName]?.get(key)
}
