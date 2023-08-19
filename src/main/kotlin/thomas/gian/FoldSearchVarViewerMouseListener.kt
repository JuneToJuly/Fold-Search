package thomas.gian

import com.intellij.ide.DataManager
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.VisualPosition
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.treeStructure.Tree
import java.awt.event.MouseEvent
import java.awt.event.MouseListener

class FoldSearchVarViewerMouseListener(val project: Project, panel: Tree, toolWindow: ToolWindow) : MouseListener {
    private val panel = panel
    
    private var dragStart : ArrayList<String> = ArrayList<String>()
    private var dragDropped = ""

    override fun mouseClicked(e: MouseEvent?) {
        panel.lastSelectedPathComponent?.let {
            if (it is SortedMutableTreeNode) {
                val node = it.userObject
                if(it.parent is SortedMutableTreeNode) {
                    val search = node.toString()
                    val dataContext = DataManager.getInstance().getDataContext(panel.parent)
                    val (action, event) = FoldSearchUtil.createFoldSearchAction("Standard", search, dataContext)

                    if(!FoldSearchState.getInstance().methodScoping) {
                        val (action1, event1) = FoldSearchUtil.createAction(
                            "thomas.gian.FoldSearchToggleMethodScoping",
                            dataContext
                        )
                        action1.actionPerformed(event1)
                        action.actionPerformed(event)
                        when (action) {
                            is FoldSearchBase -> action.close()
                        }
                        action1.actionPerformed(event1)
                    }
                    else {
                        action.actionPerformed(event)
                        when (action) {
                            is FoldSearchBase -> action.close()
                        }
                    }

                    val (focus, focusEvent) = FoldSearchUtil.createAction("EditorEscape", dataContext)
                    focus.actionPerformed(focusEvent)
                    val selectedEditor = project?.let { FileEditorManager.getInstance(it).selectedTextEditor }
                    selectedEditor?.caretModel?.moveToVisualPosition(VisualPosition(0, 0))
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
