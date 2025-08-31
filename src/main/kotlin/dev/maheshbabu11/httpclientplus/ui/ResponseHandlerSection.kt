package dev.maheshbabu11.httpclientplus.ui

import com.intellij.ui.components.JBTextArea
import com.intellij.util.ui.JBUI
import com.intellij.util.ui.UIUtil
import java.awt.BorderLayout
import java.awt.Font
import javax.swing.*
import javax.swing.border.LineBorder

class ResponseHandlerSection {
    private val editor = JBTextArea(8, 60).apply {
        lineWrap = false
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        toolTipText =
            "Enter HTTP Client response handler script. Example: > {% client.global.set(\"auth_token\", response.body.token); %}"
        border = JBUI.Borders.empty() // remove default border
        background = UIUtil.getTextFieldBackground()
    }

    val component: JComponent = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.empty(10)

        add(JLabel("Response handler script (optional)").apply {
            border = JBUI.Borders.emptyBottom(6)
        }, BorderLayout.NORTH)

        val scrollPane = JScrollPane(editor).apply {
            border = LineBorder(
                UIManager.getColor("Component.borderColor") ?: UIManager.getColor("Panel.background"),
                1,
                true
            )
            verticalScrollBarPolicy = JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED
            horizontalScrollBarPolicy = JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED
            putClientProperty("JScrollPane.smoothScrolling", true)
        }

        add(scrollPane, BorderLayout.CENTER)
    }

    fun getScript(): String? {
        val raw = editor.text.trim()
        if (raw.isEmpty()) return null
        return if (raw.startsWith(">")) raw else "> $raw"
    }

    fun setScript(script: String?) {
        editor.text = script?.removePrefix(">")?.trim() ?: ""
    }

    fun clear() {
        editor.text = ""
    }


}
