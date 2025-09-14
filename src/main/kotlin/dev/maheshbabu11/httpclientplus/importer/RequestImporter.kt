package dev.maheshbabu11.httpclientplus.importer

import com.intellij.openapi.project.Project
import dev.maheshbabu11.httpclientplus.service.HttpRequestData

interface RequestImporter {
    val name: String
    fun import(project: Project, input: Any): List<HttpRequestData>
}
