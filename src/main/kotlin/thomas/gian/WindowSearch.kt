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
import com.intellij.refactoring.suggested.startOffset
import com.intellij.ui.LanguageTextField
import com.intellij.ui.LanguageTextField.SimpleDocumentCreator
import thomas.gian.exile.ExileState
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JPanel

class WindowSearch : FoldSearchBase(){

    var startingString : String = ""
    override fun actionPerformed(actionEvent: AnActionEvent) {
        val manager = actionEvent.project?.let { FileEditorManager.getInstance(it) }
        val project = actionEvent.project
        var chunkSize = 5

        val topPanel = JPanel(BorderLayout())
        val searchField = LanguageTextField(Language.findLanguageByID("Java"), project, "", SimpleDocumentCreator(), false)
        topPanel.add(searchField, BorderLayout.CENTER)

        // --EXILE-- Search field
        searchField.setSize(75, 300)
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
                .setTitle("Window Search (5)")
                .setShowShadow(true)
                .setMovable(true)
                .setResizable(true)
                .setAlpha(0.1f)
                .setCancelCallback {
                    val content = window?.contentManager?.getContent(0)
                    if(content?.component is FoldSearchHistoryWindow) {
                        val updater = content.component as FoldSearchHistoryWindow
                        updater.update("Window", searchField.text.replace(";", ""))
                        startingString = ""
                    }
                    true
                }
                .setFocusable(true)
                .setRequestFocus(true)
                .setMinSize(Dimension(75, 400))
                .createPopup()
        }

        actionEvent.dataContext.let { foldSearchPopup?.showInBestPositionFor(it) }

        searchField.addDocumentListener(object :
            DocumentListener {
            override fun documentChanged(event: DocumentEvent) {
                val selectedTextEditor = manager?.selectedTextEditor

                var searchText = searchField.text
                var ignoreCase = true

                // --Exile-- Cancel popup
                if(searchText.contains(";")) {
                    foldSearchPopup?.cancel()
                    return
                }

                // --Exile-- Case sensitive
                if(searchText.contains("`")) {
                    ignoreCase = false
                    searchText = searchText.replace("`", "")
                }

                var searchTerms = searchText.trim().split("\n")
                val count = searchTerms.stream().filter { it.contains('~', ignoreCase) }.count()
                if(count > 0) {
                    chunkSize = (count.toInt() + 1) * 5
                }
                searchTerms = searchTerms.filter { !it.contains('~', ignoreCase) }

                val psiFile = selectedTextEditor?.project?.let { PsiDocumentManager.getInstance(it).getPsiFile(
                    selectedTextEditor.document
                ) }
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

                var lines =
                    text
                        ?.asSequence()
                        ?.mapIndexed { index: Int, s: String -> Pair(startingLine + index, s) }
                        ?.filter { indexedComments?.none { comment -> comment.first!! <= it.first && comment.second!! >= it.first }!! }
                        ?.filter { line: Pair<Int, String> -> !exiledLines.any { line.second.contains(it, ignoreCase) } }
                        ?.filter { it.second != ""}
                        ?.windowed(chunkSize, 1)
                        ?.filter { line: List<Pair<Int, String>> ->
                            searchTerms.all { term: String ->
                                line.any { lineIndex: Pair<Int,String> ->
                                    lineIndex.second.contains(term, ignoreCase) } } }
                        ?.toList()

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
                        indexedComments?.forEach {
                            val chunkStartIndex = it.first
                            val chunkEndIndex = it.second
                            if(chunkStartIndex != null && chunkEndIndex != null) {
                                val chunkStartOffset = doc.getLineStartOffset(chunkStartIndex)
                                val chunkEndOffset = doc.getLineEndOffset(chunkEndIndex)
                                FoldSearchUtil.createFoldSearchRegion(
                                    selectedTextEditor,
                                    chunkStartOffset,
                                    chunkEndOffset + 1
                                )
                            }
                        }
                    }

                    ExileState.getInstance().getPassageForFile(psiFile?.name ?: "")
                    manager.selectedTextEditor?.document?.let { doc ->
                        var exiledLines = selectedTextEditor.document.text.split("\n")
                            .mapIndexed { index: Int, s: String -> Pair(index, s) }
                            .filter { line: Pair<Int, String> -> !exiledLines.any { line.second.contains(it, ignoreCase) } }.map { line -> line.first }.toSet()
                        var allLines = selectedTextEditor.document.text.split("\n").toList()
                            .mapIndexed { index: Int, s: String -> Pair(index, s) }.map{ line -> line.first}.toSet()

                        var diff = allLines subtract exiledLines
                        diff.forEach { exiledLine ->
                            val chunkStartIndex = exiledLine
                            val chunkEndIndex =  exiledLine
                            val chunkStartOffset = doc.getLineStartOffset(chunkStartIndex)
                            val chunkEndOffset = doc.getLineEndOffset(chunkEndIndex)
                                FoldSearchUtil.createFoldSearchRegion(
                                    selectedTextEditor,
                                    chunkStartOffset,
                                    chunkEndOffset + 1,
                                )
                        }
                    }

                    manager.selectedTextEditor?.document?.let { doc ->
                        lines?.filter { searchTerms.any { term: String-> it.first().second.contains(term, ignoreCase) } }?.forEach {
                            val chunkStartIndex = it.first().first
                            val chunkEndIndex = it.last().first
                            val chunkStartOffset = doc.getLineStartOffset(chunkStartIndex)
                            val chunkEndOffset = doc.getLineEndOffset(chunkEndIndex)
                            FoldSearchUtil.createFoldSearchRegion(selectedTextEditor, currentOffset, chunkStartOffset)

                            currentOffset = chunkEndOffset + 1
                        }
                        if(currentOffset > 0) {
                            FoldSearchUtil.createFoldSearchRegion(selectedTextEditor, currentOffset, doc.getLineEndOffset(doc.lineCount - 1))
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