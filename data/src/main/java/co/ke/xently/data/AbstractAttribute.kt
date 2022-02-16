package co.ke.xently.data

abstract class AbstractAttribute {
    abstract var name: String
    abstract var value: String
    override fun toString() = "${name}:${value}"
}