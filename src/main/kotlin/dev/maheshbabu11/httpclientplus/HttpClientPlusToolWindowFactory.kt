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

package dev.maheshbabu11.httpclientplus

import dev.maheshbabu11.httpclientplus.ui.HttpClientPlusPanel
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory
import dev.maheshbabu11.httpclientplus.service.HttpClientPlusService
import dev.maheshbabu11.httpclientplus.ui.EnvEditorSection
import dev.maheshbabu11.httpclientplus.ui.SavedRequestsSection
import javax.swing.SwingUtilities

class HttpClientPlusToolWindowFactory : ToolWindowFactory, DumbAware {
    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val contentFactory = ContentFactory.getInstance()
        val requestPanel = HttpClientPlusPanel(project)
        val requestContent = contentFactory.createContent(requestPanel, "Request", false)
        toolWindow.contentManager.addContent(requestContent)
        val savedSection = SavedRequestsSection(
            project,
            onRequestSelected = { data, vFile ->
                SwingUtilities.invokeLater {
                    requestPanel.clearUI()
                    requestPanel.loadRequestData(data, vFile)
                    toolWindow.contentManager.setSelectedContent(requestContent, true)
                }
            },
            onShowResponses = { collection, requestName ->
                SwingUtilities.invokeLater {
                    requestPanel.showResponsesForRequest(collection, requestName)
                    toolWindow.contentManager.setSelectedContent(requestContent, true)
                }
            }
        )
        val savedContent = contentFactory.createContent(savedSection.component, "Saved Requests", false)
        toolWindow.contentManager.addContent(savedContent)
        val envSection = EnvEditorSection(project)
        val envContent = contentFactory.createContent(envSection.component, "Environments", false)
        toolWindow.contentManager.addContent(envContent)
        toolWindow.contentManager.setSelectedContent(requestContent, true)
        HttpClientPlusService.getInstance(project).registerPanel(requestPanel)
    }
}
