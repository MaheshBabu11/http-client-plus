package dev.maheshbabu11.httpclientplus.ui

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import java.awt.BorderLayout
import java.awt.GridLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.JTextField

class CurlImportDialog(
    project: Project,
    existingCollections: List<String>
) : DialogWrapper(project) {

    private val curlTextArea = JTextArea(10, 50)
    private val fileNameField = JTextField()
    private val collectionDisplayMap = existingCollections.associateBy { it.replace("_", " ") }
    private val collectionCombo = ComboBox(collectionDisplayMap.keys.toTypedArray()).apply {
        isEditable = true
        if (itemCount > 0) selectedIndex = 0
        prototypeDisplayValue = "Select or type collection name"
    }


    var curlCommand: String? = null
        private set
    var resultFileName: String? = null
        private set
    var resultCollection: String? = null
        private set

    init {
        title = "Import cURL Request"
        init()
    }

    override fun createCenterPanel(): JComponent {
        val panel = JPanel(BorderLayout(10, 10))

        val form = JPanel(GridLayout(3, 2, 5, 5))
        form.add(JLabel("Request Name:"))
        form.add(fileNameField)
        form.add(JLabel("Collection:"))
        form.add(collectionCombo)

        panel.add(form, BorderLayout.NORTH)
        panel.add(JBScrollPane(curlTextArea), BorderLayout.CENTER)

        return panel
    }

    override fun doOKAction() {
        curlCommand = curlTextArea.text.trim()
        resultFileName = fileNameField.text.trim()
        val selectedDisplay = collectionCombo.selectedItem?.toString()?.trim()
        resultCollection = collectionDisplayMap[selectedDisplay]
        if (resultCollection == null && !selectedDisplay.isNullOrEmpty()) {
            resultCollection = selectedDisplay.replace(" ", "_")
        }
        super.doOKAction()
    }
}

