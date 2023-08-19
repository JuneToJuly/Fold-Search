package thomas.gian

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.VisibleAreaListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiFile
import javax.swing.JSplitPane

class WideScreen : AnAction() {
    var currentEditor: Editor? = null
    var files = mutableSetOf<Editor>()

    override fun actionPerformed(event: AnActionEvent) {
        val manager = event.project?.let { FileEditorManager.getInstance(it) }
        val windowMan = event.project?.let { FileEditorManagerEx.getInstanceEx(it) }
        val currentWindow = windowMan?.currentWindow

        windowMan?.unsplitAllWindow()
        // Change width of window


        windowMan?.createSplitter(JSplitPane.HORIZONTAL_SPLIT, currentWindow)
        val leftWindow = windowMan?.windows?.get(0)
        val createSplitter = windowMan?.createSplitter(JSplitPane.HORIZONTAL_SPLIT, leftWindow)

        val selectedEditor = event.project?.let { FileEditorManager.getInstance(it).selectedTextEditor }
        val currentEditor = selectedEditor?.let { it }

//        val splitPane = currentEditor?.component as JSplitPane
//        splitPane.dividerLocation = -1

        val psifile = event.project?.let {
            selectedEditor?.document?.let { it1 ->
                PsiDocumentManager.getInstance(it).getPsiFile(it1)
            }
        }

        files.forEach { it.scrollingModel.removeVisibleAreaListener(listener) }
        files.clear()
        val addVisibleAreaListener = selectedEditor?.scrollingModel?.addVisibleAreaListener(listener)
        files.add(selectedEditor!!)
    }

    val listener = VisibleAreaListener {

        val editors = EditorFactory.getInstance().getEditors(it.editor.document)
        val noCurrentEditors = editors.filter { it != currentEditor }
        noCurrentEditors[0].scrollingModel.scrollVertically(it.newRectangle.y - it.newRectangle.height)
        noCurrentEditors[1].scrollingModel.scrollVertically(it.newRectangle.y + it.newRectangle.height)
        val scrollModel = currentEditor?.scrollingModel
        val foldingModel = currentEditor?.foldingModel

        if (foldingModel != null && scrollModel != null && currentEditor != null) {
            val totalFold = let {
                foldingModel
                    .allFoldRegions
                    .filter { !it.isExpanded }
                    .sumOf {
                        Math.abs((it.document.getLineNumber(it.endOffset) - it.document.getLineNumber(it.startOffset)) * it.editor.lineHeight)
                    }
            }

            val totalHeight = (currentEditor!!.lineHeight * currentEditor!!.document.lineCount) - totalFold

            if (scrollModel.visibleArea.y < currentEditor!!.scrollingModel.visibleArea.height && !(currentEditor?.scrollingModel!!.visibleArea.height > totalHeight)) {
                currentEditor?.scrollingModel?.scrollVertically(it.newRectangle.height)
            } else if (currentEditor!!.scrollingModel.visibleArea.y > totalHeight
                - (2 * currentEditor!!.scrollingModel.visibleArea.height)
                && !(currentEditor!!.scrollingModel.visibleArea.height > totalHeight)
            ) {
                currentEditor!!.scrollingModel.scrollVertically(totalHeight - (2 * currentEditor!!.scrollingModel.visibleArea.height))
            }
        }
    }
}