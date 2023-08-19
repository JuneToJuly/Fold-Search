package thomas.gian

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.ui.popup.JBPopup

abstract class FoldSearchBase: AnAction() {
    var foldSearchPopup: JBPopup? = null
    fun close()
    {
        foldSearchPopup?.cancel()
    }
}
