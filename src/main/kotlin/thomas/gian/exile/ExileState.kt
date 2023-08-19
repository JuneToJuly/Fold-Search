package thomas.gian.exile

import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.util.xmlb.XmlSerializerUtil.copyBean

@State(name = "thomas.gian.exile.ExileState", storages = [Storage("ExileState.xml")])
class ExileState : com.intellij.openapi.components.PersistentStateComponent<ExileState> {

    // Each file has a unique exile key
    // That exile key is used to store the list of passages
    // currentExileForFile.get(Filename)
    // exilePassages(key)

    // map a key to a list of passages
    var exileList: MutableMap<Int, Int>? = mutableMapOf()
    var exileCount: Int = 0
    var passageCount: Int = 0
    // map key to exile passage
    var exilePassages: MutableMap<Int, MutableList<String>>? = mutableMapOf()
    var currentExileForFile: MutableMap<String, Int>? = mutableMapOf()

    fun getPassageForFile(file: String): MutableList<String>? {
        val exiledState = ExileState.getInstance()
        val filenameToExile = exiledState.currentExileForFile?.getOrDefault(file, exiledState.exileCount++)
        // get the current exile's passage key
        val exileToPassage = exiledState.exileList?.get(filenameToExile) ?: exiledState.passageCount++
        // get the current pass
        return exiledState.exilePassages?.getOrDefault(exileToPassage, mutableListOf())
    }

    fun getAllExiledLinesForFile(file: String): List<String> {
        return getInstance().getPassageForFile(file)
            ?.flatMap { it.split("\n").filter { myString -> myString.isNotEmpty() } } ?: mutableListOf()
    }

    override fun getState(): ExileState {
        return this
    }

    override fun loadState(state: ExileState) {
        copyBean(state, this)
    }

    companion object {
        fun getInstance(): ExileState {
            return service()
        }

    }
}