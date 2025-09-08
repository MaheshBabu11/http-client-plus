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

import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import dev.maheshbabu11.httpclientplus.service.MultipartPart
import java.awt.BorderLayout
import java.awt.FlowLayout
import java.awt.Dimension
import javax.swing.DefaultCellEditor
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.table.DefaultTableModel
import com.intellij.openapi.project.Project
import javax.swing.ListSelectionModel

class MultipartSection(private val project: Project) {
    private val boundaryField = com.intellij.ui.components.JBTextField("WebAppBoundary").apply {
        toolTipText = "Boundary used to separate multipart parts"
        preferredSize = Dimension(240, 28)
    }

    private val model: DefaultTableModel = object : DefaultTableModel(0, 6) {
        override fun isCellEditable(row: Int, column: Int): Boolean = true
    }.apply { setColumnIdentifiers(arrayOf("Type", "Name", "Content-Type", "Value", "File Path", "Filename")) }

    private val table: JBTable = JBTable(model).apply {
        emptyText.text = "No parts. Click + to add."
        rowHeight = 24
        selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        columnModel.getColumn(0).preferredWidth = 90   // Type
        columnModel.getColumn(1).preferredWidth = 180  // Name
        columnModel.getColumn(2).preferredWidth = 180  // Content-Type
        columnModel.getColumn(3).preferredWidth = 220  // Value
        columnModel.getColumn(4).preferredWidth = 260  // File Path
        columnModel.getColumn(5).preferredWidth = 180  // Filename
        putClientProperty("terminateEditOnFocusLost", java.lang.Boolean.TRUE)
        // Type column
        columnModel.getColumn(0).cellEditor =
            DefaultCellEditor(com.intellij.openapi.ui.ComboBox(arrayOf("Text", "File")))
        // Content-Type column
        val contentTypes = arrayOf(
            "text/plain",
            "application/json",
            "text/html",
            "text/csv",
            "application/octet-stream",
            "image/png",
            "image/jpeg"
        )
        columnModel.getColumn(2).cellEditor = DefaultCellEditor(com.intellij.openapi.ui.ComboBox(contentTypes))
        // File chooser editor for File Path column
        columnModel.getColumn(4).cellEditor = FileChooserCellEditor(project)
    }

    val component: JPanel = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.empty()
        val top = JPanel(FlowLayout(FlowLayout.LEFT, 8, 0))
        top.add(JLabel("Boundary:"))
        top.add(boundaryField)
        add(top, BorderLayout.NORTH)

        val decorated = ToolbarDecorator.createDecorator(table)
            .setAddAction { _ ->
                model.addRow(arrayOf("Text", "", "text/plain", "", "", ""))
            }
            .setRemoveAction { _ ->
                val rows = table.selectedRows.sortedDescending()
                rows.forEach { model.removeRow(it) }
                if (model.rowCount == 0) model.addRow(arrayOf("Text", "", "text/plain", "", "", ""))
            }
            .disableUpDownActions()
            .createPanel()
        add(decorated, BorderLayout.CENTER)
    }

    init {
        if (model.rowCount == 0) model.addRow(arrayOf("Text", "", "text/plain", "", "", ""))
    }

    fun getBoundary(): String = boundaryField.text.trim().ifBlank { "WebAppBoundary" }

    fun getParts(): List<MultipartPart> {
        val list = mutableListOf<MultipartPart>()
        for (i in 0 until model.rowCount) {
            val type = (model.getValueAt(i, 0) as? String)?.trim()?.lowercase().orEmpty()
            val name = (model.getValueAt(i, 1) as? String)?.trim().orEmpty()
            val contentType = (model.getValueAt(i, 2) as? String)?.trim()?.ifBlank { null }
            val value = (model.getValueAt(i, 3) as? String)?.trim()?.ifBlank { null }
            val filePath = (model.getValueAt(i, 4) as? String)?.trim()?.ifBlank { null }
            val filename = (model.getValueAt(i, 5) as? String)?.trim()?.ifBlank { null }
            if (name.isEmpty()) continue
            val isFile = type == "file"
            val part = if (isFile) {
                MultipartPart(
                    name = name,
                    isFile = true,
                    filename = filename,
                    contentType = contentType,
                    value = null,
                    filePath = filePath
                )
            } else {
                MultipartPart(
                    name = name,
                    isFile = false,
                    filename = null,
                    contentType = contentType ?: "text/plain",
                    value = value ?: "",
                    filePath = null
                )
            }
            list += part
        }
        return list
    }

    fun setParts(boundary: String?, parts: List<MultipartPart>) {
        boundaryField.text = boundary ?: "WebAppBoundary"
        model.rowCount = 0
        if (parts.isEmpty()) {
            model.addRow(arrayOf("Text", "", "text/plain", "", "", ""))
            return
        }
        parts.forEach { part ->
            if (part.isFile) {
                model.addRow(
                    arrayOf(
                        "File",
                        part.name,
                        part.contentType ?: "application/octet-stream",
                        "",
                        part.filePath ?: "",
                        part.filename ?: ""
                    )
                )
            } else {
                model.addRow(
                    arrayOf(
                        "Text",
                        part.name,
                        part.contentType ?: "text/plain",
                        part.value ?: "",
                        "",
                        ""
                    )
                )
            }
        }
    }

    fun clear() {
        boundaryField.text = "WebAppBoundary"
        model.rowCount = 0
        model.addRow(arrayOf("Text", "", "text/plain", "", "", ""))
    }

}
