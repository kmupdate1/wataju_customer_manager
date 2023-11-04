package jp.wataju.util.parser

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderCondition(
    @SerialName("id") val id: String,
    @SerialName("amount") val amount: Int
)
