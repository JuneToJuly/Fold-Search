package thomas.gian
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean

@State(name = "thomas.gian.FoldSearchState", storages = [Storage("FoldSearchState.xml")])
class FoldSearchState : com.intellij.openapi.components.PersistentStateComponent<FoldSearchState> {

    var methodScoping: Boolean = false
    var comments: Boolean = false

    override fun getState(): FoldSearchState {
        return this
    }

    override fun loadState(state: FoldSearchState) {
        copyBean(state, this)
    }

    companion object {
        fun getInstance(): FoldSearchState {
            return service()
        }
    }
}