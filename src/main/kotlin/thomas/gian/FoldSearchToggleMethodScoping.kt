package thomas.gian

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.wm.ToolWindowManager

class FoldSearchToggleMethodScoping : AnAction() {

    override fun actionPerformed(event: AnActionEvent) {
        FoldSearchState.getInstance().methodScoping = !FoldSearchState.getInstance().methodScoping
    }
}