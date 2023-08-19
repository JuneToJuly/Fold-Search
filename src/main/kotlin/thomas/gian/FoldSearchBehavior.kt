package thomas.gian

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.VisualPosition

class FoldSearchBehavior {

    companion object {
        fun exitFoldSearchField(selectedTextEditor: Editor) {
            val visualPosition = selectedTextEditor.caretModel.visualPosition
            selectedTextEditor.caretModel.moveToVisualPosition(VisualPosition(0, 0))
            selectedTextEditor.scrollingModel.scrollToCaret(ScrollType.CENTER)
            visualPosition.let { selectedTextEditor.caretModel.moveToVisualPosition(it) }
        }

    }
}