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

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * Shows the saved .http file content inside the Run tab panel.
 * Uses a full editor instance without opening a tab in the main editor.
 */
class RunHttpFileSection(private val project: Project) {

    private var currentFile: VirtualFile? = null
    private var editor: Editor? = null

    private val infoLabel = JLabel("Save the request to preview and run it here.")

    private val editorPanel = JPanel(BorderLayout())

    val component: JComponent = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.empty(10)
        add(infoLabel, BorderLayout.NORTH)
        add(editorPanel, BorderLayout.CENTER)
    }

    /**
     * Load and display a .http file in the embedded editor.
     * Does NOT open a tab in the main editor.
     */
    fun showFile(vFile: VirtualFile) {
        currentFile = vFile

        // Step 1: read document safely (also forces Document creation for this file)
        val docText = ApplicationManager.getApplication().runReadAction<String?> {
            FileDocumentManager.getInstance().getDocument(vFile)?.text
        }

        if (docText == null) {
            infoLabel.text = "Unable to load file: ${vFile.name}"
            infoLabel.isVisible = true
            return
        }

        // Step 2: create editor component without opening main tab
        ApplicationManager.getApplication().invokeLater {
            // Dispose previous editor if exists
            editor?.let { EditorFactory.getInstance().releaseEditor(it) }
            val document = FileDocumentManager.getInstance().getDocument(vFile) ?: return@invokeLater
            editor = EditorFactory.getInstance().createEditor(document, project, vFile.fileType, false)

            editorPanel.removeAll()
            // Editor is already scrollable; embed directly
            editorPanel.add(editor!!.component, BorderLayout.CENTER)
            infoLabel.isVisible = false
            editorPanel.revalidate()
            editorPanel.repaint()
        }
    }

    /**
     * Clear the editor panel and release resources.
     */
    fun clear() {
        currentFile = null
        editor?.markupModel?.removeAllHighlighters()
        editor?.let { EditorFactory.getInstance().releaseEditor(it) }
        editor = null

        editorPanel.removeAll()
        infoLabel.text = "Save the request to preview and run it here."
        infoLabel.isVisible = true
        editorPanel.revalidate()
        editorPanel.repaint()
    }
}
