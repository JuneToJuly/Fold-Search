package thomas.gian

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiDocumentManager

class FoldSearchOrAtCursor : FoldSearchBase() {
    var actionEvent : AnActionEvent? = null
    var startingString : String = ""

    override fun actionPerformed(event: AnActionEvent) {
        val manager = event.project?.let { FileEditorManager.getInstance(it) }
        var window = manager?.project?.let { ToolWindowManager.getInstance(it).getToolWindow("FoldSearchHistory") }
        val project = event.project

        val psiDocumentManager = project?.let { PsiDocumentManager.getInstance(it) }
        val psiFile = manager?.selectedTextEditor?.document?.let { psiDocumentManager?.getPsiFile(it) }
        val cursorText = manager?.selectedTextEditor?.caretModel?.currentCaret?.offset?.let { psiFile?.findElementAt(it) }?.text ?: ""

        val content = window?.contentManager?.getContent(0)
        if(content?.component is FoldSearchHistoryWindow) {
            val history = content.component as FoldSearchHistoryWindow
            if(history.backStack.size < 1) return
            val prevSearch = history.backStack.peek()
            startingString = prevSearch?.second ?: ""
            val newStringWithOr = prevSearch?.second + "~" + cursorText
            val searchType = when (prevSearch?.first) {
                "Accessor" -> "Standard"
                "Mutator" -> "Standard"
                else -> prevSearch?.first ?: "Standard"
            }
            val (action, event) =
                FoldSearchUtil.createFoldSearchAction(searchType, newStringWithOr, event)
            action.actionPerformed(event)
            when (action)
            {
                is FoldSearchBase -> action.close()
            }
        }
    }
}