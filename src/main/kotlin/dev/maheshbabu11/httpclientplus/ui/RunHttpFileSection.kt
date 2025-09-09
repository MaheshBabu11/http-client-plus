package dev.maheshbabu11.httpclientplus.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel

/**
 * Shows the saved .http file content and provides a gutter icon to run it.
 * Uses a full editor instance to ensure gutter icons render correctly.
 */
class RunHttpFileSection(private val project: Project) {

    private var currentFile: VirtualFile? = null

    private val infoLabel = JLabel("Save the request to preview and run it here.")

    private val editorPanel = JPanel(BorderLayout())

    val component: JComponent = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.empty(10)
        add(infoLabel, BorderLayout.NORTH)
        add(editorPanel, BorderLayout.CENTER)
    }

    private var editor: Editor? = null

    /**
     * Load and display a .http file using a full editor.
     */
    fun showFile(vFile: VirtualFile) {
        currentFile = vFile

        // Step 1: read document safely
        val docText = ApplicationManager.getApplication().runReadAction<String?> {
            val doc = FileDocumentManager.getInstance().getDocument(vFile)
            doc?.text
        }

        if (docText == null) {
            infoLabel.text = "Unable to load file: ${vFile.name}"
            return
        }

        // Step 2: create/open full editor and attach gutter icon
        ApplicationManager.getApplication().invokeLater {
            val fem = FileEditorManager.getInstance(project)
            val openEditor = fem.openTextEditor(OpenFileDescriptor(project, vFile), false)
            if (openEditor != null) {
                editor = openEditor
                editorPanel.removeAll()
                editorPanel.add(JBScrollPane(openEditor.component), BorderLayout.CENTER)
                editorPanel.revalidate()
                editorPanel.repaint()
            }
        }
    }

    /**
     * Clear the editor panel.
     */
    fun clear() {
        currentFile = null
        editor?.markupModel?.removeAllHighlighters()
        editorPanel.removeAll()
        infoLabel.text = "Save the request to preview and run it here."
        editorPanel.revalidate()
        editorPanel.repaint()
    }
}
