package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.dto.AuthorRequest
import com.isaki_kaji.book_api.dto.BookUpdateRequest
import com.isaki_kaji.book_api.dto.BookResponse
import com.isaki_kaji.book_api.exception.ResourceNotFoundException
import com.isaki_kaji.book_api.repository.AuthorRepository
import com.isaki_kaji.book_api.repository.BookRepository
import com.isaki_kaji.book_api.repository.BookAuthorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 書籍更新ユースケース
 */
@Service
@Transactional
class UpdateBookUseCase(
    private val bookRepository: BookRepository,
    private val bookAuthorRepository: BookAuthorRepository,
    private val authorProcessor: AuthorProcessor
) {
    
    /**
     * 書籍の基本情報を更新する（タイトル、価格、著者）
     */
    fun execute(id: Long, request: BookUpdateRequest): BookResponse {
        // 既存書籍の存在確認
        val existingBook = bookRepository.findByIdWithAuthors(id)
            ?: throw ResourceNotFoundException("書籍が見つかりません。ID: $id")
        
        // 著者の処理（重複チェック + 新規作成 or 既存使用）
        val authors = authorProcessor.processAuthors(request.authors)
        
        // 書籍の基本情報を更新
        bookRepository.updateBasicInfo(id, request.title, request.getPrice())
        
        // 既存の著者関連付けを削除
        bookAuthorRepository.deleteByBookId(id)
        
        // 新しい著者関連付けを作成
        bookAuthorRepository.createRelations(
            bookId = id,
            authorIds = authors.map { it.id!! }
        )
        
        // 完全な書籍情報を取得して返却
        val updatedBook = bookRepository.findByIdWithAuthors(id)!!
        return BookResponse.from(updatedBook)
    }
}
