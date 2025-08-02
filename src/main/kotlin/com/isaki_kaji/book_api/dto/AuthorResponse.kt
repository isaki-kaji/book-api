package com.isaki_kaji.book_api.dto

import java.time.LocalDate

/**
 * 著者レスポンス
 */
data class AuthorResponse(
    val id: Long,
    val name: String,
    val birthDate: LocalDate
) {
    companion object {
        fun from(author: com.isaki_kaji.book_api.domain.Author): AuthorResponse {
            requireNotNull(author.id) { "著者IDは必須です" }
            return AuthorResponse(
                id = author.id,
                name = author.name,
                birthDate = author.birthDate
            )
        }
    }
}
