package thomas.gian

import com.intellij.openapi.actionSystem.AnActionEvent

class FoldSearchFrameAsAccessor : FoldSearchBase() {
    var startingString : String = ""

    override fun actionPerformed(event: AnActionEvent) {

        val lastFoldSearch = event.project?.let { FoldSearchUtil.getLastFoldSearch(it) }
        val (action, event) =
            FoldSearchUtil.createFoldSearchAction("Accessor", lastFoldSearch?.second ?: "", event)
        action.actionPerformed(event)
        when (action) {
            is FoldSearchBase -> action.close()
        }
    }
}