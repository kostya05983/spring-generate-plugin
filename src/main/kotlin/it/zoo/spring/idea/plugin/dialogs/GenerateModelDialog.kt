package it.zoo.spring.idea.plugin.dialogs

import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
import com.intellij.openapi.ui.LabeledComponent
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextField
import org.jetbrains.kotlin.psi.KtClass
import java.awt.BorderLayout
import javax.swing.BoxLayout
import javax.swing.JComponent
import javax.swing.JPanel

class GenerateModelDialog(
    private val ktClass: KtClass
) : BaseDialog(ktClass.project) {

    override fun createCenterPanel(): JComponent? {
        val jComponent = JPanel()
        jComponent.layout = BoxLayout(jComponent, BoxLayout.Y_AXIS)
        val removePrefixField = createRemovePrefix()
        val addPrefixField = createAddPrefix()
        jComponent.add(removePrefixField)
        jComponent.add(addPrefixField)

        val folder = createPackageChooserPath()
        jComponent.add(folder)

        return jComponent
    }

    private fun createRemovePrefix(): JComponent {
        val field = JBTextField()
        field.columns = 15
        return LabeledComponent.create(field, "Удалить постфикс", BorderLayout.CENTER)
    }

    private fun createAddPrefix(): JComponent {
        val field = JBTextField()
        field.columns = 15
        return LabeledComponent.create(field, "Добавить постфикс", BorderLayout.CENTER)
    }

    private fun createPackageChooserPath(): JComponent {
        val field = TextFieldWithBrowseButton()
        field.addBrowseFolderListener(
            "Тест",
            "",
            ktClass.project,
            FileChooserDescriptorFactory.createSingleFolderDescriptor()
        )
        return field
    }
}