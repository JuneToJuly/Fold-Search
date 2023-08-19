package thomas.gian.exile

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import thomas.gian.FoldSearchToggle
import thomas.gian.FoldSearchUtil

class ExileAdd : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val exile = ExileState.getInstance()
        val manager = e.project?.let { FileEditorManager.getInstance(it) }
        val selectedTextEditor = manager?.selectedTextEditor
        val psiFile = selectedTextEditor?.project?.let { PsiDocumentManager.getInstance(it).getPsiFile(
            selectedTextEditor.document
        ) }

        var currentOffset = manager?.selectedTextEditor?.caretModel?.offset
        if(manager?.selectedTextEditor?.selectionModel?.hasSelection()!!) {
            val selectionStart = manager.selectedTextEditor?.selectionModel?.selectionStart
            var startLine = selectionStart?.let { manager.selectedTextEditor?.document?.getLineNumber(it)}
            val startLineOffset = startLine?.let { manager.selectedTextEditor?.document?.getLineStartOffset(it)}

            val selectionEnd = manager.selectedTextEditor?.selectionModel?.selectionEnd
            var endLine = selectionEnd?.let { manager.selectedTextEditor?.document?.getLineNumber(it.minus(1))}
            val endLineOffset = endLine?.let { manager.selectedTextEditor?.document?.getLineEndOffset(it)}
            val totalText = startLineOffset?.let { endLineOffset?.let { it1 -> manager.selectedTextEditor?.document?.getText(
                TextRange(it, it1)
            ) } }

            psiFile?.name?.let {
                // get the current exile for the file
                val filenameToExile = exile.currentExileForFile?.getOrDefault(it, exile.exileCount++)
                // get the current exile's passage key
                val exileToPassage = exile.exileList?.get(filenameToExile) ?: exile.passageCount++
                // get the current pass
                val currentPass = exile.exilePassages?.getOrDefault(exileToPassage, ArrayList<String>())
                // add the line text to pass
                currentPass?.add(totalText ?: "")
                // add the pass as the current exile's passage key
                exile.exilePassages?.put(exileToPassage, currentPass!!)
                filenameToExile?.let { it1 -> exile.exileList?.put(it1,  exileToPassage) }
                filenameToExile?.let { it1 -> exile.currentExileForFile?.put(it, it1) }
            }
        }
        else
        {
            val currentLine = currentOffset?.let { manager.selectedTextEditor?.document?.getLineNumber(currentOffset)}
            val currentLineStartOffset = currentLine?.let { manager.selectedTextEditor?.document?.getLineStartOffset(currentLine)}
            val currentLineEndOffset = currentLine?.let { manager.selectedTextEditor?.document?.getLineEndOffset(currentLine)}
            val currentLineText = currentLineStartOffset?.let { currentLineEndOffset?.let { it1 -> manager.selectedTextEditor?.document?.getText(
                TextRange(it, it1 + 1)
            ) } }

            psiFile?.name?.let {
                // get the current exile for the file
                val filenameToExile = exile.currentExileForFile?.getOrDefault(it, exile.exileCount++)
                // get the current exile's passage key
                val exileToPassage = exile.exileList?.get(filenameToExile) ?: exile.passageCount++
                // get the current pass
                val currentPass = exile.exilePassages?.getOrDefault(exileToPassage, ArrayList())
                // add the line text to pass
                currentPass?.add(currentLineText ?: "")
                // add the pass as the current exile's passage key
                exile.exilePassages?.put(exileToPassage, currentPass!!)
                filenameToExile?.let { it1 -> exile.exileList?.put(it1,  exileToPassage) }
                filenameToExile?.let { it1 -> exile.currentExileForFile?.put(it, it1) }
            }

        }

        val (action, event) = FoldSearchUtil.createAction("thomas.gian.FoldSearchToggle", e)
        if(action is FoldSearchToggle) {
            action.foldState = action.FOLDED
            action.actionPerformed(event)
        }
    }

}