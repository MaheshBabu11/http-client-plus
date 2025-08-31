package dev.maheshbabu11.httpclientplus.ui

import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.editor.colors.EditorColorsManager
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBLabel
import com.intellij.ui.components.panels.HorizontalLayout
import com.intellij.ui.scale.JBUIScale
import com.intellij.util.ui.JBUI
import com.intellij.openapi.ui.popup.JBPopupFactory
import dev.maheshbabu11.httpclientplus.http.MultipartPart
import java.awt.BorderLayout
import java.awt.CardLayout
import java.awt.Font
import javax.swing.*

class BodySection(project: Project) {

    companion object {
        private val CONTENT_TYPES = listOf(
            "application/json",
            "text/plain",
            "application/x-www-form-urlencoded",
            "multipart/form-data",
            "application/xml"
        )
    }

    private val bodyArea = JBTextArea(12, 40).apply {
        lineWrap = true
        wrapStyleWord = true
        toolTipText = "Request body content"
        val scheme = EditorColorsManager.getInstance().globalScheme
        font = Font(scheme.editorFontName, Font.PLAIN, scheme.editorFontSize)
    }

    private val bodyScrollPane = JBScrollPane(bodyArea)

    private val multipartSection = MultipartSection(project)

    private val contentTypeBox = ComboBox(CONTENT_TYPES.toTypedArray()).apply {
        toolTipText = "Content-Type header to include"
        prototypeDisplayValue = "application/x-www-form-urlencoded"
        selectedItem = "application/json"
        addItemListener { _ -> toggleBodyMode() }
    }

    private val bodyOverflowButton = JButton(AllIcons.Actions.More).apply {
        toolTipText = "More actions"
        putClientProperty("JButton.buttonType", "toolbar")
        isContentAreaFilled = false
        isBorderPainted = false
        margin = JBUI.emptyInsets()
        preferredSize = JBUI.size(28, 28)
        addActionListener {
            showOverflowMenu()
        }
    }

    private val bodyCardLayout = CardLayout()
    private val bodyContentPanel = JPanel(bodyCardLayout).apply {
        add(bodyScrollPane, "text")
        add(multipartSection.component, "multipart")
    }

    val component: JComponent = buildBodyTab()

    private fun buildBodyTab(): JComponent {
        val bodyPanel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(10)
        }

        val bodyHeader = JPanel(BorderLayout(8, 0))

        val ctContainer = JPanel(HorizontalLayout(8)).apply {
            add(JBLabel("Content-Type:").apply {
                font = font.deriveFont(12f)
                foreground = JBColor.foreground()
                labelFor = contentTypeBox
            })
            add(contentTypeBox)
            add(bodyOverflowButton)
        }

        bodyHeader.add(ctContainer, BorderLayout.EAST)
        bodyPanel.add(bodyHeader, BorderLayout.NORTH)
        bodyPanel.add(bodyContentPanel, BorderLayout.CENTER)

        toggleBodyMode()
        installContextMenu()
        return bodyPanel
    }

    private fun toggleBodyMode() {
        val isMultipart = isMultipartSelected()
        bodyCardLayout.show(bodyContentPanel, if (isMultipart) "multipart" else "text")
        bodyArea.isEnabled = !isMultipart
        bodyArea.isEditable = !isMultipart
    }

    private fun showOverflowMenu() {
        val actions = mapOf(
            "Beautify JSON" to { beautifyJsonBody() },
            "Minify JSON" to { minifyJsonBody() }
        )

        JBPopupFactory.getInstance()
            .createPopupChooserBuilder(actions.keys.toList())
            .setTitle("Actions")
            .setItemChosenCallback { label ->
                actions[label]?.invoke()
            }
            .createPopup()
            .showUnderneathOf(bodyOverflowButton)
    }


    private fun beautifyJsonBody() {
        val raw = bodyArea.text
        if (raw.isNullOrBlank()) return
        try {
            val pretty = JsonUtils.prettyPrintJson(raw)
            bodyArea.text = pretty
        } catch (_: Exception) {
            Messages.showInfoMessage(component, "Invalid JSON - cannot beautify", "Beautify JSON")
        }
    }

    private fun minifyJsonBody() {
        val raw = bodyArea.text
        if (raw.isNullOrBlank()) return
        try {
            val compact = JsonUtils.minifyJson(raw)
            bodyArea.text = compact
        } catch (_: Exception) {
            Messages.showInfoMessage(component, "Invalid JSON - cannot minify", "Minify JSON")
        }
    }

    fun loadText(body: String?, headers: List<Pair<String, String>>) {
        bodyArea.text = body ?: ""
        val ct = headers.firstOrNull { it.first.equals("Content-Type", true) }?.second
        contentTypeBox.selectedItem = ct ?: "application/json"
        toggleBodyMode()
    }

    fun loadMultipart(boundary: String?, parts: List<MultipartPart>) {
        contentTypeBox.selectedItem = "multipart/form-data"
        toggleBodyMode()
        multipartSection.setParts(boundary, parts)
    }

    fun clear() {
        bodyArea.text = ""
        contentTypeBox.selectedItem = "application/json"
        multipartSection.clear()
        toggleBodyMode()
    }

    private fun installContextMenu() {
        val popup = JPopupMenu()

        val beautifyItem = JMenuItem("Beautify JSON")
        beautifyItem.addActionListener { beautifyJsonBody() }
        popup.add(beautifyItem)

        val minifyItem = JMenuItem("Minify JSON")
        minifyItem.addActionListener { minifyJsonBody() }
        popup.add(minifyItem)

        bodyArea.componentPopupMenu = popup
    }


    fun selectedContentType(): String = (contentTypeBox.selectedItem as? String)?.trim().orEmpty()
    fun isMultipartSelected(): Boolean = selectedContentType().equals("multipart/form-data", true)
    fun getBodyText(): String? = bodyArea.text.takeIf { it.isNotBlank() }
    fun getBoundary(): String? = multipartSection.getBoundary()
    fun getParts(): List<MultipartPart> = multipartSection.getParts()
}
