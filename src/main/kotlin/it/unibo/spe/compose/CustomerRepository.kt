package it.unibo.spe.compose

interface CustomerRepository {
    fun findById(id: CustomerID): Iterable<Customer>
    fun findByName(name: String): Iterable<Customer>
    fun add(customer: Customer)
    fun update(oldId: CustomerID, customer: Customer)
    fun remove(id: CustomerID)
}
