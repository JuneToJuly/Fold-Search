package thomas.gian

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow

class IntelliJUtils {
    companion object {
        fun getToolWindow(project: Project, name: String) : ToolWindow? {
            return IntelliJManagers.getToolWindowManager(project).getToolWindow(name)
        }
    }
}