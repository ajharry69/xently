package co.ke.xently.data

abstract class AbstractBrand {
    abstract var name: String
    val isDefault: Boolean
        get() = name == ""

    override fun toString() = name

    override fun hashCode(): Int {
        return name.hashCode()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Product.Brand

        if (name != other.name) return false

        return true
    }
}