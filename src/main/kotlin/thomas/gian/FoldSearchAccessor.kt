package thomas.gian

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.startOffset
import thomas.gian.exile.ExileState
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JPanel

class FoldSearchAccessor : FoldSearchBase() {
    var startingString : String = ""

    override fun actionPerformed(actionEvent: AnActionEvent) {
        val manager = actionEvent.project?.let { FileEditorManager.getInstance(it) }

        val topPanel = JPanel(BorderLayout())
        val searchField = actionEvent.project?.let { FoldSearchLook.createSearchField(it) } ?: return
        topPanel.add(searchField, BorderLayout.CENTER)


        var window = manager?.project?.let { ToolWindowManager.getInstance(it).getToolWindow("FoldSearchHistory") }

        val title = if(FoldSearchState.getInstance().methodScoping) "Fold Search Accessor (Method)" else "Fold Search Accessor"
       foldSearchPopup = manager?.selectedTextEditor?.let {
            JBPopupFactory.getInstance().createComponentPopupBuilder(topPanel, searchField)
                .setTitle(title)
                .setMovable(true)
                .setAlpha(0.1f)
                .setResizable(true)
                .setFocusable(true)
                .setRequestFocus(true)
                .setCancelCallback {

                    val content = window?.contentManager?.getContent(0)
                    if(content?.component is FoldSearchHistoryWindow) {
                        val updater = content.component as FoldSearchHistoryWindow
                        updater.update("Accessor", searchField.text.replace(";", ""))
                        startingString = ""
                    }
                    true
                }
                .setMinSize(Dimension(200, 75))
                .createPopup()
        }

        val selectedTextEditor = manager?.selectedTextEditor
        val psiFile = selectedTextEditor?.project?.let { PsiDocumentManager.getInstance(it).getPsiFile(selectedTextEditor?.document) }
        val exiledLines = ExileState.getInstance().getAllExiledLinesForFile(psiFile?.name ?: "")

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
        actionEvent.dataContext.let { foldSearchPopup?.showInBestPositionFor(it) }

        searchField.addDocumentListener(object :
            DocumentListener {
            override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {

                var searchTextField = searchField.text
                var ignoreCase = true

                if(searchTextField.contains(";")) {
                    foldSearchPopup?.cancel()
                    return
                }

                if(searchTextField.contains("`")) {
                    ignoreCase = false
                    searchTextField = searchTextField.replace("`", "")
                }
                val searchTerms = searchTextField.split("\n")


                // Find all lines containing a string
                var lines =
                    text
                        ?.mapIndexed { index: Int, s: String -> Pair(startingLine + index, s) }
                        ?.filter { indexedComments?.none { comment -> comment.first!! <= it.first && comment.second!! >= it.first }!! }
                        ?.filter { line: Pair<Int, String> -> searchTerms.all { line.second.contains(it, ignoreCase) } }

                lines = lines?.filter { line: Pair<Int, String> -> !exiledLines.any { line.second.contains(it, ignoreCase) } }

                // filter lines and only allow items on the left side or those that don't have a =
                lines = lines?.filter { line: Pair<Int, String> ->
                    searchTerms.all {
                        val lineText = line.second
                        val equalsIndex = lineText.indexOf("=")
                        var filter = false
                        if (equalsIndex == -1 && lineText.trim().contains(".") && !lineText.trim().substring(0, lineText.trim().indexOf(".")).contains(it, ignoreCase))
                            filter = true
                        else if(equalsIndex == -1)
                            filter = false
                        else {
                            val leftSide = lineText.substring(0, equalsIndex)
                            val rightSide = lineText.substring(equalsIndex + 1)
                            if(leftSide.contains(it, ignoreCase) && rightSide.contains(it, ignoreCase)) {
                                filter = true
                            } else if(!leftSide.contains(it, ignoreCase) && rightSide.contains(it, ignoreCase)) {
                                filter = true
                            }
                        }
                        filter
                    }
                }

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

                        if(currentOffset != 0)
                        {
                        FoldSearchUtil.createFoldSearchRegion(selectedTextEditor, currentOffset,
                            doc.getLineEndOffset(doc.lineCount - 1))
                        }
                    }
                }
                selectedTextEditor?.let { FoldSearchBehavior.exitFoldSearchField(it) }
            }
        })

        searchField.text = startingString
        searchField.setCaretPosition(startingString.length)
    }
}