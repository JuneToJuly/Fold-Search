package thomas.gian

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.psi.PsiDocumentManager
import thomas.gian.exile.ExileState

class FoldSearchToggle : AnAction() {
    val NO_FOLDS = 0
    val FOLDED = 1
    val FOLDED_PARTIAL = 2
    var foldState = NO_FOLDS

    override fun actionPerformed(e: AnActionEvent) {
        val manager = e.project?.let { IntelliJManagers.getFileEditorManager(it) }
        val window = e.project?.let { IntelliJUtils.getToolWindow(it, "FoldSearchHistory") }
        val selectedTextEditor = manager?.selectedTextEditor

        selectedTextEditor?.foldingModel?.runBatchFoldingOperation {
            // find if there are any folds expanded, this indicated that the search is no longer held
            selectedTextEditor.foldingModel.allFoldRegions.forEach {
                if (it.group.toString().contains("Fold Search")) // integration with the "New" plugin
                    foldState = FOLDED
                else if(!it.group.toString().contains("New"))
                {
                    selectedTextEditor.foldingModel.removeFoldRegion(it)
                }
            }
        }

        val content = window?.contentManager?.getContent(0)
        if (content?.component is FoldSearchHistoryWindow) {
            val updater = content.component as FoldSearchHistoryWindow
            if (foldState == FOLDED_PARTIAL || foldState == NO_FOLDS) {
                if (updater.backStack.size > 0) {
                    val lastSearch = updater.backStack.pop()
                    if (lastSearch != null) {
                        val (action, event) =
                            FoldSearchUtil.createFoldSearchAction(lastSearch.first, lastSearch.second, e)
                        action.actionPerformed(event)
                        foldState = FOLDED
                        when (action) {
                            is FoldSearchBase -> action.close()
                        }
                        return
                    }
                }
            }
        }
        val psiFile = selectedTextEditor?.project?.let {
            PsiDocumentManager.getInstance(it).getPsiFile(selectedTextEditor.document)
        }
        val currentPassages = psiFile?.let { ExileState.getInstance().getPassageForFile(it.name) }

        // find the first line of the passage[0]
        val lines = currentPassages?.map {
            var passageLines = it.split("\n")
            passageLines = if(passageLines.first().isEmpty()) passageLines.subList(1, passageLines.size) else passageLines
            passageLines = if(passageLines.last().isEmpty()) passageLines.subList(0, passageLines.size - 1) else passageLines
            val lines =
                selectedTextEditor.document.text.split("\n")
                    .mapIndexed { index: Int, s: String -> Pair(index, s) }
                    .windowed(passageLines.size)
                    .filter { window -> window.sortedWith { item: Pair<Int,String>, item2: Pair<Int,String> -> item.first - item2.first }[0].second == passageLines.first() }
                    .filter { window: List<Pair<Int, String>> ->
                        passageLines.all { passageLine: String ->
                            window.any { lineInWindow: Pair<Int,String> ->
                                lineInWindow.second.contains(passageLine, false) } } }
            lines
        }

        selectedTextEditor?.foldingModel?.runBatchFoldingOperation {
            // --Exile-- integration with the "New" plugin
            selectedTextEditor.foldingModel.allFoldRegions.forEach {
                if (!it.group.toString().contains("New"))
                    selectedTextEditor.foldingModel.removeFoldRegion(it)
                else
                    it.isExpanded = false
            }

            lines?.forEach {
                it.forEach { passage ->
                    val firstLine = passage.first().first
                    val lastLine = passage.last().first
                    val firstLineOffset = selectedTextEditor.document.getLineStartOffset(firstLine)
                    val lastLineOffset = selectedTextEditor.document.getLineEndOffset(lastLine)
                    val region =
                    selectedTextEditor.foldingModel.addFoldRegion(firstLineOffset, lastLineOffset + 1, "")
                region?.isExpanded = false
                }
            }
        }
        foldState = NO_FOLDS
    }
}