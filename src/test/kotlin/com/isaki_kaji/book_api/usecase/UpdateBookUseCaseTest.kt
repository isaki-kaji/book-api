package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.domain.Book
import com.isaki_kaji.book_api.domain.Price
import com.isaki_kaji.book_api.domain.PublicationStatus
import com.isaki_kaji.book_api.dto.AuthorRequest
import com.isaki_kaji.book_api.dto.BookUpdateRequest
import com.isaki_kaji.book_api.exception.DuplicateResourceException
import com.isaki_kaji.book_api.exception.ResourceNotFoundException
import com.isaki_kaji.book_api.repository.BookRepository
import com.isaki_kaji.book_api.repository.BookAuthorRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import kotlin.test.assertEquals

@ActiveProfiles("test")
@ExtendWith(MockitoExtension::class)
class UpdateBookUseCaseTest {

    @Mock
    private lateinit var bookRepository: BookRepository
    
    @Mock
    private lateinit var bookAuthorRepository: BookAuthorRepository
    
    @Mock
    private lateinit var authorProcessor: AuthorProcessor
    
    private lateinit var updateBookUseCase: UpdateBookUseCase

    @BeforeEach
    fun setUp() {
        updateBookUseCase = UpdateBookUseCase(bookRepository, bookAuthorRepository, authorProcessor)
    }

    @Test
    fun `書籍更新が正常に実行される`() {
        // Given
        val bookId = 1L
        val author = Author(id = 1L, name = "夏目漱石", birthDate = LocalDate.of(1867, 2, 9))
        val existingBook = Book(
            id = bookId,
            title = "こころ（旧版）",
            price = Price.of(1000),
            publicationStatus = PublicationStatus.PUBLISHED,
            authors = listOf(author)
        )
        
        val updateRequest = BookUpdateRequest(
            title = "こころ（新版）",
            price = 1200,
            authors = listOf(AuthorRequest(name = "夏目漱石", birthDate = LocalDate.of(1867, 2, 9)))
        )
        
        val updatedBook = Book(
            id = bookId,
            title = "こころ（新版）",
            price = Price.of(1200),
            publicationStatus = PublicationStatus.PUBLISHED,
            authors = listOf(author)
        )

        // Mock設定
        `when`(bookRepository.findByIdWithAuthors(bookId)).thenReturn(existingBook)
        `when`(authorProcessor.processAuthors(updateRequest.authors)).thenReturn(listOf(author))
        `when`(bookRepository.existsByTitleAndAuthorIdsExcluding("こころ（新版）", listOf(1L), bookId)).thenReturn(false)
        `when`(bookRepository.findByIdWithAuthors(bookId)).thenReturn(updatedBook)

        // When
        val result = updateBookUseCase.execute(bookId, updateRequest)

        // Then
        assertEquals("こころ（新版）", result.title)
        assertEquals(1200, result.price)
        assertEquals(1, result.authors.size)
        assertEquals("夏目漱石", result.authors[0].name)
        
        verify(bookRepository, times(2)).findByIdWithAuthors(bookId)
        verify(authorProcessor).processAuthors(updateRequest.authors)
        verify(bookRepository).existsByTitleAndAuthorIdsExcluding("こころ（新版）", listOf(1L), bookId)
        verify(bookRepository).updateBasicInfo(bookId, "こころ（新版）", Price.of(1200))
        verify(bookAuthorRepository).deleteByBookId(bookId)
        verify(bookAuthorRepository).createRelations(bookId, listOf(1L))
    }

    @Test
    fun `存在しない書籍IDで更新しようとすると例外が発生する`() {
        // Given
        val bookId = 999L
        val updateRequest = BookUpdateRequest(
            title = "存在しない書籍",
            price = 1000,
            authors = listOf(AuthorRequest(name = "著者", birthDate = LocalDate.of(1900, 1, 1)))
        )

        `when`(bookRepository.findByIdWithAuthors(bookId)).thenReturn(null)

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            updateBookUseCase.execute(bookId, updateRequest)
        }
        assertEquals("書籍が見つかりません。ID: $bookId", exception.message)
        
        verify(bookRepository).findByIdWithAuthors(bookId)
    }

    @Test
    fun `同じタイトルと著者の組み合わせで他の書籍が存在する場合に例外が発生する`() {
        // Given
        val bookId = 1L
        val author = Author(id = 1L, name = "夏目漱石", birthDate = LocalDate.of(1867, 2, 9))
        val existingBook = Book(
            id = bookId,
            title = "こころ",
            price = Price.of(1000),
            publicationStatus = PublicationStatus.PUBLISHED,
            authors = listOf(author)
        )
        
        val updateRequest = BookUpdateRequest(
            title = "吾輩は猫である",
            price = 1200,
            authors = listOf(AuthorRequest(name = "夏目漱石", birthDate = LocalDate.of(1867, 2, 9)))
        )

        `when`(bookRepository.findByIdWithAuthors(bookId)).thenReturn(existingBook)
        `when`(authorProcessor.processAuthors(updateRequest.authors)).thenReturn(listOf(author))
        `when`(bookRepository.existsByTitleAndAuthorIdsExcluding("吾輩は猫である", listOf(1L), bookId)).thenReturn(true)

        // When & Then
        val exception = assertThrows<DuplicateResourceException> {
            updateBookUseCase.execute(bookId, updateRequest)
        }
        assertEquals("書籍「吾輩は猫である」は著者「夏目漱石」で既に登録されています", exception.message)
        
        verify(bookRepository).findByIdWithAuthors(bookId)
        verify(authorProcessor).processAuthors(updateRequest.authors)
        verify(bookRepository).existsByTitleAndAuthorIdsExcluding("吾輩は猫である", listOf(1L), bookId)
    }

    @Test
    fun `複数著者の書籍更新が正常に実行される`() {
        // Given
        val bookId = 1L
        val author1 = Author(id = 1L, name = "著者1", birthDate = LocalDate.of(1900, 1, 1))
        val author2 = Author(id = 2L, name = "著者2", birthDate = LocalDate.of(1900, 2, 2))
        val existingBook = Book(
            id = bookId,
            title = "共著書籍（旧版）",
            price = Price.of(2000),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authors = listOf(author1)
        )
        
        val updateRequest = BookUpdateRequest(
            title = "共著書籍（新版）",
            price = 2500,
            authors = listOf(
                AuthorRequest(name = "著者1", birthDate = LocalDate.of(1900, 1, 1)),
                AuthorRequest(name = "著者2", birthDate = LocalDate.of(1900, 2, 2))
            )
        )
        
        val updatedBook = Book(
            id = bookId,
            title = "共著書籍（新版）",
            price = Price.of(2500),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authors = listOf(author1, author2)
        )

        `when`(bookRepository.findByIdWithAuthors(bookId)).thenReturn(existingBook)
        `when`(authorProcessor.processAuthors(updateRequest.authors)).thenReturn(listOf(author1, author2))
        `when`(bookRepository.existsByTitleAndAuthorIdsExcluding("共著書籍（新版）", listOf(1L, 2L), bookId)).thenReturn(false)
        `when`(bookRepository.findByIdWithAuthors(bookId)).thenReturn(updatedBook)

        // When
        val result = updateBookUseCase.execute(bookId, updateRequest)

        // Then
        assertEquals("共著書籍（新版）", result.title)
        assertEquals(2500, result.price)
        assertEquals(2, result.authors.size)
        
        verify(bookAuthorRepository).createRelations(bookId, listOf(1L, 2L))
    }
}
