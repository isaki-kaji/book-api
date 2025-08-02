package com.isaki_kaji.book_api.repository

import com.isaki_kaji.book_api.domain.Author
import java.time.LocalDate

/**
 * 著者リポジトリインターフェース
 */
interface AuthorRepository {
    
    /**
     * 著者を作成する
     */
    fun create(author: Author): Author
    
    /**
     * IDで著者を取得する
     */
    fun findById(id: Long): Author?
    
    /**
     * 名前と生年月日で著者を検索する（重複チェック用）
     */
    fun findByNameAndBirthDate(name: String, birthDate: LocalDate): Author?
    
    /**
     * 著者情報を更新する
     */
    fun update(id: Long, name: String, birthDate: LocalDate): Author
    
    /**
     * 著者に関連する書籍IDリストを取得する
     */
    fun findBookIdsByAuthorId(authorId: Long): List<Long>
}
