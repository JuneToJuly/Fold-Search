package thomas.gian

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.JBPopupListener
import com.intellij.openapi.util.TextRange
import com.intellij.ui.LanguageTextField
import com.intellij.ui.LanguageTextField.SimpleDocumentCreator
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JPanel

class YankAndPut : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val manager = event.project?.let { FileEditorManager.getInstance(it) }
        val project = event.project

        val topPanel = JPanel(BorderLayout())
        val searchField = LanguageTextField(Language.findLanguageByID("Java"), project, "", SimpleDocumentCreator(), false)
        topPanel.add(searchField, BorderLayout.CENTER)

        searchField.setSize(400, 120)
        searchField.font = searchField.font.deriveFont(20f)
        searchField.isViewer = false
        searchField.isFocusable = true
        searchField.isEnabled = true
        searchField.isRequestFocusEnabled = true
        searchField.isFocusCycleRoot = true
        searchField.setShowPlaceholderWhenFocused(true)
        searchField.setCaretPosition(0)
        var lines = listOf<String>()

        val popup = manager?.selectedTextEditor?.let {
            JBPopupFactory.getInstance().createComponentPopupBuilder(topPanel, searchField)
                .setTitle("Search Result Search")
                .setMovable(true)
                .setResizable(true)
                .setFocusable(true)
                .setRequestFocus(true)
                .setMinSize(Dimension(400, 120))
                .createPopup()
        }
        popup?.showInFocusCenter()

        searchField.addDocumentListener(object :
            DocumentListener {
            override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                val selectedTextEditor = manager?.selectedTextEditor

                var searchTextField = searchField.text
                var ignoreCase = true
                if(searchField.text.contains(";")){
                    popup?.cancel()
                    return
                }
                if(searchTextField.contains("`")) {
                    ignoreCase = false
                    searchTextField = searchTextField.replace("`", "")
                }
                val searchTerms = searchTextField.split("\n")

                // Find all lines containing a string
                // select the start between first value and last
                // paste that at character

                val visualLocation = selectedTextEditor?.scrollingModel?.visibleArea?.location?.y
                val editorHeight = selectedTextEditor?.scrollingModel?.visibleArea?.height

                var lines =
                    selectedTextEditor?.document?.getText(TextRange(visualLocation!!, visualLocation + editorHeight!!))?.split("\n")
                        ?.mapIndexed { index: Int, s: String -> Pair(index, s) }
                        ?.filter { line: Pair<Int, String> -> searchTerms.all { line.second.contains(it, ignoreCase) } }
//                        ?.map { line: Pair<Int, String> -> line.second.substring(line.second.indexOf(searchTerms.first()), line.second.indexOf(searchTerms.last())) }!!
                        ?.map { line: Pair<Int, String> -> line }!!

                println(lines)

                lines.forEach { println(it) }
            }

        })

        val addListener = popup?.addListener(object : JBPopupListener {
            override fun onClosed(event: com.intellij.openapi.ui.popup.LightweightWindowEvent) {
                WriteCommandAction.runWriteCommandAction(project, {
                    manager.selectedTextEditor?.document?.insertString(
                        manager.selectedTextEditor?.caretModel?.offset ?: 0, searchField.text
                    )
                })
            }
        })

    }
}