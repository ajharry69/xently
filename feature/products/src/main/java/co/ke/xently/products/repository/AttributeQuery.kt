package co.ke.xently.products.repository

data class AttributeQuery(private val name: String = "", private val value: String = "") {
    enum class Type {
        NAME,
        VALUE,
        BOTH,
        NONE,
    }

    val type: Type
        get() = if (nameQuery != "" && valueQuery != "") {
            Type.BOTH
        } else if (nameQuery != "") {
            Type.NAME
        } else if (valueQuery != "") {
            Type.VALUE
        } else {
            Type.NONE
        }

    var nameQuery: String = ""
        private set(value) {
            field = value.trim()
        }

    var valueQuery: String = ""
        private set(value) {
            field = value.trim()
        }

    val isDefault: Boolean
        get() = nameQuery == "" && valueQuery == ""

    init {
        nameQuery = name
        valueQuery = value
    }
}
