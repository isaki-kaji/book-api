package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.domain.Book
import com.isaki_kaji.book_api.dto.AuthorRequest
import com.isaki_kaji.book_api.dto.BookCreateRequest
import com.isaki_kaji.book_api.dto.BookResponse
import com.isaki_kaji.book_api.repository.AuthorRepository
import com.isaki_kaji.book_api.repository.BookRepository
import com.isaki_kaji.book_api.repository.BookAuthorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 書籍作成ユースケース
 */
@Service
@Transactional
class CreateBookUseCase(
    private val bookRepository: BookRepository,
    private val bookAuthorRepository: BookAuthorRepository,
    private val authorProcessor: AuthorProcessor
) {
    
    /**
     * 書籍を作成する
     * 著者が存在しない場合は新規作成し、存在する場合は既存を使用する
     */
    fun execute(request: BookCreateRequest): BookResponse {
        // 著者の処理（重複チェック + 新規作成 or 既存使用）
        val authors = authorProcessor.processAuthors(request.authors)
        
        // 書籍を作成
        val book = Book.create(
            title = request.title,
            price = request.getPrice(),
            authors = authors,
            publicationStatus = request.publicationStatus
        )
        
        val createdBook = bookRepository.create(book)
        
        // 書籍と著者の関連付けを作成
        bookAuthorRepository.createRelations(
            bookId = createdBook.id!!,
            authorIds = authors.map { it.id!! }
        )
        
        // 完全な書籍情報を取得して返却
        val completeBook = bookRepository.findByIdWithAuthors(createdBook.id!!)!!
        return BookResponse.from(completeBook)
    }
}
