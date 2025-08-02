package com.isaki_kaji.book_api.domain

/**
 * 書籍ドメインモデル
 */
data class Book(
    val id: Long?,
    val title: String,
    val price: Price,
    val publicationStatus: PublicationStatus,
    val authors: List<Author> = emptyList()
) {
    companion object {
        /**
         * 新しい書籍を作成する
         * 最低1人の著者が必要
         */
        fun create(
            title: String,
            price: Price,
            authors: List<Author>,
            publicationStatus: PublicationStatus = PublicationStatus.UNPUBLISHED
        ): Book {
            require(title.isNotBlank()) { "書籍タイトルは必須です" }
            require(authors.isNotEmpty()) { "最低1人の著者を指定してください" }
            
            return Book(
                id = null,
                title = title.trim(),
                price = price,
                publicationStatus = publicationStatus,
                authors = authors
            )
        }
    }
    
    /**
     * 書籍の基本情報（タイトル、価格、著者）を更新する
     */
    fun updateBasicInfo(newTitle: String, newPrice: Price, newAuthors: List<Author>): Book {
        require(newTitle.isNotBlank()) { "書籍タイトルは必須です" }
        require(newAuthors.isNotEmpty()) { "最低1人の著者を指定してください" }
        
        return copy(
            title = newTitle.trim(),
            price = newPrice,
            authors = newAuthors
        )
    }
    
    /**
     * 出版状況を更新する
     * ビジネスルール: 出版済みから未出版への変更は不可
     */
    fun updatePublicationStatus(newStatus: PublicationStatus): Book {
        if (!publicationStatus.canChangeTo(newStatus)) {
            throw IllegalStateException(
                "出版済みの書籍を未出版に戻すことはできません。" +
                "現在: $publicationStatus, 変更先: $newStatus"
            )
        }
        return copy(publicationStatus = newStatus)
    }
}
