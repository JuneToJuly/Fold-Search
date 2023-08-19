package thomas.gian

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.ToolWindowManager

class FoldSearchPrevious : FoldSearchBase() {
    var startingString : String = ""

    override fun actionPerformed(event: AnActionEvent) {
        val manager = event.project?.let { FileEditorManager.getInstance(it) }
        var window = manager?.project?.let { ToolWindowManager.getInstance(it).getToolWindow("FoldSearchHistory") }

        val content = window?.contentManager?.getContent(0)
        if(content?.component is FoldSearchHistoryWindow) {
            val history = content.component as FoldSearchHistoryWindow
            if(history.backStack.size < 2) return
            history.forwardStack.push(history.backStack.pop())
            val prevSearch = history.backStack.pop()
            startingString = prevSearch?.second ?: ""
            val (action, event) =
                FoldSearchUtil.createFoldSearchAction(prevSearch?.first ?: "", prevSearch?.second ?: "", event)
            action.actionPerformed(event)
            when (action)
            {
                is FoldSearchBase -> action.close()
            }
        }
    }
}