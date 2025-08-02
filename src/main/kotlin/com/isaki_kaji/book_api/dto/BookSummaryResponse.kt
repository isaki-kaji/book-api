package com.isaki_kaji.book_api.dto

import com.isaki_kaji.book_api.domain.Book
import com.isaki_kaji.book_api.domain.PublicationStatus

/**
 * 著者の書籍一覧レスポンス用の書籍サマリー情報
 */
data class BookSummaryResponse(
    val id: Long,
    val title: String,
    val price: Int,
    val publicationStatus: PublicationStatus
) {
    companion object {
        fun from(book: Book): BookSummaryResponse {
            requireNotNull(book.id) { "書籍IDは必須です" }
            return BookSummaryResponse(
                id = book.id,
                title = book.title,
                price = book.price.toInt(),
                publicationStatus = book.publicationStatus
            )
        }
    }
}
