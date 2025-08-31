package dev.maheshbabu11.httpclientplus.ui

import com.intellij.openapi.fileChooser.FileChooser
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.vfs.VfsUtil
import java.awt.Component
import javax.swing.AbstractCellEditor
import javax.swing.JTable
import javax.swing.table.TableCellEditor

// Editor with a TextFieldWithBrowseButton to choose a file path
class FileChooserCellEditor(private val project: Project) : AbstractCellEditor(), TableCellEditor {
    private val field = TextFieldWithBrowseButton()
    private var currentValue: String? = null

    init {
        val descriptor = FileChooserDescriptor(
            true,
            false,
            false,
            false,
            false,
            false
        )
        field.addActionListener {
            val toSelect = if (!currentValue.isNullOrBlank()) VfsUtil.findFile(
                java.nio.file.Path.of(currentValue!!),
                true
            ) else null
            val chosen = FileChooser.chooseFile(descriptor, project, toSelect)
            if (chosen != null) {
                field.text = chosen.path
            }
        }
    }

    override fun getTableCellEditorComponent(
        table: JTable?,
        value: Any?,
        isSelected: Boolean,
        row: Int,
        column: Int
    ): Component {
        currentValue = (value as? String)
        field.text = currentValue ?: ""
        return field
    }

    override fun getCellEditorValue(): Any {
        return field.text
    }
}

