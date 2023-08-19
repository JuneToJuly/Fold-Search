package thomas.gian

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.startOffset
import thomas.gian.exile.ExileState
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JPanel

class FoldSearch : FoldSearchBase() {
    var startingString: String = ""

    override fun actionPerformed(event: AnActionEvent) {
        val actionEvent = event
        val fileEditorManager = event.project?.let { FileEditorManager.getInstance(it) }
        val foldSearchField = event.project?.let { FoldSearchLook.createSearchField(it) }
        val topLevelPanel = JPanel(BorderLayout())
        if (foldSearchField == null) return

        topLevelPanel.add(foldSearchField, BorderLayout.CENTER)
        val foldSearchHistoryTool = fileEditorManager?.project?.let { ToolWindowManager.getInstance(it).getToolWindow("FoldSearchHistory") }

        val foldSearchTitle = if (FoldSearchState.getInstance().methodScoping) "Fold Search (Method)" else "Fold Search"
        foldSearchPopup = fileEditorManager?.selectedTextEditor?.let {
            JBPopupFactory.getInstance().createComponentPopupBuilder(topLevelPanel, foldSearchField)
                .setTitle(foldSearchTitle)
                .setMovable(true)
                .setAlpha(0.1f)
                .setResizable(true)
                .setFocusable(true)
                .setCancelCallback {
                    // On cancel update history
                    val content = foldSearchHistoryTool?.contentManager?.getContent(0)
                    if (content?.component is FoldSearchHistoryWindow) {
                        val foldSearchHistoryWindow = content.component as FoldSearchHistoryWindow
                        foldSearchHistoryWindow.update("Standard", foldSearchField.text.replace(";", ""))
                        startingString = ""
                    }
                    true
                }
                .setRequestFocus(true)
                .setMinSize(Dimension(200, 200))
                .createPopup()
        }
        val selectedEditor = fileEditorManager?.selectedTextEditor
        val selectedPsiFile = selectedEditor?.project?.let {
            PsiDocumentManager.getInstance(it).getPsiFile(selectedEditor.document)
        }
        val exiledLines = ExileState.getInstance().getAllExiledLinesForFile(selectedPsiFile?.name ?: "")

        val allSelectedDocumentLines = selectedEditor?.document?.text?.split("\n")
        var linesToProcess = allSelectedDocumentLines
        val indexedComments = selectedPsiFile?.let { EditorUtils.getComments(it) }
        var startingLine = 0

        // Need to limit processing to stay in method scope.
        if (FoldSearchState.getInstance().methodScoping) {
            val caret = selectedEditor?.caretModel
            val psiFile = selectedEditor?.project?.let {
                PsiDocumentManager.getInstance(it).getPsiFile(
                    selectedEditor.document
                )
            }
            val offset = AtomicInteger(-1)
            caret?.offset?.let { offset.compareAndSet(-1, it) }
            val findElementAt = offset.let { psiFile?.findElementAt(it.get()) }
            val method = PsiTreeUtil.getParentOfType(findElementAt, PsiMethod::class.java)
            startingLine = method?.startOffset?.let { fileEditorManager?.selectedTextEditor?.document?.getLineNumber(it) } ?: 0
            linesToProcess = actionEvent.project?.let { EditorUtils.getMethodTextAtCaret(it) }?.split("\n")
        }

        actionEvent.dataContext.let { foldSearchPopup?.showInBestPositionFor(it) }

        foldSearchField.addDocumentListener(object :
            DocumentListener {
            override fun documentChanged(event: com.intellij.openapi.editor.event.DocumentEvent) {
                val selectedTextEditor = fileEditorManager?.selectedTextEditor
                val searchText = foldSearchField.text
                var ignoreCase = true

                // Close out popup keybinding
                if (searchText.contains(";")) {
                    foldSearchPopup?.cancel()
                    return
                }

                // Split into ors
                val groups = searchText.split("~")

                // search for all lines containing each group
                val lines = groups.flatMap {
                    var searchText = it
                    if (it.contains("`")) {
                        ignoreCase = false
                        searchText = it.replace("`", "")
                    }

                    val searchTerms = searchText.split("\n")

                    // Find all lines containing the phrases
                    var lines = linesToProcess?.mapIndexed { lineIndex: Int, s: String -> Pair(startingLine + lineIndex, s) }
                        ?.filter { indexedComments?.none { comment -> comment.first!! <= it.first && comment.second!! >= it.first }!! }
                        ?.filter { line: Pair<Int, String> ->
                            searchTerms.all {
                                line.second.contains(
                                    it,
                                    ignoreCase
                                )
                            }
                        }

                    lines = lines?.filter { line: Pair<Int, String> ->
                        !exiledLines.any {
                            line.second.contains(
                                it,
                                ignoreCase
                            )
                        }
                    }
                    lines!!
                }.distinct().sortedWith(compareBy({ it.first }))

                // Check for case sensitivity
                var currentOffset = 0

                selectedTextEditor?.foldingModel?.runBatchFoldingOperation {
                    // remove all folds
                    selectedTextEditor.foldingModel.allFoldRegions.forEach {
                        if (!it.group.toString().contains("New")) // integration with the "New" plugin
                            selectedTextEditor.foldingModel.removeFoldRegion(it)
                        else
                            it.isExpanded = false
                    }

                    // Create folds for each line not containing the string
                    fileEditorManager.selectedTextEditor?.document?.let { doc ->
                        lines.forEach {
                            val lineIndex = it.first
                            val fullLine = it.second
                            val lineStart = doc.getLineStartOffset(lineIndex)
                            FoldSearchUtil.createFoldSearchRegion(selectedTextEditor, currentOffset, lineStart)
                            currentOffset = lineStart + fullLine.length + 1
                        }

                        // Create the final fold to fold the rest of the document
                        if (currentOffset > 0) {
                            FoldSearchUtil.createFoldSearchRegion(
                                selectedTextEditor,
                                currentOffset,
                                doc.getLineEndOffset(doc.lineCount - 1)
                            )
                        }
                    }
                }

                // Need to move the caret to the top of the document to force the editor to scroll to the top
                selectedTextEditor?.let { FoldSearchBehavior.exitFoldSearchField(it) }
            }
        })

        // Apply any starting string's for the current fold search
        foldSearchField.text = startingString
        foldSearchField.setCaretPosition(startingString.length)
    }
}