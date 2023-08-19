package thomas.gian

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.treeStructure.Tree
import java.awt.event.KeyEvent
import java.awt.event.KeyListener
import javax.swing.tree.DefaultTreeModel

class FoldSearchHistoryKeyListener(project: Project, panel: Tree, toolWindow: ToolWindow) :
    KeyListener {
    private val panel = panel

    override fun keyTyped(e: KeyEvent?) {
        if (e?.keyChar == 'c') {
            if(panel.model?.root is SortedMutableTreeNode)
            {
                val root = panel.model?.root as SortedMutableTreeNode
                root.children().toList().forEach {
                    (it as SortedMutableTreeNode).removeAllChildren()
                }
                (panel.model as DefaultTreeModel).reload(root)
            }
        }
    }

    override fun keyPressed(e: KeyEvent?) {
    }

    override fun keyReleased(e: KeyEvent?) {
    }

}
