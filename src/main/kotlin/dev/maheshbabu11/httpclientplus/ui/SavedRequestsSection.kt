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

import com.intellij.icons.AllIcons
import com.intellij.ide.actions.RevealFileAction
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.openapi.vfs.newvfs.BulkFileListener
import com.intellij.openapi.vfs.newvfs.events.VFileEvent
import com.intellij.ui.JBColor
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import dev.maheshbabu11.httpclientplus.service.HttpFileService
import dev.maheshbabu11.httpclientplus.service.HttpRequestData
import java.awt.*
import java.awt.datatransfer.DataFlavor
import java.awt.dnd.DnDConstants
import java.awt.dnd.DropTarget
import java.awt.dnd.DropTargetDragEvent
import java.awt.dnd.DropTargetDropEvent
import java.awt.dnd.DropTargetEvent
import java.awt.event.*
import java.io.File
import java.nio.file.Paths
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.swing.*
import javax.swing.event.DocumentEvent
import javax.swing.event.DocumentListener
import javax.swing.table.*

class SavedRequestsSection(
    private val project: Project,
    private val onRequestSelected: (HttpRequestData, VirtualFile) -> Unit,
    private val onShowResponses: (String, String) -> Unit
) {
    val component: JComponent
    private val tableModel: DefaultTableModel
    private val table: JBTable
    private val sorter: TableRowSorter<DefaultTableModel>
    private val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        .withZone(ZoneId.systemDefault())

    private val lastModifiedInstants = mutableListOf<Instant>()

    init {
        // Table model: Collection | Name | Method | Last Modified
        tableModel = object : DefaultTableModel(arrayOf("Collection", "Name", "Method", "Last Modified"), 0) {
            override fun isCellEditable(row: Int, column: Int) = false
            override fun getColumnClass(column: Int) = when (column) {
                0, 1, 2 -> String::class.java
                3 -> Instant::class.java
                else -> super.getColumnClass(column)
            }
        }

        table = JBTable(tableModel).apply {
            preferredScrollableViewportSize = Dimension(600, 300)
            setSelectionMode(ListSelectionModel.SINGLE_SELECTION)
            rowHeight = 28
            intercellSpacing = Dimension(0, 0)

            columnModel.getColumn(0).preferredWidth = 150 // Collection
            columnModel.getColumn(1).preferredWidth = 250 // Name
            columnModel.getColumn(2).preferredWidth = 80  // Method
            columnModel.getColumn(3).preferredWidth = 150 // Date

            setDefaultRenderer(String::class.java, MethodCellRenderer())
            setDefaultRenderer(Any::class.java, DateCellRenderer())

            selectionForeground = JBColor.foreground()
            selectionBackground = JBColor(0xE0E0E0, 0x454545)
        }

        table.addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                if (e.clickCount == 2 && e.button == MouseEvent.BUTTON1) openSelectedFile()
                if (e.button == MouseEvent.BUTTON3 && table.selectedRow != -1) showContextMenu(e)
            }
        })

        table.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                when (e.keyCode) {
                    KeyEvent.VK_ENTER -> openSelectedFile()
                    KeyEvent.VK_DELETE -> deleteSelectedFile()
                }
            }
        })

        sorter = TableRowSorter(tableModel)
        table.rowSorter = sorter
        sorter.setComparator(0) { a, b -> (a as String).compareTo(b as String, ignoreCase = true) }
        sorter.setComparator(1) { a, b -> (a as String).compareTo(b as String, ignoreCase = true) }
        sorter.setComparator(2) { a, b -> (a as String).compareTo(b as String, ignoreCase = true) }
        sorter.setComparator(3) { a, b ->
            val rowA = tableModel.findRowByValue(a)
            val rowB = tableModel.findRowByValue(b)
            lastModifiedInstants[rowA].compareTo(lastModifiedInstants[rowB])
        }

        val header: JTableHeader = table.tableHeader
        header.reorderingAllowed = false
        header.defaultRenderer = TableCellRenderer { _, value, _, _, _, _ ->
            val label = JLabel(value.toString())
            label.border = BorderFactory.createEmptyBorder(0, 8, 0, 0)
            label
        }

        // File system listener (auto-refresh)
        val connection = project.messageBus.connect()
        connection.subscribe(
            VirtualFileManager.VFS_CHANGES,
            object : BulkFileListener {
                override fun after(events: MutableList<out VFileEvent>) {
                    for (event in events) {
                        val file = event.file ?: continue
                        if (file.extension == "http" && file.path.contains("http-client-plus/collections")) {
                            SwingUtilities.invokeLater { refreshList() }
                        }
                    }
                }
            }
        )

        // Build main component
        component = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(8)
            add(buildSearchPanel(), BorderLayout.NORTH) // ✅ Search
            add(JBScrollPane(table).apply {
                border = JBUI.Borders.customLine(JBColor.border(), 1)
            }, BorderLayout.CENTER)
            add(buildToolbarPanel(), BorderLayout.SOUTH) // ✅ Toolbar
        }

        // Drag & Drop support (import collections)
        component.dropTarget = object : DropTarget() {
            override fun drop(e: DropTargetDropEvent) {
                try {
                    e.acceptDrop(DnDConstants.ACTION_COPY)
                    val transferable = e.transferable
                    if (transferable.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
                        val files = transferable.getTransferData(DataFlavor.javaFileListFlavor) as List<*>
                        files.filterIsInstance<File>()
                            .firstOrNull { it.name.endsWith(".json") }
                            ?.let { importCollection(it) }
                    }
                    e.dropComplete(true)
                } catch (ex: Exception) {
                    e.dropComplete(false)
                }
            }

            override fun dragEnter(e: DropTargetDragEvent) {
                component.background = JBColor(0xE0E8FF, 0x2D3A4C)
                super.dragEnter(e)
            }

            override fun dragExit(e: DropTargetEvent) {
                component.background = JBColor.background()
                super.dragExit(e)
            }
        }

        refreshList()
    }

    // === Toolbar ===
    private fun buildToolbarPanel(): JComponent {
        return JPanel(FlowLayout(FlowLayout.LEFT, 4, 4)).apply {
            background = null
            add(createToolbarButton(AllIcons.Actions.Edit, "Open File") { openSelectedFile() })
            add(createToolbarButton(AllIcons.General.Delete, "Delete File") { deleteSelectedFile() })
            add(createToolbarButton(AllIcons.General.Refresh, "Refresh Requests") { refreshList() })
            add(createToolbarButton(AllIcons.ToolbarDecorator.Import, "Import Collection") { importCollection() })
        }
    }

    private fun createToolbarButton(icon: Icon, tooltip: String, action: () -> Unit): JButton {
        return JButton(icon).apply {
            putClientProperty("JButton.buttonType", "toolbar")
            toolTipText = tooltip
            isFocusable = false
            border = JBUI.Borders.empty(4)
            addActionListener { action() }
        }
    }

    // === Search ===
    private fun buildSearchPanel(): JComponent {
        val searchField = JTextField(20)
        val clearButton = JButton(AllIcons.Actions.Close).apply {
            border = null
            isContentAreaFilled = false
            isFocusPainted = false
            isFocusable = false
            isVisible = false
            addActionListener {
                searchField.text = ""
                sorter.rowFilter = null
                isVisible = false
            }
        }

        searchField.document.addDocumentListener(object : DocumentListener {
            private fun updateFilter() {
                val text = searchField.text
                sorter.rowFilter = if (text.isBlank()) null
                else RowFilter.regexFilter("(?i)$text", 1) // search by Name
                clearButton.isVisible = text.isNotEmpty()
            }

            override fun insertUpdate(e: DocumentEvent) = updateFilter()
            override fun removeUpdate(e: DocumentEvent) = updateFilter()
            override fun changedUpdate(e: DocumentEvent) = updateFilter()
        })

        return JPanel(BorderLayout()).apply {
            border = BorderFactory.createEmptyBorder(0, 0, 8, 0)
            add(JLabel("Search: "), BorderLayout.WEST)
            add(JPanel(BorderLayout()).apply {
                add(searchField, BorderLayout.CENTER)
                add(clearButton, BorderLayout.EAST)
            }, BorderLayout.CENTER)
        }
    }

    // === Renderers ===
    private inner class MethodCellRenderer : DefaultTableCellRenderer() {
        private val methodColors = mapOf(
            "GET" to JBColor(0x22863A, 0x2DB84D),
            "POST" to JBColor(0x6F42C1, 0x8A63D2),
            "PUT" to JBColor(0x005CC5, 0x1C7CD5),
            "DELETE" to JBColor(0xD73A49, 0xE5534B),
            "PATCH" to JBColor(0xE36209, 0xF57C00),
            "HEAD" to JBColor(0x6F42C1, 0x8A63D2),
            "OPTIONS" to JBColor(0x005CC5, 0x1C7CD5)
        )

        override fun getTableCellRendererComponent(
            table: JTable, value: Any?, isSelected: Boolean,
            hasFocus: Boolean, row: Int, column: Int
        ): Component {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            val method = value?.toString()?.uppercase()
            background = if (isSelected) table.selectionBackground else table.background
            foreground = methodColors[method] ?: JBColor.foreground()
            font = font.deriveFont(Font.BOLD)
            border = BorderFactory.createEmptyBorder(0, 8, 0, 8)
            return this
        }
    }

    private inner class DateCellRenderer : DefaultTableCellRenderer() {
        override fun getTableCellRendererComponent(
            table: JTable, value: Any?, isSelected: Boolean,
            hasFocus: Boolean, row: Int, column: Int
        ): Component {
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column)
            foreground = JBColor(0x666666, 0x999999)
            horizontalAlignment = SwingConstants.LEFT
            border = BorderFactory.createEmptyBorder(0, 8, 0, 8)
            return this
        }
    }


    // === Context Menu ===
    private fun showContextMenu(e: MouseEvent) {
        val popup = JPopupMenu().apply {
            add(JMenuItem("Open", AllIcons.Actions.Edit).apply { addActionListener { openSelectedFile() } })
            add(JMenuItem("Delete", AllIcons.General.Delete).apply { addActionListener { deleteSelectedFile() } })
            addSeparator()
            add(JMenuItem("Show Responses", AllIcons.FileTypes.Json).apply {
                addActionListener {
                    val selectedRow = table.selectedRow
                    if (selectedRow >= 0) {
                        val modelRow = table.convertRowIndexToModel(selectedRow)
                        val collection = tableModel.getValueAt(modelRow, 0)?.toString() ?: return@addActionListener
                        val requestName = tableModel.getValueAt(modelRow, 1)?.toString() ?: return@addActionListener
                        onShowResponses(collection, requestName)
                    }
                }
            })

            add(JMenuItem("Show in Files", AllIcons.Actions.NewFolder).apply {
                addActionListener {
                    getSelectedFile()?.let { vFile ->
                        RevealFileAction.openFile(Paths.get(vFile.path))
                    } ?: run {
                        Messages.showErrorDialog(project, "File not found or not accessible", "Show in Files Failed")
                    }
                }
            })
        }
        popup.show(table, e.x, e.y)
    }

    // === File Handling ===
    private fun refreshList() {
        tableModel.rowCount = 0
        lastModifiedInstants.clear()

        val collectionsDir = File(project.basePath, "http-client-plus/collections")
        if (collectionsDir.exists() && collectionsDir.isDirectory) {
            collectionsDir.listFiles { f -> f.isDirectory }?.forEach { collectionDir ->
                collectionDir.listFiles { f -> f.isFile && f.extension == "http" }
                    ?.forEach { file ->
                        val instant = Instant.ofEpochMilli(file.lastModified())
                        lastModifiedInstants.add(instant)
                        val method = parseHttpMethod(file)
                        tableModel.addRow(
                            arrayOf(
                                collectionDir.name.replace("_", " "),
                                file.name.replace("_", " ").substringBefore(".http"),
                                method,
                                formatter.format(instant)
                            )
                        )
                    }
            }
        }
    }

    private fun parseHttpMethod(file: File): String =
        runCatching {
            file.useLines { lines ->
                lines.filterNot { it.isBlank() || it.trim().startsWith("#") || it.trim().startsWith("//") }
                    .firstOrNull {
                        it.matches(
                            Regex(
                                "^(GET|POST|PUT|PATCH|DELETE|HEAD|OPTIONS)\\s+.*",
                                RegexOption.IGNORE_CASE
                            )
                        )
                    }
                    ?.trim()
                    ?.substringBefore(" ")
                    ?.uppercase()
            } ?: "GET"
        }.getOrDefault("GET")


    private fun getSelectedFile(): VirtualFile? {
        val selectedRow = table.selectedRow
        if (selectedRow < 0) return null
        val modelRow = table.convertRowIndexToModel(selectedRow)
        val collection = tableModel.getValueAt(modelRow, 0) as? String ?: return null
        val collectionDir = collection.replace(" ", "_")
        val fileName = tableModel.getValueAt(modelRow, 1) as? String ?: return null
        val filePathName = fileName.replace(" ", "_") + ".http"
        val file = File(project.basePath, "http-client-plus/collections/$collectionDir/$filePathName")
        return LocalFileSystem.getInstance().findFileByIoFile(file)
    }

    private fun openSelectedFile() {
        val vFile = getSelectedFile() ?: return
        val data = HttpFileService.parseRequestFile(project, vFile) ?: return
        onRequestSelected(data, vFile)
       // FileEditorManager.getInstance(project).openFile(vFile, true)
    }

    private fun deleteSelectedFile() {
        val vFile = getSelectedFile() ?: return
        if (Messages.showYesNoDialog(
                project,
                "Delete this request?",
                "Confirm Delete",
                AllIcons.General.Delete
            ) == Messages.YES
        ) {
            ApplicationManager.getApplication().runWriteAction { vFile.delete(this) }
            refreshList()
        }
    }

    private fun DefaultTableModel.findRowByValue(value: Any): Int {
        for (i in 0 until rowCount) {
            if (getValueAt(i, 3) == value) return i
        }
        return -1
    }

    // === Import Postman Collection ===
    private fun importCollection(file: File? = null) {
        val selectedFile = file ?: run {
            val chooser = JFileChooser().apply {
                fileSelectionMode = JFileChooser.FILES_ONLY
                fileFilter = object : javax.swing.filechooser.FileFilter() {
                    override fun accept(f: File) = f.isDirectory || f.name.endsWith(".json")
                    override fun getDescription() = "Postman Collection (*.json)"
                }
            }
            if (chooser.showOpenDialog(component) != JFileChooser.APPROVE_OPTION) return
            chooser.selectedFile
        }

        try {
            val importer = PostmanImporter(project)
            val requests = importer.importFromFile(selectedFile)
            var success = 0
            requests.forEach { req ->
                if (HttpFileService.createRequestFile(project, req) != null) success++
            }
            refreshList()
            JOptionPane.showMessageDialog(
                component,
                "Successfully imported $success/${requests.size} requests",
                "Import Complete",
                JOptionPane.INFORMATION_MESSAGE
            )
        } catch (e: Exception) {
            JOptionPane.showMessageDialog(
                component,
                "Failed to import: ${e.message}",
                "Import Error",
                JOptionPane.ERROR_MESSAGE
            )
        }
    }
}
