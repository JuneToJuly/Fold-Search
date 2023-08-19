package thomas.gian

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.ui.LanguageTextField
import com.intellij.ui.LanguageTextField.SimpleDocumentCreator
import java.awt.BorderLayout
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JPanel


class FoldSearchCursorMethod : FoldSearchBase() {
    override fun actionPerformed(event: AnActionEvent) {
        val manager = event.project?.let { FileEditorManager.getInstance(it) }
        val project = event.project

        var offset = AtomicInteger(-1)

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

                var searchTextField = searchField.text
                var ignoreCase = false
                if(searchTextField.contains("`")) {
                    ignoreCase = false
                    searchTextField = searchTextField.replace("`", "")
                }
                val searchTerms = searchTextField.split("\n")


                val carret = selectedTextEditor?.caretModel
                val psiFile = selectedTextEditor?.project?.let { PsiDocumentManager.getInstance(it).getPsiFile(
                    selectedTextEditor.document
                ) }

                carret?.offset?.let { offset.compareAndSet(-1, it) }

                var findElementAt = offset.let { psiFile?.findElementAt(it.get()) }
                val method = PsiTreeUtil.getParentOfType(findElementAt, PsiMethod::class.java)
                val startingLine = method?.startOffset?.let { manager?.selectedTextEditor?.document?.getLineNumber(it) } ?: 0
                val endingLine = method?.endOffset?.let { manager?.selectedTextEditor?.document?.getLineNumber(it) } ?: 0
                val text = selectedTextEditor?.document?.getText(TextRange(
                    selectedTextEditor.document.getLineStartOffset(startingLine),
                    selectedTextEditor.document.getLineEndOffset(endingLine)
                ))

                // Find all lines containing a string
                var lines =
                    text?.split("\n")
                        ?.mapIndexed { index: Int, s: String -> Pair(startingLine + index, s) }
                        ?.filter { line: Pair<Int, String> -> searchTerms.all { line.second.contains(it, ignoreCase) } }

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
        if(cursorText != null) {
            searchField.text = cursorText.text
            searchField.setCaretPosition(cursorText.text.length)

            var window = manager.project.let { ToolWindowManager.getInstance(it).getToolWindow("FoldSearchHistory") }
            val content = window?.contentManager?.getContent(0)
            if(content?.component is FoldSearchHistoryWindow) {
                val updater = content.component as FoldSearchHistoryWindow
                updater.update("Method", searchField.text.replace(";", ""))
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