package thomas.gian

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileEditor.ex.FileEditorManagerEx


class CleanJava : AnAction() {
    override fun actionPerformed(event: AnActionEvent) {
        val manager = event.project?.let { FileEditorManager.getInstance(it) }
        val windowMan = event.project?.let { FileEditorManagerEx.getInstanceEx(it) }

        EditorFactory.getInstance().eventMulticaster.addCaretListener(object : CaretListener {
            override fun caretPositionChanged(e: CaretEvent) {

                manager?.selectedTextEditor?.foldingModel?.runBatchFoldingOperation ({
                    manager.selectedTextEditor?.foldingModel?.allFoldRegions?.forEach {
                        if(it.placeholderText.equals("") && it.group.toString().contains("CUP")){
                            if (e.caret?.offset != null) {
                                val carretLine = it.document.getLineNumber(e.caret?.offset ?: 0)
                                val foldLine = it.document.getLineNumber(it.startOffset)
                                if (it.isExpanded && (carretLine != foldLine)) {
                                    it.isExpanded = false
                                }
                                else if(!it.isExpanded && (carretLine == foldLine) &&
                                    !it.group.toString().contains("New")) {
                                        it.isExpanded = true
                                }
                            }
                        }
                    }
                }, false, true)
            }
        })
    }
}