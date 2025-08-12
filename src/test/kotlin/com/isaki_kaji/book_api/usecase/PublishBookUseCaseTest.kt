package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.domain.Book
import com.isaki_kaji.book_api.domain.Price
import com.isaki_kaji.book_api.domain.PublicationStatus
import com.isaki_kaji.book_api.exception.ResourceNotFoundException
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
import java.time.LocalDate

/**
 * PublishBookUseCaseのユニットテスト
 * ロンドン学派のアプローチ: repositoryをモック化してビジネスロジックのみテスト
 */
@ExtendWith(MockitoExtension::class)
class PublishBookUseCaseTest {
    
    @Mock
    private lateinit var bookRepository: BookRepository
    
    private lateinit var publishBookUseCase: PublishBookUseCase
    
    @BeforeEach
    fun setUp() {
        publishBookUseCase = PublishBookUseCase(bookRepository)
    }
    
    @Test
    fun `未出版の書籍を出版できる`() {
        // Given
        val bookId = 1L
        val author = Author(
            id = 1L,
            name = "テスト著者",
            birthDate = LocalDate.of(1980, 5, 15)
        )
        
        val unpublishedBook = Book(
            id = bookId,
            title = "未出版書籍",
            price = Price.of(1500),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authors = listOf(author)
        )
        
        val publishedBook = unpublishedBook.copy(publicationStatus = PublicationStatus.PUBLISHED)
        
        // Mock設定
        `when`(bookRepository.findByIdWithAuthors(bookId)).thenReturn(unpublishedBook, publishedBook)
        
        // When
        val response = publishBookUseCase.execute(bookId)
        
        // Then
        assertEquals(bookId, response.id)
        assertEquals("未出版書籍", response.title)
        assertEquals(1500, response.price)
        assertEquals(PublicationStatus.PUBLISHED, response.publicationStatus)
        assertEquals("テスト著者", response.authors.first().name)
        
        // Verify interactions
        verify(bookRepository, times(2)).findByIdWithAuthors(bookId)
        verify(bookRepository).updatePublicationStatus(bookId, PublicationStatus.PUBLISHED)
    }
    
    @Test
    fun `出版済みの書籍を出版しても状態は変わらない（冪等性）`() {
        // Given
        val bookId = 2L
        val author = Author(
            id = 2L,
            name = "出版済み著者",
            birthDate = LocalDate.of(1975, 8, 20)
        )
        
        val publishedBook = Book(
            id = bookId,
            title = "出版済み書籍",
            price = Price.of(2000),
            publicationStatus = PublicationStatus.PUBLISHED,
            authors = listOf(author)
        )
        
        // Mock設定 - 既に出版済み
        `when`(bookRepository.findByIdWithAuthors(bookId)).thenReturn(publishedBook)
        
        // When
        val response = publishBookUseCase.execute(bookId)
        
        // Then
        assertEquals(bookId, response.id)
        assertEquals("出版済み書籍", response.title)
        assertEquals(2000, response.price)
        assertEquals(PublicationStatus.PUBLISHED, response.publicationStatus)
        assertEquals("出版済み著者", response.authors.first().name)
        
        // Verify interactions - 冪等性のため、常にPUBLISHEDに設定
        verify(bookRepository, times(2)).findByIdWithAuthors(bookId)
        verify(bookRepository).updatePublicationStatus(bookId, PublicationStatus.PUBLISHED)
    }
    
    @Test
    fun `存在しない書籍IDで出版しようとすると例外が発生する`() {
        // Given
        val nonExistentBookId = 99999L
        
        // Mock設定 - 書籍が見つからない
        `when`(bookRepository.findByIdWithAuthors(nonExistentBookId)).thenReturn(null)
        
        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            publishBookUseCase.execute(nonExistentBookId)
        }
        
        assertTrue(exception.message!!.contains("書籍が見つかりません"))
        assertTrue(exception.message!!.contains(nonExistentBookId.toString()))
        
        // Verify interactions
        verify(bookRepository).findByIdWithAuthors(nonExistentBookId)
        verify(bookRepository, never()).updatePublicationStatus(any(), any())
    }
    
    @Test
    fun `複数著者の書籍を出版できる`() {
        // Given
        val bookId = 3L
        val author1 = Author(
            id = 3L,
            name = "第一著者",
            birthDate = LocalDate.of(1970, 1, 1)
        )
        val author2 = Author(
            id = 4L,
            name = "第二著者",
            birthDate = LocalDate.of(1980, 2, 2)
        )
        
        val unpublishedBook = Book(
            id = bookId,
            title = "共同執筆書籍",
            price = Price.of(3000),
            publicationStatus = PublicationStatus.UNPUBLISHED,
            authors = listOf(author1, author2)
        )
        
        val publishedBook = unpublishedBook.copy(publicationStatus = PublicationStatus.PUBLISHED)
        
        // Mock設定
        `when`(bookRepository.findByIdWithAuthors(bookId)).thenReturn(unpublishedBook, publishedBook)
        
        // When
        val response = publishBookUseCase.execute(bookId)
        
        // Then
        assertEquals("共同執筆書籍", response.title)
        assertEquals(PublicationStatus.PUBLISHED, response.publicationStatus)
        assertEquals(2, response.authors.size)
        
        val authorNames = response.authors.map { it.name }.sorted()
        assertEquals(listOf("第一著者", "第二著者"), authorNames)
        
        // Verify interactions
        verify(bookRepository).updatePublicationStatus(bookId, PublicationStatus.PUBLISHED)
    }
}
