package dev.maheshbabu11.httpclientplus

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.wm.ToolWindowManager

class OpenHttpClientPlusAction : AnAction(), DumbAware {

    init {
        templatePresentation.text = "HTTP Client Plus"
        templatePresentation.description = "Show the HTTP Client Plus tool window"
        templatePresentation.icon =
            IconLoader.getIcon("/icons/toolWindow.svg", OpenHttpClientPlusAction::class.java)
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project ?: return
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("HTTP Client Plus") ?: return
        toolWindow.activate(null, true)
    }
}