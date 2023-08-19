package thomas.gian

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.NlsSafe
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.refactoring.suggested.endOffset
import com.intellij.refactoring.suggested.startOffset

class EditorUtils {
    companion object {

        fun getMethodTextAtCaret(project: Project): String {
            val manager = project?.let { FileEditorManager.getInstance(it) }
            val selectedTextEditor = manager?.selectedTextEditor
            var offset = -1;
            val caret = selectedTextEditor?.caretModel
            val psiFile = selectedTextEditor?.project?.let {
                PsiDocumentManager.getInstance(it).getPsiFile(
                    selectedTextEditor.document
                )
            }

            caret?.offset?.let { offset = it }
            var findElementAt = offset.let { psiFile?.findElementAt(it) }
            var method = PsiTreeUtil.getParentOfType(findElementAt, PsiMethod::class.java)
            val startingLine =
                method?.startOffset?.let { manager?.selectedTextEditor?.document?.getLineNumber(it) } ?: 0
            val endingLine = method?.endOffset?.let { manager?.selectedTextEditor?.document?.getLineNumber(it) } ?: 0
            val text = selectedTextEditor?.document?.getText(
                TextRange(
                    selectedTextEditor.document.getLineStartOffset(startingLine),
                    selectedTextEditor.document.getLineEndOffset(endingLine)
                )
            )
            return text ?: ""
        }
        fun getComments(psiFile: PsiFile): MutableList<Pair<Int?, Int?>> {
            var indexedComments = mutableListOf<Pair<Int?, Int?>>()
            if(!FoldSearchState.getInstance().comments) {
                return indexedComments
            }

            var comments: MutableList<PsiComment> = mutableListOf()
            psiFile.let { it.accept(object: JavaRecursiveElementVisitor() {
                override fun visitComment(comment: PsiComment) {
                    super.visitComment(comment)
                    comments.add(comment)
                }
            })
            }
            indexedComments = comments.map {
                val document = PsiDocumentManager.getInstance(psiFile.project).getDocument(psiFile)
                Pair(document?.getLineNumber(it.startOffset), document?.getLineNumber(it.endOffset))
            }.toMutableList()
            return indexedComments
        }
    }
}