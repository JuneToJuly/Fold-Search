package thomas.gian

import com.intellij.openapi.actionSystem.AnActionEvent

class FoldSearchToggleCaseSensitive : FoldSearchBase() {
    var startingString : String = ""

    override fun actionPerformed(event: AnActionEvent) {

        val lastFoldSearch = event.project?.let { FoldSearchUtil.getLastFoldSearch(it) }
        var search = lastFoldSearch?.second + "" ?: ""
            search = if (search.contains("`")) search.replace("`", "") else search + "`"
        val (action, event) = FoldSearchUtil.createFoldSearchAction(lastFoldSearch?.first ?: "Standard", search, event)
        action.actionPerformed(event)
        when (action) {
            is FoldSearchBase -> action.close()
        }
    }
}