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

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBPanel
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.GridLayout
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JRadioButton

class ImportTypeDialog : DialogWrapper(true) {
    private var selectedType: String = "Postman"

    init {
        title = "Import Requests"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JBPanel<JBPanel<*>>(BorderLayout())
        panel.preferredSize = Dimension(320, 140)
        panel.border = JBUI.Borders.empty(10)

        val optionsPanel = JBPanel<JBPanel<*>>(GridLayout(2, 1, 5, 10))

        // --- Postman option
        val postmanRadio = JRadioButton("Postman Collection (.json)", selectedType == "Postman")
        val postmanLabel = JBLabel("Import from exported Postman collections")
        postmanLabel.border = JBUI.Borders.emptyLeft(20)

        val postmanPanel = JBPanel<JBPanel<*>>(BorderLayout())
        postmanPanel.add(postmanRadio, BorderLayout.NORTH)
        postmanPanel.add(postmanLabel, BorderLayout.CENTER)

        // --- cURL option
        val curlRadio = JRadioButton("cURL Command (raw text)", selectedType == "cURL")
        val curlLabel = JBLabel("Paste raw cURL command for quick import")
        curlLabel.border = JBUI.Borders.emptyLeft(20)

        val curlPanel = JBPanel<JBPanel<*>>(BorderLayout())
        curlPanel.add(curlRadio, BorderLayout.NORTH)
        curlPanel.add(curlLabel, BorderLayout.CENTER)

        val group = ButtonGroup()
        group.add(postmanRadio)
        group.add(curlRadio)

        postmanRadio.addActionListener { selectedType = "Postman" }
        curlRadio.addActionListener { selectedType = "cURL" }

        optionsPanel.add(postmanPanel)
        optionsPanel.add(curlPanel)

        panel.add(optionsPanel, BorderLayout.CENTER)
        return panel
    }

    fun getSelectedType(): String = selectedType
}
