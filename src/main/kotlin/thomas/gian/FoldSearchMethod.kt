package thomas.gian

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.ui.LanguageTextField
import com.intellij.ui.LanguageTextField.SimpleDocumentCreator
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicReference
import javax.swing.JPanel

class FoldSearchMethod : FoldSearchBase() {
    val method = AtomicReference<PsiMethod>(null)
    var startingString : String = ""

    override fun actionPerformed(event: AnActionEvent) {
        val manager = event.project?.let { FileEditorManager.getInstance(it) }
        val project = event.project
        val actionEvent = event

        val topPanel = JPanel(BorderLayout())
        val searchField = LanguageTextField(Language.findLanguageByID("Java"), project, "", SimpleDocumentCreator(), false)
        topPanel.add(searchField, BorderLayout.CENTER)

        var offset = AtomicInteger(-1)

        searchField.setSize(400, 120)
        searchField.font = searchField.font.deriveFont(20f)
        searchField.isViewer = false
        searchField.isFocusable = true
        searchField.isEnabled = true
        searchField.isRequestFocusEnabled = true
        searchField.isFocusCycleRoot = true
        searchField.setShowPlaceholderWhenFocused(true)
        searchField.setCaretPosition(0)

        var window = manager?.project?.let { ToolWindowManager.getInstance(it).getToolWindow("FoldSearchHistory") }

        foldSearchPopup = manager?.selectedTextEditor?.let {
            JBPopupFactory.getInstance().createComponentPopupBuilder(topPanel, searchField)
                .setTitle("Fold Search Method")
                .setMovable(true)
                .setResizable(true)
                .setAlpha(0.1f)
                .setFocusable(true)
                .setCancelCallback {
                    val content = window?.contentManager?.getContent(0)
                    if(content?.component is FoldSearchHistoryWindow) {
                        val updater = content.component as FoldSearchHistoryWindow
                        updater.update("Method", searchField.text.replace(";", ""))
                        startingString = ""
                    }
                    true
                }
                .setShowBorder(true)
                .setRequestFocus(true)
                .setMinSize(Dimension(200, 200))
                .createPopup()
        }
        event.dataContext.let { foldSearchPopup?.showInBestPositionFor(it) }

        searchField.addDocumentListener(object :
            DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                val selectedTextEditor = manager?.selectedTextEditor

                var searchTextField = searchField.text
                var ignoreCase = true

                if(searchTextField.contains(";")) {
                    foldSearchPopup?.cancel()
                    return
                }


                val carret = selectedTextEditor?.caretModel
                val psiFile = selectedTextEditor?.project?.let { PsiDocumentManager.getInstance(it).getPsiFile(
                    selectedTextEditor.document
                ) }

                carret?.offset?.let { offset.compareAndSet(-1, it) }
                var findElementAt = offset.let { psiFile?.findElementAt(it.get()) }
                var method = PsiTreeUtil.getParentOfType(findElementAt, PsiMethod::class.java)
                val startingLine = method?.startOffset?.let { manager?.selectedTextEditor?.document?.getLineNumber(it) } ?: 0

                val text = actionEvent.project?.let { EditorUtils.getMethodTextAtCaret(it) }
                val groups = searchField.text.split("~")

                // search for all lines containing each group
                val lines = groups.flatMap {
                    var searchText = it
                    if(it.contains("`")) {
                        ignoreCase = false
                        searchText = it.replace("`", "")
                    }

                    val searchTerms = searchText.split("\n")

                    // Find all lines containing the phrases
                    var lines =
                        text?.split("\n")
                            ?.mapIndexed { index: Int, s: String -> Pair(startingLine + index, s) }
                            ?.filter { line: Pair<Int, String> -> searchTerms.all { line.second.contains(it, ignoreCase) } }
                    lines!!
                }.distinct().sortedWith(compareBy({ it.first }))
                //flatten two list

                // Find all lines containing a string

                var currentOffset = 0
                selectedTextEditor?.foldingModel?.runBatchFoldingOperation {
                    // remove all folds
                    selectedTextEditor.foldingModel.allFoldRegions.forEach {
                        if(!it.group.toString().contains("New"))
                            selectedTextEditor.foldingModel.removeFoldRegion(it)
                        else
                            it.isExpanded = false
                    }

                    manager.selectedTextEditor?.document?.let { doc ->
                        lines.forEach {
                            val lineIndex = it.first
                            val fullLine = it.second
                            val lineStart = doc.getLineStartOffset(lineIndex)
                            val region =
                                selectedTextEditor.foldingModel.addFoldRegion(currentOffset, lineStart, "")
                            region?.isExpanded = false
                            currentOffset = lineStart + fullLine.length + 1
                        }

                        val region =
                            selectedTextEditor.foldingModel.addFoldRegion(
                                currentOffset,
                                doc.getLineEndOffset(doc.lineCount - 1),
                                ""
                            )
                        region?.isExpanded = false
                    }
                }
                selectedTextEditor?.let { FoldSearchBehavior.exitFoldSearchField(it) }
            }
        })

        searchField.text = startingString
        searchField.setCaretPosition(startingString.length)
    }
}