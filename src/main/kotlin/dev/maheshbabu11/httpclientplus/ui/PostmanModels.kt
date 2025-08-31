package dev.maheshbabu11.httpclientplus.ui


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
    val value: String? = null,
    val src: String? = null,
    val type: String? = null
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