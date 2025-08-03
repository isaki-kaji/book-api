package com.isaki_kaji.book_api.domain

/**
 * 出版状況を表すEnum
 */
enum class PublicationStatus {
    UNPUBLISHED,
    PUBLISHED;
    
    /**
     * 書籍を出版する
     * ビジネスルール: 未出版の書籍のみ出版可能、出版済みの書籍は出版済みのまま
     * 
     * @return 出版後のステータス
     */
    fun publish(): PublicationStatus {
        return when (this) {
            UNPUBLISHED -> PUBLISHED  // 未出版から出版済みに変更
            PUBLISHED -> PUBLISHED    // 出版済みは出版済みのまま（冪等性）
        }
    }
}
