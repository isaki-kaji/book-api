package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.dto.BookResponse
import com.isaki_kaji.book_api.domain.PublicationStatus
import com.isaki_kaji.book_api.exception.ResourceNotFoundException
import com.isaki_kaji.book_api.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 書籍出版ユースケース
 */
@Service
@Transactional
class PublishBookUseCase(
    private val bookRepository: BookRepository
) {
    
    /**
     * 書籍を出版する
     * 冪等性: 既に出版済みの書籍を出版しても問題なし
     */
    fun execute(id: Long): BookResponse {
        // 既存書籍の存在確認
        val existingBook = bookRepository.findByIdWithAuthors(id)
            ?: throw ResourceNotFoundException("書籍が見つかりません。ID: $id")
        
        // ドメインモデルで出版処理（ビジネスルールの確認）
        existingBook.publish()
        
        // データベースの更新（冪等性のため、常に PUBLISHED に設定）
        bookRepository.updatePublicationStatus(id, PublicationStatus.PUBLISHED)
        
        val updatedBook = bookRepository.findByIdWithAuthors(id)!!
        return BookResponse.from(updatedBook)
    }
}
