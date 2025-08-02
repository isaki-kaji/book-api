package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.dto.PublicationStatusUpdateRequest
import com.isaki_kaji.book_api.dto.BookResponse
import com.isaki_kaji.book_api.exception.ResourceNotFoundException
import com.isaki_kaji.book_api.exception.BusinessRuleViolationException
import com.isaki_kaji.book_api.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 出版状況更新ユースケース
 */
@Service
@Transactional
class UpdatePublicationStatusUseCase(
    private val bookRepository: BookRepository
) {
    
    /**
     * 出版状況を更新する
     */
    fun execute(id: Long, request: PublicationStatusUpdateRequest): BookResponse {
        // 既存書籍の存在確認
        val existingBook = bookRepository.findByIdWithAuthors(id)
            ?: throw ResourceNotFoundException("書籍が見つかりません。ID: $id")
        
        // ドメインモデルでビジネスルールをチェック
        try {
            val updatedDomainBook = existingBook.updatePublicationStatus(request.publicationStatus)
            
            // データベースの更新
            bookRepository.updatePublicationStatus(id, request.publicationStatus)
            
            val updatedBook = bookRepository.findByIdWithAuthors(id)!!
            return BookResponse.from(updatedBook)
        } catch (e: IllegalStateException) {
            throw BusinessRuleViolationException(e.message ?: "出版状況の更新でエラーが発生しました")
        }
    }
}
