
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
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.vfs.LocalFileSystem
import com.intellij.ui.OnePixelSplitter
import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBLabel
import com.intellij.ui.table.JBTable
import com.intellij.ui.components.panels.HorizontalLayout
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import javax.swing.*
import javax.swing.table.DefaultTableModel

class EnvEditorSection(private val project: Project, private val onSaved: (() -> Unit)? = null) {
    private enum class Scope { Public, Private }

    private val scopeBox = ComboBox(Scope.entries.toTypedArray())
    private val envListModel = DefaultListModel<String>()
    private val envList = JBList(envListModel).apply {
        selectionMode = ListSelectionModel.SINGLE_SELECTION
        visibleRowCount = 8
        fixedCellHeight = 22
    }

    private val varsModel: DefaultTableModel = object : DefaultTableModel(0, 2) {
        override fun isCellEditable(row: Int, column: Int): Boolean = true
    }.apply { setColumnIdentifiers(arrayOf("Key", "Value")) }

    private val varsTable: JBTable = JBTable(varsModel).apply {
        emptyText.text = "No variables. Click + to add."
        columnModel.getColumn(0).preferredWidth = 200
        columnModel.getColumn(1).preferredWidth = 320
        rowHeight = 24
        putClientProperty("terminateEditOnFocusLost", true)
    }

    // flat, borderless icon buttons
    private fun iconButton(icon: Icon, tooltip: String) = JButton(icon).apply {
        toolTipText = tooltip
        isContentAreaFilled = false
        isBorderPainted = false
        isFocusPainted = false
        isOpaque = false
        border = JBUI.Borders.empty()
        margin = JBUI.emptyInsets()
        isFocusable = false
    }

    private val addEnvButton = iconButton(AllIcons.General.Add, "Add environment")
    private val removeEnvButton = iconButton(AllIcons.General.Remove, "Remove selected environment")
    private val saveButton = iconButton(AllIcons.Actions.MenuSaveall, "Save environments")
    private val openFileButton = iconButton(AllIcons.Actions.MenuOpen, "Open environment file")
    private val refreshButton = iconButton(AllIcons.Actions.Refresh, "Refresh environment list")

    val component: JComponent = buildPanel()

    private fun buildPanel(): JComponent {
        val panel = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.empty(10)
        }

        // Top bar
        val top = JPanel(BorderLayout()).apply {
            border = JBUI.Borders.emptyBottom(8)
        }

        val left = JPanel(HorizontalLayout(6)).apply {
            add(JBLabel("Type:"))
            add(scopeBox)
        }

        val right = JPanel(HorizontalLayout(4)).apply {
            add(addEnvButton)
            add(removeEnvButton)
            add(saveButton)
            add(openFileButton)
            add(refreshButton)
        }

        top.add(left, BorderLayout.WEST)
        top.add(right, BorderLayout.EAST)
        panel.add(top, BorderLayout.NORTH)

        val split = OnePixelSplitter(false, 0.3f).apply {
            firstComponent = buildEnvPanel()
            secondComponent = buildVarsPanel()
        }

        panel.add(split, BorderLayout.CENTER)

        // listeners
        scopeBox.addActionListener { reloadEnvList() }
        addEnvButton.addActionListener { addEnvironment() }
        removeEnvButton.addActionListener { removeEnvironment() }
        saveButton.addActionListener { saveCurrentScope() }
        openFileButton.addActionListener { openCurrentEnvFile() }
        refreshButton.addActionListener { reloadEnvList() }
        envList.addListSelectionListener { if (!it.valueIsAdjusting) loadVarsForSelectedEnv() }

        // init
        reloadEnvList()
        if (envListModel.size() > 0) envList.selectedIndex = 0

        return panel
    }

    private fun buildEnvPanel(): JComponent =
        JPanel(BorderLayout()).apply {
            border = JBUI.Borders.emptyRight(8)
            add(JBLabel("Environments"), BorderLayout.NORTH)
            add(JBScrollPane(envList), BorderLayout.CENTER)
        }

    private fun buildVarsPanel(): JComponent =
        JPanel(BorderLayout()).apply {
            add(JBLabel("Variables"), BorderLayout.NORTH)
            val decoratedVars = ToolbarDecorator.createDecorator(varsTable)
                .setAddAction { _ -> varsModel.addRow(arrayOf("", "")) }
                .setRemoveAction { _ ->
                    val rows = varsTable.selectedRows.sortedDescending()
                    rows.forEach { varsModel.removeRow(it) }
                    if (varsModel.rowCount == 0) varsModel.addRow(arrayOf("", ""))
                }
                .disableUpDownActions()
                .createPanel()
            add(decoratedVars, BorderLayout.CENTER)
        }

    private fun reloadEnvList() {
        envListModel.clear()
        val names: List<String> = when (currentScope()) {
            Scope.Public -> EnvManager.loadPublic(project).keys.sorted()
            Scope.Private -> EnvManager.loadPrivate(project).keys.sorted()
        }
        names.forEach { envListModel.addElement(it) }
        if (envListModel.size() == 0) {
            varsModel.setRowCount(0)
            ensureVarsRow()
            return
        }
        envList.selectedIndex = 0
        loadVarsForSelectedEnv()
    }

    private fun loadVarsForSelectedEnv() {
        varsModel.setRowCount(0)
        val env = envList.selectedValue ?: return
        val map = when (currentScope()) {
            Scope.Public -> EnvManager.loadPublic(project)[env] ?: emptyMap()
            Scope.Private -> EnvManager.loadPrivate(project)[env] ?: emptyMap()
        }
        if (map.isEmpty()) {
            ensureVarsRow()
            return
        }
        map.forEach { (k, v) -> varsModel.addRow(arrayOf(k, v.toString())) }
    }

    private fun addEnvironment() {
        val name = Messages.showInputDialog(
            component,
            "Environment name:",
            "Add Environment",
            Messages.getQuestionIcon()
        )?.trim().orEmpty()

        if (name.isEmpty()) return
        if ((0 until envListModel.size()).any { envListModel.getElementAt(it).equals(name, true) }) {
            Messages.showInfoMessage(component, "Environment already exists", "Info")
            return
        }
        envListModel.addElement(name)
        envList.selectedIndex = envListModel.size() - 1
        varsModel.setRowCount(0)
        ensureVarsRow()
    }

    private fun removeEnvironment() {
        val idx = envList.selectedIndex
        if (idx < 0) return
        envListModel.remove(idx)
        varsModel.setRowCount(0)
        ensureVarsRow()
    }

    private fun saveCurrentScope() {
        val pub = EnvManager.loadPublic(project).toMutableMap()
        val prv = EnvManager.loadPrivate(project).toMutableMap()

        val allNames = (0 until envListModel.size()).map { envListModel.getElementAt(it) }
        val currentEnv = envList.selectedValue
        val currentVars = collectVars()

        when (currentScope()) {
            Scope.Public -> {
                pub.keys.filter { it !in allNames }.toList().forEach { pub.remove(it) }
                allNames.forEach { name ->
                    if (name == currentEnv) pub[name] = currentVars.toMutableMap()
                    else pub.putIfAbsent(name, mutableMapOf())
                }
                EnvManager.savePublic(project, pub)
            }

            Scope.Private -> {
                prv.keys.filter { it !in allNames }.toList().forEach { prv.remove(it) }
                allNames.forEach { name ->
                    if (name == currentEnv) prv[name] = currentVars.toMutableMap()
                    else prv.putIfAbsent(name, mutableMapOf())
                }
                EnvManager.savePrivate(project, prv)
            }
        }
        onSaved?.invoke()
        Messages.showInfoMessage(component, "Saved", "Environments")
    }

    private fun collectVars(): Map<String, Any> {
        val map = linkedMapOf<String, Any>()
        for (i in 0 until varsModel.rowCount) {
            val k = (varsModel.getValueAt(i, 0) as? String)?.trim().orEmpty()
            val vStr = (varsModel.getValueAt(i, 1) as? String)?.trim().orEmpty()
            if (k.isEmpty()) continue
            val v: Any = when {
                vStr.equals("true", true) -> true
                vStr.equals("false", true) -> false
                else -> vStr
            }
            map[k] = v
        }
        return map
    }

    private fun ensureVarsRow() {
        if (varsModel.rowCount == 0) varsModel.addRow(arrayOf("", ""))
    }

    private fun currentScope(): Scope = scopeBox.selectedItem as Scope

    private fun openCurrentEnvFile() {
        val files = EnvManager.envFiles(project)
        if (files == null) {
            Messages.showErrorDialog(component, "Project base path not available", "Error")
            return
        }
        val path = when (currentScope()) {
            Scope.Public -> files.publicPath
            Scope.Private -> files.privatePath
        }
        try {
            val parent = path.parent
            if (parent != null && !Files.exists(parent)) Files.createDirectories(parent)
            if (!Files.exists(path)) Files.writeString(path, "{}\n", StandardCharsets.UTF_8)
            val vFile = LocalFileSystem.getInstance().refreshAndFindFileByPath(path.toString())
            if (vFile != null) {
                FileEditorManager.getInstance(project).openFile(vFile, true)
            } else {
                Messages.showErrorDialog(component, "Unable to open file: $path", "Error")
            }
        } catch (e: Exception) {
            Messages.showErrorDialog(component, "Failed to open file: ${e.message}", "Error")
        }
    }
}
