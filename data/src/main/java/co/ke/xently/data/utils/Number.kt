package co.ke.xently.data.utils


val Number.forDisplay: String
    get() = toString().let {
        if (it.endsWith(".0")) {
            it.replace(".0", "")
        } else {
            it
        }
    }