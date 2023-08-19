package thomas.gian

import com.intellij.ide.highlighter.JavaFileType
import com.intellij.openapi.actionSystem.ActionManager
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


class VarShowWindow(val project: Project, val toolWindow: ToolWindow) {

    private var scrollPane: JBScrollPane? = null
    private var panel: Tree? = null
    private var running = AtomicBoolean(false)
    private var currentMethod: PsiMethod? = null
    private var classTypes: MutableMap<String, SortedMutableTreeNode>? = null

    fun getContent(): JBScrollPane {
        val manager = FileEditorManager.getInstance(project)
        val root = SortedMutableTreeNode("No Method Selected")

        panel = Tree(DefaultTreeModel(root))
        scrollPane?.isFocusable = false
        toolWindow?.component.isFocusable = false
        panel?.addMouseListener(FoldSearchVarViewerMouseListener(project, panel!!, toolWindow))

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

        panel?.cellRenderer = renderer
        scrollPane = JBScrollPane(panel)
        classTypes = mutableMapOf()

        EditorFactory.getInstance().eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(event: CaretEvent) {
                val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(event.editor.document)
                val findElementAt = event.caret?.offset?.let { psiFile?.findElementAt(it) }
                val method = PsiTreeUtil.getParentOfType(findElementAt, PsiMethod::class.java)
                if (method?.equals(currentMethod) == true) return
                if (!running.compareAndSet(false, true))
                    return

                currentMethod = method

                val caret = manager.selectedTextEditor?.caretModel?.currentCaret

                SwingUtilities.invokeLater {
                    root.userObject = currentMethod?.returnType?.presentableText + " " + currentMethod?.name + currentMethod?.parameterList?.text
                    if(root.userObject.toString().contains("null")){
                        root.userObject = "No method selected"
                    }
                    else
                    {
                        root.removeAllChildren()
                        classTypes?.clear()
                    }

                    val document = manager?.selectedTextEditor?.document
                    if (document != null) {
                        val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
                        caret?.let {
                            val element = psiFile?.findElementAt(it.offset)
                            val mytext = element?.text ?: ""
                            currentMethod?.accept(object : JavaRecursiveElementVisitor() {
                                override fun visitLocalVariable(variable: PsiLocalVariable?) {
                                    super.visitLocalVariable(variable)

                                    var text = variable?.text
                                    if (text != null) {
                                        if (text.contains("="))
                                            text = text.removeRange(text.indexOf("="), text.length)
                                        if (!text.contains("{")) {
                                            val code = JavaCodeFragmentFactory.getInstance(project)
                                                .createExpressionCodeFragment(text, variable, null, true)
                                            if(text.contains(", ")){
                                                text = text.replace(", ",",")
                                            }
                                            var declaration =  text.split(" ")
                                            val clazz = declaration[0].replace(",", ", ")

                                            val variable = declaration[1]
                                            var classNode = SortedMutableTreeNode(clazz)
                                            if(classTypes?.contains(clazz) == true) {
                                                classNode = classTypes?.get(clazz)!!
                                            }
                                            else {
                                                classTypes?.put(clazz, classNode)
                                                root.add(classNode)
                                            }
                                            classNode.children().toList().stream()
                                                .map { it as SortedMutableTreeNode }
                                                .filter { it.userObject.toString().contains(variable) }
                                                .forEach {
                                                classNode.remove(it)
                                            }
                                            classNode.add(SortedMutableTreeNode(variable))
                                        }
                                    }
                                }
                            })
                            val model =  panel?.model
                            if(model is DefaultTreeModel)
                                model.reload(root)

                            val rc = panel?.rowCount
                            var index= 0
                            for (index in 0..100)
                                panel?.expandRow(index)

                            panel?.expandPath(TreePath(root))
//                            panel?.repaint()
                        }
                        running.set(false)
                    }
                }
            }
        })

        return scrollPane as JBScrollPane
    }

}