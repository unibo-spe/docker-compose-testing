package it.unibo.spe.compose

data class TaxCode(override val value: String) : CustomerID {
    init {
        require(PATTERN.matches(value))
    }
    companion object {
        private val PATTERN = "[A-Z]{6}\\d{2}[A-Z]\\d{2}[A-Z]\\d{3}[A-Z]".toRegex()
    }
}
