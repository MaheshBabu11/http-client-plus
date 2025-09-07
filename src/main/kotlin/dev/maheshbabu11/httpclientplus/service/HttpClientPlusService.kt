package dev.maheshbabu11.httpclientplus.service

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager
import dev.maheshbabu11.httpclientplus.ui.HttpClientPlusPanel
import javax.swing.SwingUtilities

@Service(Service.Level.PROJECT)
class HttpClientPlusService(private val project: Project) {

    private var panel: HttpClientPlusPanel? = null

    fun registerPanel(panel: HttpClientPlusPanel) {
        this.panel = panel
    }

    fun openRequest(data: HttpRequestData) {
        val toolWindow = ToolWindowManager.getInstance(project).getToolWindow("HTTP Client Plus")
            ?: return

        SwingUtilities.invokeLater {
            toolWindow.show {
                panel?.let {
                    it.clearUI()
                    it.loadRequestData(data, null)
                }
            }
        }
    }

    companion object {
        fun getInstance(project: Project): HttpClientPlusService =
            project.getService(HttpClientPlusService::class.java)
    }
}
