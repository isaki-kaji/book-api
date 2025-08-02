package com.isaki_kaji.book_api.repository

import com.isaki_kaji.book_api.domain.Book
import com.isaki_kaji.book_api.domain.Price
import com.isaki_kaji.book_api.domain.PublicationStatus

/**
 * 書籍リポジトリインターフェース
 */
interface BookRepository {
    
    /**
     * 書籍を作成する
     */
    fun create(book: Book): Book
    
    /**
     * IDで書籍を取得する（著者情報も含む）
     */
    fun findByIdWithAuthors(id: Long): Book?
    
    /**
     * 書籍の基本情報を更新する
     */
    fun updateBasicInfo(id: Long, title: String, price: Price): Book
    
    /**
     * 出版状況を更新する
     */
    fun updatePublicationStatus(id: Long, publicationStatus: PublicationStatus): Book
    
    /**
     * 指定された書籍IDリストで書籍を取得する
     */
    fun findByIds(ids: List<Long>): List<Book>
}
