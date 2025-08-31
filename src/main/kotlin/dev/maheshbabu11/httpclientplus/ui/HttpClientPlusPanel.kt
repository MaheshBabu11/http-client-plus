package dev.maheshbabu11.httpclientplus.ui

import com.intellij.icons.AllIcons
import dev.maheshbabu11.httpclientplus.http.HttpFileService
import dev.maheshbabu11.httpclientplus.http.HttpRequestData
import dev.maheshbabu11.httpclientplus.http.MultipartPart
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.components.JBTabbedPane
import com.intellij.util.ui.JBUI
import com.intellij.ui.JBColor
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Font
import java.awt.FlowLayout
import javax.swing.*
import com.intellij.openapi.vfs.VirtualFile
import java.awt.GridBagConstraints
import java.awt.GridBagLayout

class HttpClientPlusPanel(private val project: Project) : JPanel(BorderLayout()) {
    private val methodBox = ComboBox(arrayOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS")).apply {
        preferredSize = Dimension(100, 40)
        font = font.deriveFont(14f)
    }
    private val urlField = JBTextField().apply {
        toolTipText = "Enter request URL"
        font = font.deriveFont(14f)
        preferredSize = Dimension(preferredSize.width, 40)
        emptyText.text = "Enter request URL"
        accessibleContext.accessibleName = "Request URL"
    }

    // Sections
    private val paramsSection = ParamsSection()
    private val headersSection = HeadersSection()
    private val authorizationSection = AuthorizationSection()
    private val bodySection = BodySection(project)
    private val settingsSection = SettingsSection()
    private val envEditorSection = EnvEditorSection(project) { /* no-op: env selector removed */ }
    private val responseHandlerSection = ResponseHandlerSection()
    private val savedRequestsSection = SavedRequestsSection(project) { data, vFile ->
        loadRequestData(data, vFile)
    }

    private val nameField = JBTextField().apply {
        toolTipText = "Request name (used as file name)"
        font = font.deriveFont(14f)
        preferredSize = Dimension(preferredSize.width, 40)
        emptyText.text = "Request name (required to save)"
        accessibleContext.accessibleName = "Request Name"
    }
    private val collectionBox = ComboBox<String>().apply {
        toolTipText = "Choose or create a collection"
        preferredSize = Dimension(200, 40)
        font = font.deriveFont(14f)
    }

    private val newCollectionButton = JButton("Add New").apply {
        preferredSize = Dimension(100, 40)
        font = font.deriveFont(Font.BOLD, 14f)
        isFocusPainted = false
        toolTipText = "Create a new collection"
    }

    private val saveButton = JButton("Save").apply {
        preferredSize = Dimension(90, 40)
        font = font.deriveFont(Font.BOLD, 14f)
        isFocusPainted = false
        toolTipText = "Save request"
    }

    private val addButton = JButton("Add New").apply {
        preferredSize = Dimension(90, 40)
        font = font.deriveFont(Font.BOLD, 14f)
        isFocusPainted = false
        toolTipText = "Add new request"
    }

    private lateinit var tabbedPane: JBTabbedPane

    // Track current file
    private var currentRequestFile: VirtualFile? = null

    init {
        border = JBUI.Borders.empty()
        add(buildMainPanel(), BorderLayout.CENTER)
        saveButton.addActionListener { saveRequest() }
        addButton.addActionListener { clearUI() }
        newCollectionButton.addActionListener { createNewCollection() }
        loadCollections()
    }

    // Removed refreshEnvSelector and updateEnvHostToggle

    private fun buildMainPanel(): JComponent {
        val mainPanel = JPanel(BorderLayout())
        mainPanel.add(buildRequestBar(), BorderLayout.NORTH)
        mainPanel.add(buildTabsSection(), BorderLayout.CENTER)
        return mainPanel
    }

    private fun buildRequestBar(): JComponent {
        val requestPanel = JPanel(BorderLayout())
        requestPanel.border = JBUI.Borders.empty(15, 15, 10, 15)

        val urlPanel = JPanel(BorderLayout(8, 0))
        urlPanel.add(methodBox, BorderLayout.WEST)
        urlPanel.add(urlField, BorderLayout.CENTER)

        val actionsPanel = JPanel(FlowLayout(FlowLayout.RIGHT, 8, 0))
        actionsPanel.add(saveButton)
        actionsPanel.add(addButton)
        urlPanel.add(actionsPanel, BorderLayout.EAST)

        val nameLabel = JLabel("Name:").apply {
            font = font.deriveFont(Font.BOLD, 12f)
            foreground = JBColor.foreground()
            labelFor = nameField
        }
        val nameContainer = JPanel(BorderLayout(0, 4)).apply {
            add(nameLabel, BorderLayout.NORTH)
            add(nameField, BorderLayout.CENTER)
        }
        val collectionLabel = JLabel("Collection:").apply {
            font = font.deriveFont(Font.BOLD, 12f)
            foreground = JBColor.foreground()
            labelFor = collectionBox
        }
        val collectionContainer = JPanel(BorderLayout(8, 0)).apply {
            add(collectionLabel, BorderLayout.WEST)
            add(collectionBox, BorderLayout.CENTER)
            add(newCollectionButton, BorderLayout.EAST)
        }


        val fieldsPanel = JPanel(BorderLayout(0, 8)).apply {
            add(collectionContainer, BorderLayout.NORTH)
            add(nameContainer, BorderLayout.CENTER)
            add(urlPanel, BorderLayout.SOUTH)
        }


        requestPanel.add(fieldsPanel, BorderLayout.CENTER)
        return requestPanel
    }

    private fun buildTabsSection(): JComponent {
        tabbedPane = JBTabbedPane()
        tabbedPane.border = JBUI.Borders.empty(0, 15, 15, 15)
        tabbedPane.addTab(
            "Saved Requests",
            AllIcons.Nodes.Folder,
            savedRequestsSection.component,
            "Saved HTTP requests"
        )
        tabbedPane.addTab("Params", AllIcons.Actions.Properties, paramsSection.component, "Query parameters")
        tabbedPane.addTab("Headers", AllIcons.Actions.Properties, headersSection.component, "HTTP headers")
        tabbedPane.addTab("Authorization", AllIcons.Diff.Lock, authorizationSection.component, "Authorization settings")
        tabbedPane.addTab("Body", AllIcons.FileTypes.Json, bodySection.component, "Request body")
        tabbedPane.addTab(
            "Scripts",
            AllIcons.Actions.Execute,
            responseHandlerSection.component,
            "Response handler scripts"
        )
        tabbedPane.addTab("Settings", AllIcons.General.GearPlain, settingsSection.component, "Request options")
        tabbedPane.addTab(
            "Environments",
            AllIcons.Debugger.VariablesTab,
            envEditorSection.component,
            "Environment variables"
        )

        return tabbedPane
    }


    private fun saveRequest() {
        val data = buildRequestData() ?: return
        val fem = FileEditorManager.getInstance(project)

        val namedFile: VirtualFile? = data.name?.takeIf { it.isNotBlank() }?.let {
            HttpFileService.findRequestFileByName(project, it, data.saveDirPath)
        }

        if (namedFile != null && namedFile.isValid) {
            val updated = HttpFileService.updateRequestFile(project, namedFile, data)
            if (updated != null) {
                currentRequestFile = updated
                val isOpen = fem.getEditors(updated).isNotEmpty()
                if (isOpen) {
                    val editor = fem.selectedTextEditor
                    val caretOffset = editor?.caretModel?.offset
                    val scrollOffset = editor?.scrollingModel?.verticalScrollOffset
                    fem.closeFile(updated)
                    fem.openFile(updated, true)
                    val newEditor = fem.selectedTextEditor
                    if (caretOffset != null) newEditor?.caretModel?.moveToOffset(caretOffset)
                    if (scrollOffset != null) newEditor?.scrollingModel?.scrollVertically(scrollOffset)
                } else {
                    fem.openFile(updated, true)
                }
                return
            }
        }

        if (data.name != null) {
            val vFile = HttpFileService.createRequestFile(project, data)
            if (vFile == null) {
                JOptionPane.showMessageDialog(
                    this,
                    "Failed to create request file",
                    "HTTP Client Plus",
                    JOptionPane.ERROR_MESSAGE
                )
            } else {
                currentRequestFile = vFile
                fem.openFile(vFile, true)
            }
            return
        }

        val existing = currentRequestFile
        if (existing != null && existing.isValid) {
            val updated = HttpFileService.updateRequestFile(project, existing, data)
            if (updated != null) {
                currentRequestFile = updated
                val isOpen = fem.getEditors(updated).isNotEmpty()
                if (isOpen) {
                    FileDocumentManager.getInstance().reloadFiles(updated)
                } else {
                    fem.openFile(updated, true)
                }
                return
            }
        }

        val vFile = HttpFileService.createRequestFile(project, data)
        if (vFile == null) {
            JOptionPane.showMessageDialog(
                this,
                "Failed to create request file",
                "HTTP Client Plus",
                JOptionPane.ERROR_MESSAGE
            )
        } else {
            currentRequestFile = vFile
            fem.openFile(vFile, true)
        }
    }


    private fun buildRequestData(): HttpRequestData? {
        val method = (methodBox.selectedItem as? String)?.trim().orEmpty()
        val url = urlField.text.trim()
        if (url.isEmpty()) {
            JOptionPane.showMessageDialog(this, "URL cannot be empty", "Validation Error", JOptionPane.WARNING_MESSAGE)
            return null
        }

        var finalUrl = UrlUtils.buildUrlWithParams(
            baseUrl = url,
            params = paramsSection.getParams(),
            autoEncode = !settingsSection.isNoAutoEncoding()
        )

        val headers = headersSection.getHeaders().toMutableList()
        // Merge Authorization header from Authorization tab
        authorizationSection.getAuthorizationHeader()?.let { authHeader ->
            val idx = headers.indexOfFirst { it.first.equals(authHeader.first, ignoreCase = true) }
            if (idx >= 0) headers[idx] = authHeader else headers += authHeader
        }

        val selectedCt = bodySection.selectedContentType()
        val isMultipart = bodySection.isMultipartSelected()

        val name = nameField.text.trim().ifBlank { null }
        if (name == null) {
            JOptionPane.showMessageDialog(
                this,
                "Name is required to save the request",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            )
            return null
        }

        val methodUpper = method.uppercase()
        val bodyPresent = !bodySection.getBodyText().isNullOrBlank()
        val shouldIncludeCt =
            isMultipart || bodyPresent || methodUpper == "POST" || methodUpper == "PUT" || methodUpper == "PATCH"
        if (shouldIncludeCt && selectedCt.isNotEmpty() && !selectedCt.equals("Auto", true)) {
            val idx = headers.indexOfFirst { it.first.equals("Content-Type", true) }
            if (idx >= 0) headers[idx] = ("Content-Type" to selectedCt) else headers += ("Content-Type" to selectedCt)
        }

        val body = if (!isMultipart) bodySection.getBodyText() else null
        val (boundary, parts) = if (isMultipart) bodySection.getBoundary() to bodySection.getParts() else null to emptyList<MultipartPart>()

        val collection = (collectionBox.selectedItem as? String)
            ?.takeIf { it.isNotBlank() }
            ?.replace(" ", "_")

        if (collection == null) {
            JOptionPane.showMessageDialog(
                this,
                "Please select a collection",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE
            )
            return null
        }
        val saveDirPath = "http-client-plus/collections/$collection"

        val httpVersionToken = settingsSection.httpVersionToken()
        val responseScript = responseHandlerSection.getScript()

        return HttpRequestData(
            method = method,
            url = finalUrl,
            headers = headers,
            body = body,
            name = name,
            runImmediately = false,
            multipartBoundary = if (isMultipart) boundary else null,
            multipartParts = if (isMultipart) parts else emptyList(),
            saveDirPath = saveDirPath,
            noRedirect = settingsSection.isNoRedirect(),
            noCookieJar = settingsSection.isNoCookieJar(),
            noAutoEncoding = settingsSection.isNoAutoEncoding(),
            httpVersion = httpVersionToken,
            responseHandlerScript = responseScript
        )
    }

    fun loadRequestData(data: HttpRequestData, vFile: VirtualFile?) {
        // clear everything before loading
        paramsSection.clear()
        headersSection.clear()
        authorizationSection.clear()
        bodySection.clear()
        settingsSection.clear()
        responseHandlerSection.clear()
        currentRequestFile = vFile

        methodBox.selectedItem = data.method
        urlField.text = data.url.substringBefore("?")
        nameField.text = data.name ?: ""
        // If request belongs to a collection, select it
        data.saveDirPath?.let { dir ->
            if (dir.startsWith("http-client-plus/collections/")) {
                val col = dir.removePrefix("http-client-plus/collections/").substringBefore("/").replace("_", " ")
                if ((0 until collectionBox.itemCount).none { collectionBox.getItemAt(it) == col }) {
                    collectionBox.addItem(col)
                }
                collectionBox.selectedItem = col
            }

        }


        // Params
        paramsSection.setParams(UrlUtils.extractParams(data.url))

        // Headers
        headersSection.setHeaders(data.headers)

        // Auth (requires enhancement in AuthorizationSection to set values)
        authorizationSection.setFromHeader(data.headers)

        // Body
        if (data.multipartParts.isNotEmpty()) {
            bodySection.loadMultipart(data.multipartBoundary, data.multipartParts)
        } else {
            bodySection.loadText(data.body, data.headers)
        }

        // Settings
        settingsSection.loadSettings(data.noRedirect, data.noCookieJar, data.noAutoEncoding, data.httpVersion)

        // Response script
        responseHandlerSection.setScript(data.responseHandlerScript)
    }

    fun clearUI() {
        paramsSection.clear()
        headersSection.clear()
        authorizationSection.clear()
        bodySection.clear()
        settingsSection.clear()
        responseHandlerSection.clear()
        this.nameField.text = null
        this.urlField.text = null
        this.methodBox.selectedItem = "GET"
        this.collectionBox.selectedItem = null
        this.revalidate()
        this.repaint()
    }

    private fun createNewCollection() {
        val collectionNameField = JBTextField().apply {
            emptyText.text = "Enter collection name"
            columns = 20
        }

        // Panel with label + text field
        val formPanel = JPanel(GridBagLayout()).apply {
            border = JBUI.Borders.empty(10)

            val gbc = GridBagConstraints().apply {
                gridx = 0
                gridy = 0
                anchor = GridBagConstraints.WEST
            }
            add(JLabel("Name:"), gbc)

            gbc.gridx = 1
            gbc.weightx = 1.0
            gbc.fill = GridBagConstraints.HORIZONTAL
            add(collectionNameField, gbc)
        }

        val options = arrayOf("Create", "Cancel")
        val result = JOptionPane.showOptionDialog(
            parent,
            formPanel,
            "Create New Collection",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.PLAIN_MESSAGE,
            null,
            options,
            options[0]
        )

        if (result == JOptionPane.OK_OPTION) {
            val newName = collectionNameField.text.trim()
            val dirName = newName.replace(' ', '_')
            val collectionsDir = project.basePath?.let { "$it/http-client-plus/collections" } ?: return
            val newDir = java.io.File(collectionsDir, dirName)
            if (!newDir.exists()) {
                newDir.mkdirs()
            }
            if (newName.isNotBlank()) {
                // add to combo box
                collectionBox.addItem(newName)
                collectionBox.selectedItem = newName
            } else {
                JOptionPane.showMessageDialog(
                    parent,
                    "Collection name cannot be empty",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE
                )
            }
        }
    }

    private fun loadCollections() {
        val collectionsDir = project.basePath?.let { "$it/http-client-plus/collections" } ?: return
        val dirFile = java.io.File(collectionsDir)
        if (dirFile.exists() && dirFile.isDirectory) {
            dirFile.listFiles { f -> f.isDirectory }?.forEach { dir ->
                val name = dir.name.replace('_', ' ') // optional: prettify underscores
                if ((0 until collectionBox.itemCount).none { collectionBox.getItemAt(it) == name }) {
                    collectionBox.addItem(name)
                }
            }
        }
    }


}

