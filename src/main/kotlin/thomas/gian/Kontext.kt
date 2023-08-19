package thomas.gian

import com.intellij.ide.util.EditSourceUtil
import com.intellij.ide.util.PsiNavigationSupport
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.OpenFileDescriptor
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx
import com.intellij.openapi.fileEditor.impl.FileEditorManagerImpl
import com.intellij.pom.Navigatable
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.util.PsiNavigateUtil


class Kontext : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val manager = event.project?.let { FileEditorManager.getInstance(it) }
        val windowMan = event.project?.let { FileEditorManagerEx.getInstanceEx(it) }


        EditorFactory.getInstance().eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(e: CaretEvent) {

                val psiFile = event.project?.let {
                    manager?.selectedTextEditor?.document?.let { it1 ->
                        PsiDocumentManager.getInstance(it).getPsiFile(it1)
                    }
                }


                psiFile?.findElementAt(e.caret?.offset ?: 0)?.let {
                    if (manager is FileEditorManagerImpl) {
                        val fm = manager

                        val parentOfType = PsiTreeUtil.getParentOfType(it, PsiReferenceExpression::class.java)
                        val reference = it.parent.reference?.resolve()
                        if (reference?.navigationElement is Navigatable) {
                            val parentNav = reference.navigationElement as Navigatable
                            val fileToOpen = reference.navigationElement.containingFile.virtualFile ?: return
                            val currentWindow = fm.mainSplitters.currentWindow
                            fm.mainSplitters.windows.filter { it != currentWindow }.first().let { fire ->
                                fm.openFileImpl2(fire, fileToOpen, false )
                            }
//                            fm.openFile(parentNav.virtualFile, parentNav.navigationOffset, parentNav.navigationTarget)
//                            manager?.openFile(reference.containingFile.virtualFile, false);
                            parentNav.navigate(false)
                        }
                    }
                }
            }
        })
    }
}