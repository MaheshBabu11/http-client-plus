package dev.maheshbabu11.httpclientplus.ui

import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.JBColor
import com.intellij.util.ui.JBUI
import java.awt.Component
import java.awt.FlowLayout
import javax.swing.*

class SettingsSection {
    private val noRedirectCheck = JCheckBox("No redirect (# @no-redirect)")
    private val noCookieJarCheck = JCheckBox("No cookie jar (# @no-cookie-jar)")
    private val noAutoEncodingCheck = JCheckBox("No auto encoding (# @no-auto-encoding)")
    private val httpVersionBox = ComboBox(arrayOf("HTTP/1.1 (default)", "HTTP/2")).apply {
        selectedIndex = 0
        toolTipText = "Optional HTTP version token in request line"
    }

    val component: JComponent = buildPanel()

    private fun buildPanel(): JComponent {
        val panel = JPanel()
        panel.layout = BoxLayout(panel, BoxLayout.Y_AXIS)
        panel.border = JBUI.Borders.empty(12)

        val httpVersionPanel = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0)).apply {
            add(JLabel("HTTP version:").apply {
                foreground = JBColor.foreground()
            })
            add(httpVersionBox)
            add(JLabel("(HTTP/1.1 emits no token)").apply {
                foreground = JBColor.foreground()
            })
        }

        // Ensure left alignment within BoxLayout
        noRedirectCheck.alignmentX = Component.LEFT_ALIGNMENT
        noCookieJarCheck.alignmentX = Component.LEFT_ALIGNMENT
        noAutoEncodingCheck.alignmentX = Component.LEFT_ALIGNMENT
        httpVersionPanel.alignmentX = Component.LEFT_ALIGNMENT

        panel.add(noRedirectCheck)
        panel.add(Box.createVerticalStrut(6))
        panel.add(noCookieJarCheck)
        panel.add(Box.createVerticalStrut(6))
        panel.add(noAutoEncodingCheck)
        panel.add(Box.createVerticalStrut(10))
        panel.add(httpVersionPanel)

        return panel
    }

    fun isNoRedirect(): Boolean = noRedirectCheck.isSelected
    fun isNoCookieJar(): Boolean = noCookieJarCheck.isSelected
    fun isNoAutoEncoding(): Boolean = noAutoEncodingCheck.isSelected

    fun httpVersionToken(): String? = when (httpVersionBox.selectedItem as String) {
        "HTTP/2" -> "HTTP/2"
        else -> null
    }

    fun loadSettings(noRedirect: Boolean, noCookieJar: Boolean, noAutoEncoding: Boolean, httpVersion: String?) {
        noRedirectCheck.isSelected = noRedirect
        noCookieJarCheck.isSelected = noCookieJar
        noAutoEncodingCheck.isSelected = noAutoEncoding
        httpVersionBox.selectedItem = if (httpVersion.equals("HTTP/2", true)) "HTTP/2" else "HTTP/1.1 (default)"
    }

    fun clear() {
        noRedirectCheck.isSelected = false
        noCookieJarCheck.isSelected = false
        noAutoEncodingCheck.isSelected = false
        httpVersionBox.selectedIndex = 0
    }

}

