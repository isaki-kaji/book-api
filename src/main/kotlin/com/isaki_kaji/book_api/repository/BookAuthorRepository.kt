package com.isaki_kaji.book_api.repository

/**
 * 書籍と著者の関連付けリポジトリインターフェース
 */
interface BookAuthorRepository {
    
    /**
     * 書籍と著者の関連付けを作成する
     */
    fun createRelations(bookId: Long, authorIds: List<Long>)
    
    /**
     * 指定された書籍の全ての著者関連付けを削除する
     */
    fun deleteByBookId(bookId: Long)
}
