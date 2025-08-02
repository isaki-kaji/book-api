package com.isaki_kaji.book_api.dto

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.domain.Book
import java.time.LocalDate

/**
 * 著者の書籍一覧レスポンス
 */
data class AuthorBooksResponse(
    val authorId: Long,
    val authorName: String,
    val authorBirthDate: LocalDate,
    val books: List<BookSummaryResponse>
) {
    companion object {
        fun from(author: Author, books: List<Book>): AuthorBooksResponse {
            requireNotNull(author.id) { "著者IDは必須です" }
            return AuthorBooksResponse(
                authorId = author.id,
                authorName = author.name,
                authorBirthDate = author.birthDate,
                books = books.map { BookSummaryResponse.from(it) }
            )
        }
    }
}
