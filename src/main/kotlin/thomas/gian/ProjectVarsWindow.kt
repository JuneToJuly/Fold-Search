package thomas.gian

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.psi.*
import com.intellij.psi.impl.java.stubs.index.JavaMethodNameIndex
import com.intellij.psi.search.PsiSearchHelper
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.ui.ColoredTreeCellRenderer
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.treeStructure.Tree
import java.awt.Color
import java.util.concurrent.atomic.AtomicBoolean
import javax.swing.JTree
import javax.swing.SwingUtilities
import javax.swing.tree.*


class ProjectVarsWindow(val project: Project, val toolWindow: ToolWindow) {

    private var scrollPane: JBScrollPane? = null
    private var panel: Tree? = null
    private var running = AtomicBoolean(false)
    private var currentMethod: PsiMethod? = null
    private var classTypes: MutableMap<String, SortedMutableTreeNode>? = null

    fun getContent(): JBScrollPane {
        val manager = FileEditorManager.getInstance(project)

        val testString = "`version_number=1.0.0`"

        // get stored strings from xml file
        // create a listener on the current window to be looking for $words$
        // when a $word$ is found, look for the word in the current file
        // if found then sub that word in the current file with the stored string
        // if that word is not found in the current file, then create a new variable in the current file with prompt
        // and sub that word in the current file with the stored string

        // any changes to the var in the project, must be propagated to the xml file and all referenced files
        return scrollPane as JBScrollPane
    }
}