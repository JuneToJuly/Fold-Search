package thomas.gian

import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.ActionPlaces
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.treeStructure.Tree
import java.awt.event.MouseEvent
import java.awt.event.MouseListener
import kotlin.streams.toList

class FoldSearchHistoryMouseListener(project: Project, panel: Tree, toolWindow: ToolWindow) : MouseListener {
    private val panel = panel
    private var dragStart : ArrayList<String> = ArrayList<String>()
    private var dragDropped = ""

    override fun mouseClicked(e: MouseEvent?) {
        // TODO change to control
        panel.lastSelectedPathComponent?.let {
            if (it is SortedMutableTreeNode) {
                val node = it.userObject
                if(e?.isAltDown == true) {
                    dragStart.add(node.toString())
                    return
                }
                if(it.parent is SortedMutableTreeNode) {
                    val parent = it.parent as SortedMutableTreeNode
                    var joined =  if (dragStart.size == 1) "${dragStart.get(0)}~" else ""
                    if(dragStart.size > 1)
                        dragStart.stream().map { it -> "$it~" }.forEach({ joined += it })
                    joined += node.toString()

                    when (parent.userObject.toString())
                    {
                        "Standard" -> {
                            val action = ActionManager.getInstance().getAction("thomas.gian.FoldSearch")
                            if(action is FoldSearch)
                            {
                                action.startingString = joined
                                dragStart.clear()
                            }
                            val dataContext = DataManager.getInstance().getDataContext(panel.parent)
                            val event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN, dataContext)
                            action.actionPerformed(event)
                            if(action is FoldSearch)
                            {
                                action.close()
                            }
                        }
                        "Method" -> {
                            val action = ActionManager.getInstance().getAction("thomas.gian.FoldSearchMethod")
                            if(action is FoldSearchMethod)
                            {
                                action.startingString = joined
                                dragStart.clear()
                            }
                            val dataContext = DataManager.getInstance().getDataContext(panel.parent)
                            val event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN, dataContext)
                            action.actionPerformed(event)
                            if(action is FoldSearchMethod)
                            {
                                action.close()
                            }

                        }
                        "Accessor" -> {
                            val action = ActionManager.getInstance().getAction("thomas.gian.FoldSearchAccessor")
                            if(action is FoldSearchAccessor)
                            {
                                action.startingString = joined
                                dragStart.clear()
                            }
                            val dataContext = DataManager.getInstance().getDataContext(panel.parent)
                            val event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN, dataContext)
                            action.actionPerformed(event)
                            if(action is FoldSearchAccessor)
                            {
                                action.close()
                            }

                        }
                        "Mutator" -> {
                            val action = ActionManager.getInstance().getAction("thomas.gian.FoldSearchMutator")
                            if(action is FoldSearchMutator)
                            {
                                action.startingString = joined
                                dragStart.clear()
                            }
                            val dataContext = DataManager.getInstance().getDataContext(panel.parent)
                            val event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN, dataContext)
                            action.actionPerformed(event)
                            if(action is FoldSearchMutator)
                            {
                                action.close()
                            }
                        }
                        "Window" -> {
                            val action = ActionManager.getInstance().getAction("thomas.gian.WindowSearch")
                            if(action is WindowSearch)
                            {
                                action.startingString = joined
                                dragStart.clear()
                            }
                            val dataContext = DataManager.getInstance().getDataContext(panel.parent)
                            val event = AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN, dataContext)
                            action.actionPerformed(event)
                            if(action is WindowSearch)
                            {
                                action.close()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun mousePressed(e: MouseEvent?) {
        return
    }

    override fun mouseReleased(e: MouseEvent?) {
        return
    }

    override fun mouseEntered(e: MouseEvent?) {
        return
    }

    override fun mouseExited(e: MouseEvent?) {
        return
    }

}
