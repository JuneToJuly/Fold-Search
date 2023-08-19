package thomas.gian

import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.ToolWindowManager

class FoldSearchFrameAsWindow : FoldSearchBase() {
    var startingString : String = ""

    override fun actionPerformed(event: AnActionEvent) {

        val lastFoldSearch = event.project?.let { FoldSearchUtil.getLastFoldSearch(it) }
        val (action, event) =
            FoldSearchUtil.createFoldSearchAction("Window", lastFoldSearch?.second ?: "", event)
        action.actionPerformed(event)
        when (action) {
            is FoldSearchBase -> action.close()
        }
    }
}