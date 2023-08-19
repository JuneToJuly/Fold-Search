package thomas.gian.exile

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.psi.PsiDocumentManager
import thomas.gian.FoldSearchToggle
import thomas.gian.FoldSearchUtil

class ExileClear : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        val exile = ExileState.getInstance()
        val manager = e.project?.let { FileEditorManager.getInstance(it) }
        val selectedTextEditor = manager?.selectedTextEditor
        val psiFile = selectedTextEditor?.project?.let { PsiDocumentManager.getInstance(it).getPsiFile(
            selectedTextEditor.document
        ) }

            psiFile?.name?.let {
                val filenameToExile = exile.currentExileForFile?.getOrDefault(it, exile.exileCount++)
                // get the current exile's passage key
                val exileToPassage = exile.exileList?.get(filenameToExile) ?: exile.passageCount++
                // get the current pass
                val currentPass = exile.exilePassages?.getOrDefault(exileToPassage, mutableListOf())
                // add the line text to pass
                currentPass?.clear()
                // add the pass as the current exile's passage key
                exile.exilePassages?.put(exileToPassage, currentPass!!)
            }

        val (action, event) = FoldSearchUtil.createAction("thomas.gian.FoldSearchToggle", e)
        if(action is FoldSearchToggle) {
            action.foldState = action.FOLDED
            action.actionPerformed(event)
        }
    }

}