package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.dto.AuthorBooksResponse
import com.isaki_kaji.book_api.exception.ResourceNotFoundException
import com.isaki_kaji.book_api.repository.AuthorRepository
import com.isaki_kaji.book_api.repository.BookRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 著者の書籍一覧取得ユースケース
 */
@Service
@Transactional(readOnly = true)
class GetAuthorBooksUseCase(
    private val authorRepository: AuthorRepository,
    private val bookRepository: BookRepository
) {
    
    /**
     * 著者に紐づく書籍一覧を取得する
     */
    fun execute(authorId: Long): AuthorBooksResponse {
        // 著者の存在確認
        val author = authorRepository.findById(authorId)
            ?: throw ResourceNotFoundException("著者が見つかりません。ID: $authorId")
        
        // 著者に紐づく書籍IDを取得
        val bookIds = authorRepository.findBookIdsByAuthorId(authorId)
        
        // 書籍情報を取得
        val books = if (bookIds.isNotEmpty()) {
            bookRepository.findByIds(bookIds)
        } else {
            emptyList()
        }
        
        return AuthorBooksResponse.from(author, books)
    }
}
