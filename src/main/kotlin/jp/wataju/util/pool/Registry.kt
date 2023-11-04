package jp.wataju.util.pool

import jp.wataju.util.Properties
import java.util.*

data class UserRegistry(
    val familyName: String,
    val firstName: String,
    val familyNameKana: String,
    val firstNameKana: String,
    val phoneNumber: String,
    val mailAddress: String
)

data class CustomerRegistry (
    val familyName: String,
    val firstName: String,
    val familyNameKana: String,
    val firstNameKane: String,
    val zipcodeWard: String,
    val zipcodeTown: String,
    val prefecture: String,
    val address1: String,
    val address2: String,
    val address3: String,
    val phoneNumber: String,
    val mailAddress: String
)

data class ProductRegistry(
    val productName: String,
    val productNameKana: String,
    val price: Int,
    val enabled: Enabled
) {
    val condition = enabled.condition
}

enum class Enabled(val enabled: Boolean, val condition: String) {
    TRUE(true, Properties.getText("TRUE")),
    FALSE(false, Properties.getText("FALSE"))
}
data class OrderRegistry (
    val customerId: UUID,
    val orders: MutableMap<UUID, Int>,
    val orderDate: String,
)
