package dev.maheshbabu11.httpclientplus

import dev.maheshbabu11.httpclientplus.ui.HttpClientPlusPanel
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import dev.maheshbabu11.httpclientplus.service.HttpClientPlusService

class HttpClientPlusToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val panel = HttpClientPlusPanel(project)
        val content = ContentFactory.getInstance().createContent(panel, "", false)
        toolWindow.contentManager.addContent(content)
        HttpClientPlusService.getInstance(project).registerPanel(panel)
    }
}
