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

import com.intellij.openapi.ui.ComboBox
import java.awt.Component
import javax.swing.AbstractAction
import javax.swing.AbstractCellEditor
import javax.swing.DefaultComboBoxModel
import javax.swing.JTable
import javax.swing.KeyStroke
import javax.swing.SwingUtilities
import javax.swing.table.TableCellEditor

// Editor with an editable ComboBox that filters options by typed prefix and still allows any custom input
class PrefixFilteringComboBoxCellEditor(private val options: List<String>) : AbstractCellEditor(), TableCellEditor {
    private val combo = ComboBox(options.toTypedArray()).apply {
        isEditable = true
        // Hint LAF/behaviors that this combo lives inside a table editor
        putClientProperty("JComboBox.isTableCellEditor", true)
    }
    private val model = DefaultComboBoxModel(options.toTypedArray())
    private val textComp = (combo.editor.editorComponent as javax.swing.text.JTextComponent)
    private var updating = false
    // Track last typed value to avoid redundant rebuilds
    private var lastTyped: String = ""
    // Flag when user is navigating popup with arrow keys; skip filtering then
    private var navigating = false
    // Debounce updates to avoid racing with mouse selection and reduce churn
    private val debounce = javax.swing.Timer(120) { doFilterUpdate() }.apply { isRepeats = false }

    init {
        combo.model = model
        // Show popup on focus to hint available options
        combo.addAncestorListener(object : javax.swing.event.AncestorListener {
            override fun ancestorAdded(event: javax.swing.event.AncestorEvent?) {
                SwingUtilities.invokeLater { if (!combo.isPopupVisible) combo.showPopup() }
            }
            override fun ancestorMoved(event: javax.swing.event.AncestorEvent?) {}
            override fun ancestorRemoved(event: javax.swing.event.AncestorEvent?) {}
        })
        // Detect arrow navigation to avoid filtering while browsing the popup
        textComp.addKeyListener(object : java.awt.event.KeyAdapter() {
            override fun keyPressed(e: java.awt.event.KeyEvent) {
                if (e.keyCode == java.awt.event.KeyEvent.VK_UP || e.keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                    navigating = true
                }
            }
            override fun keyReleased(e: java.awt.event.KeyEvent) {
                if (e.keyCode == java.awt.event.KeyEvent.VK_UP || e.keyCode == java.awt.event.KeyEvent.VK_DOWN) {
                    // Release after event cycle so selection can settle
                    SwingUtilities.invokeLater { navigating = false }
                }
            }
        })
        // Commit on Enter and cancel on Escape while editing inside the table
        val commitAction = object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                if (updating) return
                updating = true
                // Defer to let JComboBox/JList update the selection first
                SwingUtilities.invokeLater {
                    try {
                        // If popup is visible, prefer the highlighted selection over raw text
                        val selected = combo.selectedItem
                        if (selected != null) combo.editor.item = selected
                        combo.hidePopup()
                        stopCellEditing()
                    } finally {
                        updating = false
                    }
                }
            }
        }
        val cancelAction = object : AbstractAction() {
            override fun actionPerformed(e: java.awt.event.ActionEvent?) {
                cancelCellEditing()
            }
        }
        // Bind on the editor text field
        textComp.inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "commit")
        textComp.actionMap.put("commit", commitAction)
        textComp.inputMap.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "cancel")
        textComp.actionMap.put("cancel", cancelAction)
        // Also bind on the ComboBox as an ancestor so Enter works when popup list has focus
        val im = (combo as javax.swing.JComponent).getInputMap(javax.swing.JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT)
        val am = combo.actionMap
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ENTER, 0), "commitFromCombo")
        am.put("commitFromCombo", commitAction)
        im.put(KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_ESCAPE, 0), "cancelFromCombo")
        am.put("cancelFromCombo", cancelAction)
        // Override default UI "enterPressed" to ensure commit even when popup consumes Enter
        am.put("enterPressed", commitAction)

        textComp.document.addDocumentListener(object : javax.swing.event.DocumentListener {
            private fun schedule() {
                if (updating || navigating) return
                // If the editor text already equals current selection, don't rebuild
                val typed = textComp.text ?: ""
                if (typed == combo.selectedItem?.toString()) return
                if (typed == lastTyped) return
                // Debounce to avoid interfering with mouse clicks
                debounce.restart()
            }
            override fun insertUpdate(e: javax.swing.event.DocumentEvent?) = schedule()
            override fun removeUpdate(e: javax.swing.event.DocumentEvent?) = schedule()
            override fun changedUpdate(e: javax.swing.event.DocumentEvent?) = schedule()
        })
    }

    private fun doFilterUpdate() {
        if (updating || navigating) return
        val typed = textComp.text ?: ""
        if (typed == lastTyped) return
        lastTyped = typed
        val matches = if (typed.isBlank()) options else options.filter { it.startsWith(typed, ignoreCase = true) }
        updating = true
        val hadPopup = combo.isPopupVisible
        try {
            model.removeAllElements()
            if (typed.isNotEmpty() && (matches.isEmpty() || matches.none { it.equals(typed, true) })) {
                model.addElement(typed)
            }
            matches.forEach { model.addElement(it) }
            // Keep user's typed value in the editor without toggling selection events
            combo.editor.item = typed
            if (!hadPopup && model.size > 0) combo.showPopup()
        } finally {
            updating = false
        }
    }

    override fun getTableCellEditorComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        val initial = (value as? String).orEmpty()
        updating = true
        model.removeAllElements()
        options.forEach { model.addElement(it) }
        // Align selection with the initial value to prevent auto-selecting the first option
        val match = options.firstOrNull { it.equals(initial, ignoreCase = true) }
        if (!initial.isNullOrEmpty()) {
            if (match != null) {
                combo.selectedItem = match
            } else {
                model.insertElementAt(initial, 0)
                combo.selectedIndex = 0
            }
        } else {
            combo.selectedIndex = -1
        }
        combo.editor.item = initial
        lastTyped = initial
        updating = false

        SwingUtilities.invokeLater {
            try { textComp.select(initial.length, initial.length) } catch (_: Exception) {}
            if (!combo.isPopupVisible) combo.showPopup()
        }
        return combo
    }

    override fun getCellEditorValue(): Any {
        val item = combo.editor.item
        return (item as? String) ?: item?.toString().orEmpty()
    }
}
