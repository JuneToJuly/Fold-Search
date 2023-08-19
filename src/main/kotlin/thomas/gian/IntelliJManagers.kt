package thomas.gian

import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindowManager

class IntelliJManagers {
    companion object {
        fun getFileEditorManager(project: Project): FileEditorManager {
            return FileEditorManager.getInstance(project)
        }
        fun getToolWindowManager(project: Project): ToolWindowManager {
            var manager = getFileEditorManager(project)
            var window = manager.project.let { ToolWindowManager.getInstance(it).getToolWindow("FoldSearchHistory") }
            return ToolWindowManager.getInstance(project)
        }
    }
}