package thomas.gian

import com.intellij.credentialStore.createSecureRandom
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiReferenceExpression
import com.intellij.psi.util.PsiTreeUtil

class MethodFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        // Initialize the group of folding regions that will expand/collapse together.
        val group: FoldingGroup = FoldingGroup.newGroup("New")
        // Initialize the list of folding regions
        val descriptors: MutableList<FoldingDescriptor> = ArrayList()
        // Get a collection of the literal expressions in the document below root
        val referenceExpression: Collection<PsiReferenceExpression> =
            PsiTreeUtil.findChildrenOfType(root, PsiReferenceExpression::class.java)
        // Evaluate the collection
        val trigger = "set"
        for (expression in referenceExpression) {
            val value = expression.text
            if (value != null && value.contains(trigger)) {
                val project: Project = expression.project
                val key: Int = value.indexOf(trigger)
                if (value.contains(trigger)) {
                    // Add a folding descriptor for the literal expression at this node.
                    descriptors.add(
                        FoldingDescriptor(
                            expression.node,
                            TextRange(
                                expression.textRange.startOffset,
                                document.getLineEndOffset(document.getLineNumber(expression.textRange.endOffset)),
                            ),
                            FoldingGroup.newGroup("CUP:" + createSecureRandom().nextFloat())
                        )
                    )
                }
            }
        }
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {

        return node.text + "..."
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }
}