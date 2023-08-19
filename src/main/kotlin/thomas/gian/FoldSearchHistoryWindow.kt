package thomas.gian

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.components.JBList
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Dimension
import java.util.*
import javax.swing.JPanel
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.DefaultTreeModel
import javax.swing.tree.TreePath


class FoldSearchHistoryWindow(val project: Project, val toolWindow: ToolWindow) : JPanel() {

    private var historyBySearchTypePanel: Tree? = null
    private var historyList: JBList<String> = JBList()
    private var outerPanel: JPanel = JPanel(BorderLayout())
    private var foldTypes: MutableMap<String, SortedMutableTreeNode>? = null
    private var root: SortedMutableTreeNode? = null
    var backStack : Stack<Pair<String, String>> = Stack<Pair<String,String>>()
    var forwardStack : Stack<Pair<String, String>> = Stack<Pair<String,String>>()

    fun getContent(): JPanel{
        val manager = FileEditorManager.getInstance(project)
        root = SortedMutableTreeNode("Fold History")
        historyBySearchTypePanel = Tree(DefaultTreeModel(root))
        historyBySearchTypePanel?.preferredSize = Dimension(200, 800)

        // create a cell renderer for the tree
        val renderer = object : ColoredTreeCellRenderer() {
            override fun customizeCellRenderer(
                tree: JTree,
                value: Any?,
                selected: Boolean,
                expanded: Boolean,
                leaf: Boolean,
                row: Int,
                hasFocus: Boolean
            ) {
                if (value is DefaultMutableTreeNode) {
                    val userObject = value.userObject
                    if (value.childCount > 0) {
                        // get color scheme for current editor

                        foreground = Color.decode("#A064B5")

                        append(value.toString())
                    } else {
                        userObject as String
                        append(userObject)
                    }
                }
            }
        }

        historyBySearchTypePanel?.cellRenderer = renderer
        historyBySearchTypePanel?.addMouseListener(FoldSearchHistoryMouseListener(project, historyBySearchTypePanel!!, toolWindow))
        historyList.preferredSize = Dimension(200,800)
        historyList?.addMouseListener(FoldSearchHistoryMouseListener(project, historyBySearchTypePanel!!, toolWindow))


        outerPanel.add(historyBySearchTypePanel, BorderLayout.WEST)
        outerPanel.add(historyList, BorderLayout.CENTER)

        // fill outerPanel to
        this.add(outerPanel, BorderLayout.WEST)
        foldTypes = mutableMapOf()
        root?.add(SortedMutableTreeNode("Standard"))
        root?.add(SortedMutableTreeNode("Method"))
        root?.add(SortedMutableTreeNode("Accessor"))
        root?.add(SortedMutableTreeNode("Mutator"))
        root?.add(SortedMutableTreeNode("Window"))
        historyBySearchTypePanel?.addKeyListener(FoldSearchHistoryKeyListener(project, historyBySearchTypePanel!!, toolWindow))

        // set height to fill window
        historyList.size = this.size
        historyBySearchTypePanel!!.size = this.size
        return this
    }

    fun update(type: String, searchPhrase: String) {

        if(searchPhrase.isEmpty()) return

        backStack.push(Pair(type, searchPhrase))
        historyList.setListData(backStack.map { it.second }.toTypedArray().reversedArray())

        SwingUtilities.invokeLater {
            root?.children()?.toList()?.find { it.toString() == type }?.let {
                val node = it as DefaultMutableTreeNode
                if(node.children().toList().find { it.toString() == searchPhrase } == null) {
                    node.add(SortedMutableTreeNode(searchPhrase))
                    (historyBySearchTypePanel?.model as DefaultTreeModel).reload()
                }
            }

            val rc = historyBySearchTypePanel?.rowCount
            var index = 0
            for (index in 0..100)
                historyBySearchTypePanel?.expandRow(index)

            historyBySearchTypePanel?.expandPath(TreePath(root))
        }
    }
}