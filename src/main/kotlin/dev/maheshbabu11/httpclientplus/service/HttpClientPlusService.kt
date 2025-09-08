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
