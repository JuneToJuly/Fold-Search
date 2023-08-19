package com.example

import com.intellij.credentialStore.createSecureRandom
import com.intellij.lang.ASTNode
import com.intellij.lang.folding.FoldingBuilderEx
import com.intellij.lang.folding.FoldingDescriptor
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.FoldingGroup
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import com.intellij.psi.util.PsiTreeUtil
import kotlin.random.Random

class NewFoldingBuilder : FoldingBuilderEx() {
    override fun buildFoldRegions(root: PsiElement, document: Document, quick: Boolean): Array<FoldingDescriptor> {
        // Initialize the group of folding regions that will expand/collapse together.
        val group: FoldingGroup = FoldingGroup.newGroup("New")
        // Initialize the list of folding regions
        val descriptors: MutableList<FoldingDescriptor> = ArrayList()
        // Get a collection of the literal expressions in the document below root
//        val declarationStatement: Collection<PsiDeclarationStatement> =
//            PsiTreeUtil.findChildrenOfType(root, PsiDeclarationStatement::class.java)
//        // Evaluate the collection
//        for (declaration in declarationStatement) {
//            val value = declaration.text
//            if (value != null && value.contains("new ")) {
//                val project: Project = declaration.getProject()
//                val key: Int = value.indexOf("new ")
//                if (value.contains("new ")) {
//                    // Add a folding descriptor for the literal expression at this node.
//                    descriptors.add(
//                        FoldingDescriptor(
//                            declaration.getNode(),
//                            TextRange(
//                                declaration.getTextRange().getStartOffset() + key,
//                                declaration.getTextRange().getStartOffset() + key + "new ".length,
//                            ),
//                            FoldingGroup.newGroup("New:" + createSecureRandom().nextFloat())
//                        )
//                    )
//                }
//            }
//        }
//
//        val expressionStatement: Collection<PsiExpressionStatement> =
//            PsiTreeUtil.findChildrenOfType(root, PsiExpressionStatement::class.java)
//        // Evaluate the collection
//        for (expression in expressionStatement) {
//            val value = expression.text
//            if (value != null && value.contains("new ")) {
//                val project: Project = expression.getProject()
//                val key: Int = value.indexOf("new ")
//                if (value.contains("new ")) {
//                    // Add a folding descriptor for the literal expression at this node.
//                    descriptors.add(
//                        FoldingDescriptor(
//                            expression.getNode(),
//                            TextRange(
//                                expression.getTextRange().getStartOffset() + key,
//                                expression.getTextRange().getStartOffset() + key + "new ".length,
//                           ),
//                            FoldingGroup.newGroup("New:" + createSecureRandom().nextFloat())
//                        )
//                    )
//                }
//            }
//        }

        val expressionList: Collection<PsiNewExpression> =
            PsiTreeUtil.findChildrenOfType(root, PsiNewExpression::class.java)
        // Evaluate the collection
        for (expression in expressionList) {
            val value = expression.text
            if (value != null && value.contains("new")) {
                val project: Project = expression.project
                val key: Int = value.indexOf("new")
                if (value.contains("new")) {
                    // Add a folding descriptor for the literal expression at this node.
                    descriptors.add(
                        FoldingDescriptor(
                            expression.node,
                            TextRange(
                                expression.textRange.startOffset + key,
                                expression.textRange.startOffset + key + "new ".length,
                            ),
                            FoldingGroup.newGroup("New:" + createSecureRandom().nextFloat())
                        )
                    )
                }
            }
        }

        return descriptors.toTypedArray()
    }

    override fun getPlaceholderText(node: ASTNode): String {
        return ""
    }

    override fun isCollapsedByDefault(node: ASTNode): Boolean {
        return true
    }
}