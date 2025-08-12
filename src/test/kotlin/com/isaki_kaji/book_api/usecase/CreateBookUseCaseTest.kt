package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.domain.PublicationStatus
import com.isaki_kaji.book_api.dto.AuthorRequest
import com.isaki_kaji.book_api.dto.BookCreateRequest
import com.isaki_kaji.book_api.exception.DuplicateResourceException
import com.isaki_kaji.book_api.repository.BookAuthorRepository
import com.isaki_kaji.book_api.repository.BookRepository
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import java.time.LocalDate

/**
 * CreateBookUseCaseのユニットテスト
 * ロンドン学派のアプローチ: repositoryをモック化してビジネスロジックのみテスト
 */
@ExtendWith(MockitoExtension::class)
class CreateBookUseCaseTest {
    
    @Mock
    private lateinit var bookRepository: BookRepository
    
    @Mock
    private lateinit var bookAuthorRepository: BookAuthorRepository
    
    @Mock
    private lateinit var authorProcessor: AuthorProcessor
    
    private lateinit var createBookUseCase: CreateBookUseCase
    
    @BeforeEach
    fun setUp() {
        createBookUseCase = CreateBookUseCase(bookRepository, bookAuthorRepository, authorProcessor)
    }
    
    @Test
    fun `新しい著者と書籍を作成できる`() {
        // Given
        val request = BookCreateRequest(
            title = "テスト書籍",
            price = 1500,
            authors = listOf(
                AuthorRequest(
                    name = "テスト著者",
                    birthDate = LocalDate.of(1980, 5, 15)
                )
            ),
            publicationStatus = PublicationStatus.UNPUBLISHED
        )
        
        val processedAuthor = Author(
            id = 1L,
            name = "テスト著者",
            birthDate = LocalDate.of(1980, 5, 15)
        )
        
        val createdBook = com.isaki_kaji.book_api.domain.Book(
            id = 1L,
            title = "テスト書籍",
            price = com.isaki_kaji.book_api.domain.Price.of(1500),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authors = listOf(processedAuthor)
        )
        
        // Mock設定
        `when`(authorProcessor.processAuthors(request.authors)).thenReturn(listOf(processedAuthor))
        `when`(bookRepository.existsByTitleAndAuthorIds("テスト書籍", listOf(1L))).thenReturn(false)
        `when`(bookRepository.create(any())).thenReturn(createdBook.copy(authors = emptyList()))
        `when`(bookRepository.findByIdWithAuthors(1L)).thenReturn(createdBook)
        
        // When
        val response = createBookUseCase.execute(request)
        
        // Then
        assertEquals(1L, response.id)
        assertEquals("テスト書籍", response.title)
        assertEquals(1500, response.price)
        assertEquals(PublicationStatus.UNPUBLISHED, response.publicationStatus)
        assertEquals(1, response.authors.size)
        assertEquals("テスト著者", response.authors.first().name)
        
        // Verify interactions
        verify(authorProcessor).processAuthors(request.authors)
        verify(bookRepository).existsByTitleAndAuthorIds("テスト書籍", listOf(1L))
        verify(bookRepository).create(any())
        verify(bookAuthorRepository).createRelations(1L, listOf(1L))
        verify(bookRepository).findByIdWithAuthors(1L)
    }
    
    @Test
    fun `同じタイトルと著者の書籍を作成しようとすると重複例外が発生する`() {
        // Given
        val request = BookCreateRequest(
            title = "重複テスト書籍",
            price = 1000,
            authors = listOf(
                AuthorRequest(
                    name = "重複テスト著者",
                    birthDate = LocalDate.of(1985, 4, 1)
                )
            ),
            publicationStatus = PublicationStatus.UNPUBLISHED
        )
        
        val processedAuthor = Author(
            id = 2L,
            name = "重複テスト著者",
            birthDate = LocalDate.of(1985, 4, 1)
        )
        
        // Mock設定 - 重複が存在する
        `when`(authorProcessor.processAuthors(request.authors)).thenReturn(listOf(processedAuthor))
        `when`(bookRepository.existsByTitleAndAuthorIds("重複テスト書籍", listOf(2L))).thenReturn(true)
        
        // When & Then  
        val exception = assertThrows<DuplicateResourceException> {
            createBookUseCase.execute(request)
        }
        
        assertTrue(exception.message!!.contains("重複テスト書籍"))
        assertTrue(exception.message!!.contains("重複テスト著者"))
        
        // Verify interactions
        verify(authorProcessor).processAuthors(request.authors)
        verify(bookRepository).existsByTitleAndAuthorIds("重複テスト書籍", listOf(2L))
        verify(bookRepository, never()).create(any())
        verify(bookAuthorRepository, never()).createRelations(any(), any())
    }
    
    @Test
    fun `複数著者の書籍を作成できる`() {
        // Given
        val request = BookCreateRequest(
            title = "共著書籍",
            price = 3000,
            authors = listOf(
                AuthorRequest(name = "第一著者", birthDate = LocalDate.of(1975, 1, 10)),
                AuthorRequest(name = "第二著者", birthDate = LocalDate.of(1980, 6, 25))
            ),
            publicationStatus = PublicationStatus.PUBLISHED
        )
        
        val processedAuthors = listOf(
            Author(id = 3L, name = "第一著者", birthDate = LocalDate.of(1975, 1, 10)),
            Author(id = 4L, name = "第二著者", birthDate = LocalDate.of(1980, 6, 25))
        )
        
        val createdBook = com.isaki_kaji.book_api.domain.Book(
            id = 2L,
            title = "共著書籍",
            price = com.isaki_kaji.book_api.domain.Price.of(3000),
            publicationStatus = PublicationStatus.PUBLISHED,
            authors = processedAuthors
        )
        
        // Mock設定
        `when`(authorProcessor.processAuthors(request.authors)).thenReturn(processedAuthors)
        `when`(bookRepository.existsByTitleAndAuthorIds("共著書籍", listOf(3L, 4L))).thenReturn(false)
        `when`(bookRepository.create(any())).thenReturn(createdBook.copy(authors = emptyList()))
        `when`(bookRepository.findByIdWithAuthors(2L)).thenReturn(createdBook)
        
        // When
        val response = createBookUseCase.execute(request)
        
        // Then
        assertEquals("共著書籍", response.title)
        assertEquals(PublicationStatus.PUBLISHED, response.publicationStatus)
        assertEquals(2, response.authors.size)
        
        val authorNames = response.authors.map { it.name }.sorted()
        assertEquals(listOf("第一著者", "第二著者"), authorNames)
        
        // Verify interactions
        verify(bookAuthorRepository).createRelations(2L, listOf(3L, 4L))
    }
}
