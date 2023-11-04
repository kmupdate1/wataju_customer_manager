package jp.wataju.session

import java.util.UUID

data class AccountSession(
    val id: UUID?,
    val identifier: String?,
    val admin: Boolean?
)
