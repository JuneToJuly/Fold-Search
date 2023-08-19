package thomas.gian

import com.intellij.lang.Language
import com.intellij.openapi.project.Project
import com.intellij.ui.LanguageTextField

class FoldSearchLook {

    companion object {
        val instance = FoldSearchLook()

        fun createSearchField(project: Project): LanguageTextField {
            val searchField = LanguageTextField(
                Language.findLanguageByID("Java"), project, "",
                LanguageTextField.SimpleDocumentCreator(), false
            )
            searchField.setSize(400, 120)
            searchField.font = searchField.font.deriveFont(20f)
            searchField.isViewer = false
            searchField.isFocusable = true
            searchField.isEnabled = true
            searchField.isRequestFocusEnabled = true
            searchField.isFocusCycleRoot = true
            searchField.setShowPlaceholderWhenFocused(true)
            searchField.setCaretPosition(0)
            return searchField
        }
    }

}