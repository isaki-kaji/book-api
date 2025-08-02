package com.isaki_kaji.book_api.dto

import com.isaki_kaji.book_api.domain.Book
import com.isaki_kaji.book_api.domain.PublicationStatus

/**
 * 書籍レスポンス
 */
data class BookResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val authors: List<AuthorResponse>,
    val publicationStatus: PublicationStatus
) {
    companion object {
        fun from(book: Book): BookResponse {
            requireNotNull(book.id) { "書籍IDは必須です" }
            return BookResponse(
                id = book.id,
                title = book.title,
                price = book.price.toInt(),
                authors = book.authors.map { AuthorResponse.from(it) },
                publicationStatus = book.publicationStatus
            )
        }
    }
}
