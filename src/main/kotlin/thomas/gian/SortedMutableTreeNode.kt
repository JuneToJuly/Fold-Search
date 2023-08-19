package thomas.gian

import java.util.*
import javax.swing.tree.DefaultMutableTreeNode
import javax.swing.tree.MutableTreeNode
import kotlin.Comparator

class SortedMutableTreeNode : DefaultMutableTreeNode {

    constructor(userObject: Any) {
        this.userObject = userObject
        this.allowsChildren = true
    }


    override fun add(newChild: MutableTreeNode?) {
        super.add(newChild)
        sort()
    }

    private fun sort() {
        Collections.sort(children, Comparator { o1, o2 ->
            (o2 as SortedMutableTreeNode)
            (o1 as SortedMutableTreeNode).userObject.toString().compareTo(o2.userObject.toString(), true)
        })
    }
}