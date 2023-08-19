package thomas.gian

import com.intellij.lang.Language
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.JavaRecursiveElementVisitor
import com.intellij.psi.PsiBlockStatement
import com.intellij.psi.PsiCodeBlock
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset
import com.intellij.testFramework.statement
import com.intellij.ui.LanguageTextField
import com.intellij.ui.LanguageTextField.SimpleDocumentCreator
import thomas.gian.exile.ExileState
import java.awt.BorderLayout
import java.awt.Dimension
import java.util.concurrent.atomic.AtomicInteger
import javax.swing.JPanel
import kotlin.streams.toList

class BlockSearch : FoldSearchBase() {

    var startingString: String = ""

    override fun actionPerformed(event: AnActionEvent) {
        val manager = event.project?.let { FileEditorManager.getInstance(it) }
        val project = event.project

        val topPanel = JPanel(BorderLayout())
        val searchField =
            LanguageTextField(Language.findLanguageByID("Java"), project, "", SimpleDocumentCreator(), false)
        topPanel.add(searchField, BorderLayout.CENTER)

        searchField.setSize(75, 300)
        searchField.font = searchField.font.deriveFont(20f)
        searchField.isViewer = false
        searchField.isFocusable = true
        searchField.isEnabled = true
        searchField.isRequestFocusEnabled = true
        searchField.isFocusCycleRoot = true
        searchField.setShowPlaceholderWhenFocused(true)
        searchField.setCaretPosition(0)

        var window =
            manager?.project?.let { ToolWindowManager.getInstance(it).getToolWindow("FoldSearchHistory") }

        val title = if(FoldSearchState.getInstance().methodScoping) "Block Search (Method)" else "Block Search"
        foldSearchPopup = manager?.selectedTextEditor?.let {
            JBPopupFactory.getInstance().createComponentPopupBuilder(topPanel, searchField)
                .setTitle(title)
                .setShowShadow(true)
                .setMovable(true)
                .setResizable(true)
                .setAlpha(0.1f)
                .setCancelCallback {
                    val content = window?.contentManager?.getContent(0)
                    if (content?.component is FoldSearchHistoryWindow) {
                        val updater = content.component as FoldSearchHistoryWindow
                        updater.update("Block", searchField.text.replace(";", ""))
                        startingString = ""
                    }
                    true
                }
                .setFocusable(true)
                .setRequestFocus(true)
                .setMinSize(Dimension(75, 400))
                .createPopup()
        }
        val selectedTextEditor = manager?.selectedTextEditor

        var method: PsiMethod? = null
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
            method =
                PsiTreeUtil.getParentOfType(findElementAt, PsiMethod::class.java)
        }
        event.dataContext.let { foldSearchPopup?.showInBestPositionFor(it) }

        searchField.addDocumentListener(object :
            DocumentListener {
            override fun documentChanged(event: DocumentEvent) {

                var searchText = searchField.text
                var ignoreCase = true

                if (searchText.contains(";")) {
                    foldSearchPopup?.cancel()
                    return
                }

                if (searchText.contains("`")) {
                    ignoreCase = false
                    searchText = searchText.replace("`", "")
                }

                var searchTerms = searchText.trim().split("\n")
                if (searchTerms.size == 1 && searchTerms[0].isBlank()) {
                    return
                }
                val count = searchTerms.stream().filter { it.contains('~', ignoreCase) }.count()

                searchTerms = searchTerms.filter { !it.contains('~', ignoreCase) }

                val psiFile = selectedTextEditor?.project?.let {
                    PsiDocumentManager.getInstance(it).getPsiFile(
                        selectedTextEditor.document
                    )
                }
                val exiledLines = ExileState.getInstance().getAllExiledLinesForFile(psiFile?.name ?: "")

                //create a list of blocks
                val blocks = mutableListOf<PsiCodeBlock>()
                if (psiFile != null) {
                    if (FoldSearchState.getInstance().methodScoping) {
                        method?.accept(object : JavaRecursiveElementVisitor() {
                            override fun visitCodeBlock(block: PsiCodeBlock?) {
                                super.visitCodeBlock(block)
                                if(block?.parent !is PsiMethod) {
                                    blocks.add(block!!)
                                }
                            }
                        })
                    } else {
                        psiFile.accept(object : JavaRecursiveElementVisitor() {
                            override fun visitCodeBlock(block: PsiCodeBlock?) {
                                super.visitCodeBlock(block)
                                if(block?.parent !is PsiMethod) {
                                    blocks.add(block!!)
                                }
                            }
                        })
                    }
                }

                var lines = blocks.stream().filter { block ->
                    searchTerms.all { term: String ->
                        block.text.contains(term, ignoreCase)
                    }
                }.toList().toMutableList()

                var filter = mutableListOf<PsiCodeBlock>()
                if (lines.size > 0) {
//                    filter = lines.filter { block ->
//                        val blockStart = block.startOffset
//                        val blockEnd = block.endOffset
//                        lines.filter { it != block }.any { otherBlock ->
//                            val otherBlockStart = otherBlock.startOffset
//                            val otherBlockEnd = otherBlock.endOffset
//                            otherBlockStart > blockStart && otherBlockEnd < blockEnd
//                        }
//                    }.toMutableList()
                    filter = lines.filter { block ->
                        searchTerms.all { term: String ->
                            block.statements
                                .filter { statement -> !(statement.text.contains("{") || statement.text.contains("}")) }
                                .filter { statement -> !exiledLines.map { it.trim() }.contains(statement.text)}
                                .any { filteredStatement ->
                                    filteredStatement.text.contains(term, ignoreCase)
                                }
                        }
                    }.toMutableList()
                }

//                lines.removeAll(filter)
                lines = filter.sortedWith { value1: PsiCodeBlock, value2: PsiCodeBlock -> value1.startOffset - value2.startOffset}.toMutableList()

                    filter = lines.filter { block ->
                        val blockStart = block.startOffset
                        val blockEnd = block.endOffset
                        lines.filter { it != block }.any { otherBlock ->
                            val otherBlockStart = otherBlock.startOffset
                            val otherBlockEnd = otherBlock.endOffset
                            otherBlockStart < blockStart && otherBlockEnd > blockEnd
                        }
                    }.toMutableList()
                lines.removeAll(filter)
//                lines = lines.subList(0,lines.size)
                var currentOffset = 0
                selectedTextEditor?.foldingModel?.runBatchFoldingOperation {
                    // remove all folds
                    selectedTextEditor.foldingModel.allFoldRegions.forEach {
                        if (!it.group.toString().contains("New"))
                            selectedTextEditor.foldingModel.removeFoldRegion(it)
                        else
                            it.isExpanded = false
                    }

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
                                chunkEndOffset + 1
                            )
                        }
                    }

                    manager.selectedTextEditor?.document?.let { doc ->
                        lines.forEach {
                            val chunkStartOffset = it.lBrace?.textOffset ?: 0
                            val chunkStartLine = doc.getLineNumber(chunkStartOffset)
                            val chunkStartLineOffset = doc.getLineStartOffset(chunkStartLine - 1)-1
                            val chunkEndOffset = it.rBrace?.textOffset ?: 0
                            FoldSearchUtil.createFoldSearchRegion(
                                selectedTextEditor,
                                currentOffset,
                                chunkStartLineOffset
                            )
                            currentOffset = chunkEndOffset + 1
                        }

                        if (currentOffset != 0) {
                            FoldSearchUtil.createFoldSearchRegion(
                                selectedTextEditor, currentOffset,
                                doc.getLineEndOffset(doc.lineCount - 1)
                            )
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