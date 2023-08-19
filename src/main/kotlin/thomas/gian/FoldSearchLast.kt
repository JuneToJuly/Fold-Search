package thomas.gian

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.ToolWindowManager

class FoldSearchLast : FoldSearchBase() {
    var startingString : String = ""

    override fun actionPerformed(event: AnActionEvent) {

        val lastFoldSearch = event.project?.let { FoldSearchUtil.getLastFoldSearch(it) }
        val (action, event) = FoldSearchUtil.createFoldSearchAction(lastFoldSearch?.first ?: "Standard", lastFoldSearch?.second ?: "", event)
        action.actionPerformed(event)
    }
}