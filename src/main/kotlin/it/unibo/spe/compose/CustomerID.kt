package it.unibo.spe.compose

interface CustomerID {
    val value: Any

    companion object {
        @JvmStatic
        fun parse(id: String) = id.toLongOrNull()?.let { VatNumber(it) } ?: TaxCode(id)
    }
}
