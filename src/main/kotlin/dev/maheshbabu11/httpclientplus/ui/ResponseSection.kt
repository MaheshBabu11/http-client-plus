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

import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.GridBagConstraints
import java.awt.GridBagLayout
import javax.swing.JCheckBox
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane
import javax.swing.JTable

class ResponseSection {
    private val nameField = JBTextField().apply {
        emptyText.text = "Optional (default - response)"
        columns = 24
    }
    private val forceCheck = JCheckBox("Force overwrite").apply {
        toolTipText = "Use >>! to overwrite existing file"
    }
    private val hint = JBLabel("Will be saved under: filename/[name]-{{timestamp}}.json")
    private val responsesPanel = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.emptyTop(10)
    }


    val component: JComponent = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.empty(10)
        val form = JPanel(GridBagLayout())

        val commonInsets = JBUI.insetsBottom(8)

        form.add(JBLabel("File Name :"), GridBagConstraints().apply {
            gridx = 0
            gridy = 0
            anchor = GridBagConstraints.WEST
            insets = commonInsets
        })

        // Inner row panel to keep name and checkbox on the same line
        val rowPanel = JPanel(GridBagLayout()).apply {
            // name field grows
            add(nameField, GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                weightx = 1.0
                fill = GridBagConstraints.HORIZONTAL
            })
            // checkbox stays visible on the right
            add(forceCheck, GridBagConstraints().apply {
                gridx = 1
                gridy = 0
                anchor = GridBagConstraints.WEST
                insets = JBUI.insetsLeft(8)
            })
        }

        // Place the row panel in column 1, let it stretch
        form.add(rowPanel, GridBagConstraints().apply {
            gridx = 1
            gridy = 0
            weightx = 1.0
            fill = GridBagConstraints.HORIZONTAL
            insets = commonInsets
        })

        // Hint spans both columns
        form.add(hint, GridBagConstraints().apply {
            gridx = 0
            gridy = 1
            gridwidth = 2
            anchor = GridBagConstraints.WEST
        })

        add(form, BorderLayout.NORTH)
        add(responsesPanel, BorderLayout.CENTER)
    }

    fun showResponsesTable(table: JTable) {
        responsesPanel.removeAll()
        responsesPanel.add(JScrollPane(table), BorderLayout.CENTER)
        responsesPanel.revalidate()
        responsesPanel.repaint()
    }

    fun getCustomName(): String? = nameField.text.trim().ifBlank { null }

    fun isForceOverwrite(): Boolean = forceCheck.isSelected

    fun clear() {
        nameField.text = ""
        forceCheck.isSelected = false
        responsesPanel.removeAll()
    }

    fun setPath(path: String?, force: Boolean = false) {
        nameField.text = path ?: ""
        forceCheck.isSelected = force
    }


    // Build the relative path using the http file base name and optional custom name
    fun buildPath(httpFileBaseName: String, customName: String?, forceOverwrite: Boolean, finalUrl: String): String {
        val safeBase = httpFileBaseName.trim().ifBlank { "request" }
        val custom = customName?.trim().orEmpty()
        val ts = if (forceOverwrite) "" else "-{{\$timestamp}}"
        val rawExt = finalUrl.substringAfterLast('.', "json")
        val extension = rawExt.ifBlank { "json" }
        val fileName = if (custom.isEmpty()) "response$ts.$extension" else "$custom$ts.$extension"
        return "$safeBase/$fileName"

    }

}
