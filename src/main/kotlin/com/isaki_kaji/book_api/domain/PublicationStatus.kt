package com.isaki_kaji.book_api.domain

/**
 * 出版状況を表すEnum
 */
enum class PublicationStatus {
    UNPUBLISHED,
    PUBLISHED;
    
    /**
     * 指定された状況への変更が可能かを判定する
     * ビジネスルール: 出版済みから未出版への変更は不可
     */
    fun canChangeTo(newStatus: PublicationStatus): Boolean {
        return when (this) {
            UNPUBLISHED -> true  // 未出版からはどちらにも変更可能
            PUBLISHED -> newStatus == PUBLISHED  // 出版済みからは出版済みのみ
        }
    }
}
