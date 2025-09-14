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
