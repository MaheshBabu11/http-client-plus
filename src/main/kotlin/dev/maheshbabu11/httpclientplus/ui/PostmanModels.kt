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

import com.google.gson.JsonElement
import com.google.gson.annotations.SerializedName


data class PostmanCollection(
    val info: PostmanInfo,
    val item: List<PostmanItem>
)

data class PostmanInfo(
    val name: String,
    val schema: String
)

data class PostmanItem(
    val name: String,
    val request: PostmanRequest,
    val item: List<PostmanItem>? = null
)

data class PostmanRequest(
    val method: String,
    val url: PostmanUrl,
    val body: PostmanBody? = null,
    val header: List<PostmanHeader>? = null,
    val auth: PostmanAuth? = null
)

data class PostmanUrl(
    val raw: String,
    val protocol: String?,
    val host: List<String>?,
    val path: List<String>?,
    val query: List<PostmanQuery>? = null
)

data class PostmanQuery(
    val key: String,
    val value: String
)

data class PostmanBody(
    val mode: String?,
    val raw: String? = null,
    val formdata: List<PostmanFormData>? = null,
    val urlencoded: List<PostmanUrlEncoded>? = null
)

data class PostmanFormData(
    val key: String,
    val value: String?,
    @SerializedName("src") val src: JsonElement?,
    val type: String?,
    val contentType: String?
)

data class PostmanUrlEncoded(
    val key: String,
    val value: String
)

data class PostmanHeader(
    val key: String,
    val value: String
)

data class PostmanAuth(
    val type: String,
    val bearer: List<PostmanAuthAttribute>? = null,
    val basic: List<PostmanAuthAttribute>? = null
)

data class PostmanAuthAttribute(
    val key: String,
    val value: String,
    val type: String
)
