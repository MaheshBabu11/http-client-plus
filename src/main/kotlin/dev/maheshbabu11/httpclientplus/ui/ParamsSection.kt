
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

import com.intellij.ui.ToolbarDecorator
import com.intellij.ui.table.JBTable
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JPanel
import javax.swing.ListSelectionModel
import javax.swing.table.DefaultTableModel

class ParamsSection {
    private val model: DefaultTableModel = object : DefaultTableModel(0, 2) {
        override fun isCellEditable(row: Int, column: Int): Boolean = true
    }.apply { setColumnIdentifiers(arrayOf("Key", "Value")) }

    private val table: JBTable = JBTable(model).apply {
        emptyText.text = "No query params. Click + to add."
        columnModel.getColumn(0).preferredWidth = 220
        columnModel.getColumn(1).preferredWidth = 320
        rowHeight = 24
        selectionModel.selectionMode = ListSelectionModel.MULTIPLE_INTERVAL_SELECTION
        putClientProperty("terminateEditOnFocusLost", java.lang.Boolean.TRUE)
    }

    val component: JPanel = JPanel(BorderLayout()).apply {
        border = JBUI.Borders.empty(10)
        val decorated = ToolbarDecorator.createDecorator(table)
            .setAddAction { _ -> model.addRow(arrayOf("", "")) }
            .setRemoveAction { _ ->
                val rows = table.selectedRows.sortedDescending()
                rows.forEach { model.removeRow(it) }
                if (model.rowCount == 0) model.addRow(arrayOf("", ""))
            }
            .disableUpDownActions()
            .createPanel()
        add(decorated, BorderLayout.CENTER)
    }

    init {
        if (model.rowCount == 0) model.addRow(arrayOf("", ""))
    }

    fun getParams(): List<Pair<String, String>> {
        val list = mutableListOf<Pair<String, String>>()
        for (i in 0 until model.rowCount) {
            val key = (model.getValueAt(i, 0) as? String)?.trim().orEmpty()
            val value = (model.getValueAt(i, 1) as? String)?.trim().orEmpty()
            if (key.isNotEmpty()) list += (key to value)
        }
        return list
    }

    fun setParams(params: List<Pair<String, String>>) {
        model.rowCount = 0
        if (params.isEmpty()) {
            model.addRow(arrayOf("", ""))
        } else {
            params.forEach { (k, v) -> model.addRow(arrayOf(k, v)) }
        }
    }

    fun clear() {
        model.rowCount = 0
        model.addRow(arrayOf("", ""))
    }

}
