// kotlin
package dev.maheshbabu11.httpclientplus.ui

import com.intellij.openapi.fileTypes.FileTypeManager
import com.intellij.openapi.project.Project
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Font
import java.awt.GridLayout
import javax.swing.ButtonGroup
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JToggleButton
import javax.swing.border.LineBorder
import javax.swing.UIManager

class ScriptHandlerSection(private val project: Project?) {

    private val jsFileType = FileTypeManager.getInstance().getFileTypeByExtension("js")

    private val preEditor = EditorTextField(null, project, jsFileType, false).apply {
        setOneLineMode(false)
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        toolTipText = "Enter pre-request script"
    }


    private val postEditor = EditorTextField(null, project, jsFileType, false).apply {
        setOneLineMode(false)
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        toolTipText = "Enter response handler script"
    }

    val component: JComponent

    init {
        // Header with two toggle buttons that share the header width equally
        val header = JPanel(GridLayout(1, 2, 0, 0))
        val preTab = JToggleButton("Pre-request script")
        val postTab = JToggleButton("Post-request script")
        preTab.isSelected = true
        ButtonGroup().apply {
            add(preTab)
            add(postTab)
        }
        // Optional padding / styling
        preTab.border = JBUI.Borders.empty(8, 12)
        postTab.border = JBUI.Borders.empty(8, 12)
        header.add(preTab)
        header.add(postTab)

        // Content area using CardLayout
        val cards = JPanel(CardLayout()).apply {
            add(wrapEditor(preEditor), "pre")
            add(wrapEditor(postEditor), "post")
        }

        // Toggle actions switch cards
        preTab.addActionListener { (cards.layout as CardLayout).show(cards, "pre") }
        postTab.addActionListener { (cards.layout as CardLayout).show(cards, "post") }

        component = JPanel(BorderLayout()).apply {
            add(header, BorderLayout.NORTH)
            add(cards, BorderLayout.CENTER)
        }
    }

    private fun wrapEditor(editor: EditorTextField): JComponent {
        val scrollPane = JBScrollPane(editor).apply {
            border = LineBorder(
                UIManager.getColor("Component.borderColor") ?: UIManager.getColor("Panel.background"),
                1,
                true
            )
            verticalScrollBarPolicy = JBScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JBScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
        }
        return JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(10)
            add(scrollPane, BorderLayout.CENTER)
        }
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
