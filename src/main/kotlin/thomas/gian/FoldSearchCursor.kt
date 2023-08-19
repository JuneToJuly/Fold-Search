package thomas.gian

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.startOffset
import com.intellij.ui.LanguageTextField
import com.intellij.ui.LanguageTextField.SimpleDocumentCreator
import java.awt.BorderLayout
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JPanel


class FoldSearchCursor : FoldSearchBase() {
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val manager = actionEvent.project?.let { FileEditorManager.getInstance(it) }
        val project = actionEvent.project

        val topPanel = JPanel(BorderLayout())
        val searchField = LanguageTextField(Language.findLanguageByID("Java"), project, "", SimpleDocumentCreator(), false)
        topPanel.add(searchField, BorderLayout.CENTER)

        val psiDocumentManager = project?.let { PsiDocumentManager.getInstance(it) }
        val psiFile = manager?.selectedTextEditor?.document?.let { psiDocumentManager?.getPsiFile(it) }
        val cursorText = manager?.selectedTextEditor?.caretModel?.currentCaret?.offset?.let { psiFile?.findElementAt(it) }

        searchField.setSize(400, 120)
        searchField.font = searchField.font.deriveFont(20f)
        searchField.isViewer = false
        searchField.isFocusable = true
        searchField.isEnabled = true
        searchField.isRequestFocusEnabled = true

        searchField.isFocusCycleRoot = true
        searchField.setShowPlaceholderWhenFocused(true)
        searchField.setCaretPosition(0)
        searchField.addDocumentListener(object :
            DocumentListener {
            override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                val selectedTextEditor = manager?.selectedTextEditor

                val searchTerms = searchField.text.split("\n")

                var text = selectedTextEditor?.document?.text?.split("\n")
                val indexedComments = psiFile?.let { EditorUtils.getComments(it) }
                var startingLine = 0
                if (FoldSearchState.getInstance().methodScoping) {
                    val caret = selectedTextEditor?.caretModel
                    val psiFile = selectedTextEditor?.project?.let {
                        PsiDocumentManager.getInstance(it).getPsiFile(
                            selectedTextEditor.document
                        )
                    }
                    var offset = AtomicInteger(-1)
                    caret?.offset?.let { offset.compareAndSet(-1, it) }
                    var findElementAt = offset.let { psiFile?.findElementAt(it.get()) }
                    var method = PsiTreeUtil.getParentOfType(findElementAt, PsiMethod::class.java)
                    startingLine =
                        method?.startOffset?.let { manager?.selectedTextEditor?.document?.getLineNumber(it) } ?: 0

                    text = actionEvent.project?.let { EditorUtils.getMethodTextAtCaret(it) }?.split("\n")
                }

                // Find all lines containing a string
                var lines =
                    text
                        ?.mapIndexed { index: Int, s: String -> Pair(startingLine + index, s) }
                        ?.filter { indexedComments?.none { comment -> comment.first!! <= it.first && comment.second!! >= it.first }!! }
                        ?.filter { line: Pair<Int, String> -> searchTerms.all { line.second.contains(it) } }

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
                        lines?.forEach {
                            val lineIndex = it.first
                            val fullLine = it.second
                            val lineStart = doc.getLineStartOffset(lineIndex)
                            FoldSearchUtil.createFoldSearchRegion(selectedTextEditor, currentOffset, lineStart)
                            currentOffset = lineStart + fullLine.length + 1
                        }
                        FoldSearchUtil.createFoldSearchRegion(selectedTextEditor, currentOffset, doc.getLineEndOffset(doc.lineCount - 1))
                    }
                }
                selectedTextEditor?.let { FoldSearchBehavior.exitFoldSearchField(it) }
            }
        })
        if(cursorText != null) {
            searchField.text = cursorText.text
            searchField.setCaretPosition(cursorText.text.length)

            var window = manager.project.let { ToolWindowManager.getInstance(it).getToolWindow("FoldSearchHistory") }
            val content = window?.contentManager?.getContent(0)
            if(content?.component is FoldSearchHistoryWindow) {
                val updater = content.component as FoldSearchHistoryWindow
                updater.update("Standard", searchField.text.replace(";", "") + "`")
            }
        }
//        val popup = manager?.selectedTextEditor?.let {
//            if (project != null) {
//                val myPop = JBPopupFactory.getInstance().createComponentPopupBuilder(topPanel, searchField)
//                    .setTitle("Search Result Search")
//                    .setMovable(true)
//                    .setResizable(true)
//                    .setFocusable(true)
//                    .setRequestFocus(true)
//                    .setMinSize(Dimension(400, 120))
//                    .createPopup()
//                myPop.showCenteredInCurrentWindow(project)
//            }
//        }
    }
}