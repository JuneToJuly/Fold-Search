package thomas.gian

import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.FoldRegion
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.editor.impl.FoldingModelImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

class FoldSearchUtil {
    data class ActionAndEvent(val action: AnAction, val event: AnActionEvent)
    companion object {
        val instance = FoldSearchUtil()
        fun createFoldSearchAction(type: String, search: String, event: AnActionEvent) : ActionAndEvent {
            return createFoldSearchAction(type, search, event.dataContext)
        }
        fun createFoldSearchAction(type: String, search: String, data: DataContext) : ActionAndEvent {
            // create a new action
            // add it to the toolbar
            // add it to the history
            val action = when (type)
            {
                "Standard" -> ActionManager.getInstance().getAction("thomas.gian.FoldSearch")
                "Method" -> ActionManager.getInstance().getAction("thomas.gian.FoldSearchMethod")
                "Accessor" -> ActionManager.getInstance().getAction("thomas.gian.FoldSearchAccessor")
                "Mutator" -> ActionManager.getInstance().getAction("thomas.gian.FoldSearchMutator")
                "Window" -> ActionManager.getInstance().getAction("thomas.gian.WindowSearch")
                "Block" -> ActionManager.getInstance().getAction("thomas.gian.BlockSearch")
                else -> ActionManager.getInstance().getAction("thomas.gian.FoldSearch")
            }
            when (action)
            {
                is FoldSearch -> {action.startingString = search}
                is FoldSearchMethod -> action.startingString = search
                is FoldSearchAccessor -> action.startingString = search
                is FoldSearchMutator -> action.startingString = search
                is WindowSearch -> action.startingString = search
                is BlockSearch -> action.startingString = search
            }

            val event = AnActionEvent.createFromAnAction(action as AnAction, null, ActionPlaces.UNKNOWN, data)
            return ActionAndEvent(action,event)
        }

        fun createAction(actionId: String, event: AnActionEvent) : ActionAndEvent {
            return createAction(actionId, event.dataContext)
        }

        fun createAction(actionId: String, data: DataContext) : ActionAndEvent {
            val action = ActionManager.getInstance().getAction(actionId)
            val event = AnActionEvent.createFromAnAction(action as AnAction, null, ActionPlaces.UNKNOWN, data)
            return ActionAndEvent(action,event)

        }

        fun createFoldSearchRegion(selectedTextEditor: Editor, startOffset: Int, endOffset: Int): FoldRegion? {
            val group: FoldingGroup = FoldingGroup.newGroup("Fold Search")

            var region : FoldRegion? = null
            if(selectedTextEditor.foldingModel is FoldingModelImpl) {
                val foldModel = selectedTextEditor.foldingModel as FoldingModelImpl
                var region =
                    foldModel.createFoldRegion(
                        startOffset,
                        endOffset,
                        "", group, false
                    )
                region?.isExpanded = false
            }
            return region
        }

        fun getLastFoldSearch(project: Project): Pair<String, String> {
            val manager = project.let { FileEditorManager.getInstance(it) }
            val window = manager?.project?.let {
                ToolWindowManager.getInstance(it).getToolWindow("FoldSearchHistory")
            }
            val content = window?.contentManager?.getContent(0)
            val previous = when (content?.component) {
                is FoldSearchHistoryWindow -> {
                    val history = content.component as FoldSearchHistoryWindow
                    if (history.backStack.size < 1) Pair("", "")
                    history.backStack.peek()
                }

                else -> Pair("", "")
            }
            return previous
        }

    }
}