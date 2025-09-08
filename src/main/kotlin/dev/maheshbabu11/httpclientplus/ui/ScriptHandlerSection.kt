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

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.Box
import javax.swing.BoxLayout
import javax.swing.border.LineBorder
import javax.swing.UIManager

class ScriptHandlerSection(private val project: Project?) {

    private val jsFileType = FileTypeManager.getInstance().getFileTypeByExtension("js")

    private val preEditor = EditorTextField(null, project, jsFileType, false).apply {
        setOneLineMode(false)
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        toolTipText = "Enter pre-request script. Example: < {% request.variables.set(\"clients\", [{id:1}]) %}"
    }

    private val postEditor = EditorTextField(null, project, jsFileType, false).apply {
        setOneLineMode(false)
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        toolTipText =
            "Enter response handler script. Example: > {% client.global.set(\"auth_token\", response.body.token); %}"
    }

    val component: JComponent = JPanel().apply {
        layout = BoxLayout(this, BoxLayout.Y_AXIS)
        border = JBUI.Borders.empty(10)

        add(buildSection("Pre-request script (optional)", preEditor))
        add(Box.createVerticalStrut(12))
        add(buildSection("Response handler script (optional)", postEditor))
    }

    private fun buildSection(label: String, editor: EditorTextField): JComponent {
        val panel = JPanel(BorderLayout())
        panel.border = JBUI.Borders.emptyBottom(10)

        panel.add(JLabel(label).apply {
            border = JBUI.Borders.emptyBottom(6)
        }, BorderLayout.NORTH)

        val scrollPane = JBScrollPane(editor).apply {
            border = LineBorder(
                UIManager.getColor("Component.borderColor") ?: UIManager.getColor("Panel.background"),
                1,
                true
            )
            verticalScrollBarPolicy = JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        }

        panel.add(scrollPane, BorderLayout.CENTER)
        return panel
    }

    fun getPreScript(): String? {
        val raw = preEditor.text.trim()
        if (raw.isEmpty()) return null
        return if (raw.startsWith("<")) raw else "< {% $raw %}"
    }

    fun setPreScript(script: String?) {
        preEditor.text = script?.removePrefix("< {%")?.removeSuffix("%}")?.trim() ?: ""
    }

    fun getPostScript(): String? {
        val raw = postEditor.text.trim()
        if (raw.isEmpty()) return null
        return if (raw.startsWith(">")) raw else "> {% $raw %}"
    }

    fun setPostScript(script: String?) {
        postEditor.text = script?.removePrefix("> {%")?.removeSuffix("%}")?.trim() ?: ""
    }

    fun clear() {
        preEditor.text = ""
        postEditor.text = ""
    }
}
