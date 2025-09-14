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

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextField
import com.intellij.util.ui.JBUI
import dev.maheshbabu11.httpclientplus.importer.CurlImporter
import dev.maheshbabu11.httpclientplus.importer.PostmanImporter
import dev.maheshbabu11.httpclientplus.service.HttpFileService
import dev.maheshbabu11.httpclientplus.service.HttpRequestData
import java.awt.*
import java.io.File
import javax.swing.*
import com.intellij.ui.JBColor

class ImportSection(private val project: Project) {
    val component: JComponent

    private val postmanPanel = JPanel()
    private val curlPanel = JPanel()
    private val cardLayout = CardLayout()
    private val contentPanel = JPanel(cardLayout)

    private val fileNameField = JBTextField().apply {
        toolTipText = "Enter a name for the imported request"
    }
    private val collectionCombo = ComboBox<String>().apply {
        isEditable = true
        prototypeDisplayValue = "Select or type collection name"
    }
    private val curlTextArea = JTextArea(8, 50).apply {
        font = Font(Font.MONOSPACED, Font.PLAIN, 12)
        lineWrap = true
        wrapStyleWord = true
        toolTipText = "Paste your cURL command here"
    }

    init {
        setupComponents()
        component = createMainPanel()
    }

    private fun setupComponents() {
        setupPostmanPanel()
        setupCurlPanel()
        loadCollections()
    }

    private fun createMainPanel(): JPanel {
        return JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(16)

            // Header panel with title and type selector
            val headerPanel = JPanel(BorderLayout()).apply {
                border = JBUI.Borders.emptyBottom(16)

                val titleLabel = JLabel("Import Requests").apply {
                    font = font.deriveFont(Font.BOLD, 16f)
                }
                add(titleLabel, BorderLayout.WEST)

                val typeSelector = createTypeSelector()
                add(typeSelector, BorderLayout.EAST)
            }

            add(headerPanel, BorderLayout.NORTH)
            add(contentPanel, BorderLayout.CENTER)
        }
    }

    private fun createTypeSelector(): JPanel {
        val panel = JPanel(FlowLayout(FlowLayout.RIGHT, 0, 0))

        val postmanRadio = JRadioButton("Postman Collection", true).apply {
            background = null
        }
        val curlRadio = JRadioButton("cURL Command").apply {
            background = null
        }

        ButtonGroup().apply {
            add(postmanRadio)
            add(curlRadio)
        }

        postmanRadio.addActionListener {
            cardLayout.show(contentPanel, "POSTMAN")
        }

        curlRadio.addActionListener {
            cardLayout.show(contentPanel, "CURL")
            loadCollections() // Refresh collections when switching to cURL
        }

        panel.add(postmanRadio)
        panel.add(Box.createHorizontalStrut(16))
        panel.add(curlRadio)

        return panel
    }

    private fun setupPostmanPanel() {
        postmanPanel.layout = BorderLayout()
        postmanPanel.border = JBUI.Borders.empty(20)

        val infoPanel = JPanel().apply {
            layout = BoxLayout(this, BoxLayout.Y_AXIS)

            val iconLabel = JLabel("📁").apply {
                font = font.deriveFont(24f)
                alignmentX = Component.CENTER_ALIGNMENT
            }

            val titleLabel = JLabel("Import Postman Collection").apply {
                font = font.deriveFont(Font.BOLD, 14f)
                alignmentX = Component.CENTER_ALIGNMENT
            }

            val descLabel = JLabel("Select a Postman collection JSON file to import").apply {
                foreground = JBColor.GRAY
                alignmentX = Component.CENTER_ALIGNMENT
            }

            add(iconLabel)
            add(Box.createVerticalStrut(8))
            add(titleLabel)
            add(Box.createVerticalStrut(4))
            add(descLabel)
        }

        val buttonPanel = JPanel(FlowLayout()).apply {
            val chooseButton = JButton("Choose Postman File").apply {
                preferredSize = Dimension(180, 32)
                addActionListener { importPostman() }
            }
            add(chooseButton)
        }

        postmanPanel.add(infoPanel, BorderLayout.CENTER)
        postmanPanel.add(buttonPanel, BorderLayout.SOUTH)

        contentPanel.add(postmanPanel, "POSTMAN")
    }

    private fun setupCurlPanel() {
        curlPanel.layout = BorderLayout()
        curlPanel.border = JBUI.Borders.empty(20)

        val formPanel = JPanel(GridBagLayout()).apply {
            val gbc = GridBagConstraints().apply {
                insets = JBUI.insets(8)
                anchor = GridBagConstraints.WEST
            }

            // Request name field
            gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE
            add(JLabel("Request Name:"), gbc)

            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL
            add(fileNameField, gbc)

            // Collection selector
            gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE
            add(JLabel("Collection:"), gbc)

            gbc.gridx = 1; gbc.weightx = 1.0; gbc.fill = GridBagConstraints.HORIZONTAL
            add(collectionCombo, gbc)

            // cURL command area
            gbc.gridx = 0; gbc.gridy = 2; gbc.weightx = 0.0; gbc.fill = GridBagConstraints.NONE; gbc.anchor = GridBagConstraints.NORTHWEST
            add(JLabel("cURL Command:"), gbc)

            gbc.gridx = 1; gbc.weightx = 1.0; gbc.weighty = 1.0; gbc.fill = GridBagConstraints.BOTH
            val scrollPane = JBScrollPane(curlTextArea).apply {
                preferredSize = Dimension(500, 150)
                border = BorderFactory.createLoweredBevelBorder()
            }
            add(scrollPane, gbc)
        }

        val buttonPanel = JPanel(FlowLayout(FlowLayout.RIGHT)).apply {
            val importButton = JButton("Import cURL").apply {
                preferredSize = Dimension(120, 32)
                addActionListener { handleCurlImport() }
            }
            add(importButton)
        }

        curlPanel.add(formPanel, BorderLayout.CENTER)
        curlPanel.add(buttonPanel, BorderLayout.SOUTH)

        contentPanel.add(curlPanel, "CURL")
    }

    private fun loadCollections() {
        val collectionsDir = File(project.basePath, "http-client-plus/collections")
        val collections = collectionsDir.listFiles()
            ?.filter { it.isDirectory }
            ?.map { it.name.replace("_", " ") } // Display names without underscores
            ?: emptyList()

        collectionCombo.removeAllItems()
        collections.forEach { collectionCombo.addItem(it) }
        collectionCombo.isEditable = true
        if (collections.isEmpty()) {
            collectionCombo.addItem("No collections found")
        }
    }

    private fun handleCurlImport() {
        val curlCommand = curlTextArea.text.trim()
        val fileName = fileNameField.text.trim()
        val selectedCollection = collectionCombo.selectedItem?.toString()?.trim()

        when {
            curlCommand.isBlank() -> {
                Messages.showWarningDialog(project, "Please enter a cURL command", "Missing cURL Command")
                return
            }
            fileName.isBlank() -> {
                Messages.showWarningDialog(project, "Please enter a request name", "Missing Request Name")
                return
            }
            selectedCollection.isNullOrBlank() || selectedCollection == "No collections found" -> {
                Messages.showWarningDialog(project, "Please select a valid collection", "No Collection Selected")
                return
            }
        }

        importCurl(curlCommand, fileName, selectedCollection)
    }

    private fun importPostman() {
        val descriptor = FileChooserDescriptor(true, false, false, false, false, false)
            .withFileFilter { it.extension == "json" }
            .withTitle("Select Postman Collection")
            .withDescription("Choose a Postman collection JSON file to import")

        val file = FileChooser.chooseFile(descriptor, project, null) ?: return

        try {
            val importer = PostmanImporter()
            val requests = importer.import(project, file)
            saveRequests(requests)
        } catch (e: Exception) {
            Messages.showErrorDialog(project, "Failed to import Postman collection: ${e.message}", "Import Error")
        }
    }

    private fun importCurl(curlCommand: String, fileName: String, collection: String) {
        try {
            val importer = CurlImporter()
            val importedRequests = importer.import(project, curlCommand).map { req ->
                req.copy(
                    name = fileName,
                    saveDirPath = "http-client-plus/collections/${collection.replace(" ", "_")}"
                )
            }
            saveRequests(importedRequests)

            // Clear form after successful import
            curlTextArea.text = ""
            fileNameField.text = ""
            loadCollections()
        } catch (e: Exception) {
            Messages.showErrorDialog(project, "Failed to import cURL command: ${e.message}", "Import Error")
        }
    }
    private fun saveRequests(requests: List<HttpRequestData>) {
        if (requests.isEmpty()) {
            Messages.showWarningDialog(project, "No requests found to import", "No Requests")
            return
        }

        var success = 0
        val errors = mutableListOf<String>()

        requests.forEach { req ->
            try {
                if (HttpFileService.createRequestFile(project, req) != null) {
                    success++
                }
            } catch (e: Exception) {
                errors.add("${req.name}: ${e.message}")
            }
        }

        when {
            success == requests.size -> {
                Messages.showInfoMessage(
                    project,
                    "Successfully imported $success request${if (success == 1) "" else "s"}",
                    "Import Successful"
                )
            }
            success > 0 -> {
                Messages.showWarningDialog(
                    project,
                    "Imported $success of ${requests.size} requests.\n\nErrors:\n${errors.joinToString("\n")}",
                    "Partial Import"
                )
            }
            else -> {
                Messages.showErrorDialog(
                    project,
                    "Failed to import requests:\n${errors.joinToString("\n")}",
                    "Import Failed"
                )
            }
        }
    }
}
