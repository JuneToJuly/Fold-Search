package thomas.gian

import com.intellij.credentialStore.createSecureRandom
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.tree.java.IJavaElementType
import com.intellij.psi.util.PsiTreeUtil
import kotlin.random.Random

class SrsFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        // Initialize the group of folding regions that will expand/collapse together.
        val group: FoldingGroup = FoldingGroup.newGroup("New")
        // Initialize the list of folding regions
        val descriptors: MutableList<FoldingDescriptor> = ArrayList()
        // Get a collection of the literal expressions in the document below root

        for (i in 0 until document.lineCount) {
            val startOffset = document.getLineStartOffset(i)
            val endOffset = document.getLineEndOffset(i)

            // find psi element at offset
            val element = root.findElementAt(endOffset) ?: continue
            element.let {

                // get text of element
                val textForfold = document.getText(TextRange( startOffset, endOffset + 1))

                val descriptor = FoldingDescriptor(
                    it,
                     startOffset,
                        endOffset + 1
                    , FoldingGroup.newGroup("SRS:" + createSecureRandom().nextFloat()), "")
                descriptor.setCanBeRemovedWhenCollapsed(false)
                descriptors.add(descriptor)
            }
        }

        val keywords: Collection<PsiJavaToken> =
            PsiTreeUtil.findChildrenOfType(root, PsiJavaToken::class.java)
        // Evaluate the collection
        for (keyword in keywords) {
            // Add a folding descriptor for the literal expression at this node.
            if(keyword.text.equals("{") || keyword.text.equals("}")) {
                val start = document.getLineStartOffset(document.getLineNumber(keyword.textRange.startOffset))
                descriptors.add(
                    FoldingDescriptor(
                        keyword.node,
                        TextRange(
                            keyword.textRange.startOffset,
                            keyword.textRange.endOffset
                        ),
                        FoldingGroup.newGroup("SRS:" + createSecureRandom().nextFloat())
                    )
                )
            }
        }

//        val whiteSpace: Collection<PsiWhiteSpace> =
//            PsiTreeUtil.findChildrenOfType(root, PsiWhiteSpace::class.java)
//        for (space in whiteSpace) {
//            if (space.getParent() is PsiCodeBlock) {
//                val start = document.getLineStartOffset(document.getLineNumber(space.getTextRange().getStartOffset()))
//                descriptors.add(
//                    FoldingDescriptor(
//                        space.getNode(),
//                        TextRange(
//                            space.getTextRange().getStartOffset(),
//                            space.getTextRange().endOffset
//                        ),
//                        FoldingGroup.newGroup("SRS:" + createSecureRandom().nextFloat())
//                    )
//                )
//            }
//        }

        descriptors.forEach{ it.setCanBeRemovedWhenCollapsed(false)}
        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return ""
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return false
    }
}