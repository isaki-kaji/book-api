package com.isaki_kaji.book_api.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

/**
 * Book ドメインモデルのユニットテスト
 * 書籍作成・更新時のビジネスルールを検証する
 */
class BookTest {
    
    // テスト用の著者データ
    private val author1 = Author.create("夏目漱石", LocalDate.of(1867, 2, 9))
    private val author2 = Author.create("太宰治", LocalDate.of(1909, 6, 19))
    private val validPrice = Price.of(1000)
    
    @Test
    fun `正常な値で書籍を作成できる`() {
        // Given
        val title = "吾輩は猫である"
        val authors = listOf(author1)
        
        // When
        val book = Book.create(title, validPrice, authors, PublicationStatus.UNPUBLISHED)
        
        // Then
        assertNull(book.id)
        assertEquals("吾輩は猫である", book.title)
        assertEquals(validPrice, book.price)
        assertEquals(PublicationStatus.UNPUBLISHED, book.publicationStatus)
        assertEquals(1, book.authors.size)
        assertEquals("夏目漱石", book.authors.first().name)
    }
    
    @Test
    fun `出版状況を指定しない場合デフォルトで未出版になる`() {
        // Given
        val title = "こころ"
        val authors = listOf(author1)
        
        // When
        val book = Book.create(title, validPrice, authors)
        
        // Then
        assertEquals(PublicationStatus.UNPUBLISHED, book.publicationStatus)
    }
    
    @Test
    fun `複数の著者で書籍を作成できる`() {
        // Given
        val title = "共著書籍"
        val authors = listOf(author1, author2)
        
        // When
        val book = Book.create(title, validPrice, authors)
        
        // Then
        assertEquals(2, book.authors.size)
        assertTrue(book.authors.any { it.name == "夏目漱石" })
        assertTrue(book.authors.any { it.name == "太宰治" })
    }
    
    @Test
    fun `空のタイトルでは書籍を作成できない`() {
        // Given
        val emptyTitle = ""
        val authors = listOf(author1)
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            Book.create(emptyTitle, validPrice, authors)
        }
        
        assertTrue(exception.message!!.contains("書籍タイトルは必須です"))
    }
    
    @Test
    fun `空白のみのタイトルでは書籍を作成できない`() {
        // Given
        val blankTitle = "   "
        val authors = listOf(author1)
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            Book.create(blankTitle, validPrice, authors)
        }
        
        assertTrue(exception.message!!.contains("書籍タイトルは必須です"))
    }
    
    @Test
    fun `著者が空の場合は書籍を作成できない`() {
        // Given
        val title = "著者なし書籍"
        val emptyAuthors = emptyList<Author>()
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            Book.create(title, validPrice, emptyAuthors)
        }
        
        assertTrue(exception.message!!.contains("最低1人の著者を指定してください"))
    }
    
    @Test
    fun `書籍の基本情報を正常に更新できる`() {
        // Given
        val originalBook = Book.create("元のタイトル", validPrice, listOf(author1))
        val newTitle = "新しいタイトル"
        val newPrice = Price.of(1500)
        val newAuthors = listOf(author2)
        
        // When
        val updatedBook = originalBook.updateBasicInfo(newTitle, newPrice, newAuthors)
        
        // Then
        assertEquals(originalBook.id, updatedBook.id) // IDは変わらない
        assertEquals(originalBook.publicationStatus, updatedBook.publicationStatus) // 出版状況は変わらない
        assertEquals("新しいタイトル", updatedBook.title)
        assertEquals(newPrice, updatedBook.price)
        assertEquals(1, updatedBook.authors.size)
        assertEquals("太宰治", updatedBook.authors.first().name)
    }
    
    @Test
    fun `基本情報更新時もタイトルの前後の空白は除去される`() {
        // Given
        val originalBook = Book.create("元のタイトル", validPrice, listOf(author1))
        val titleWithSpaces = "  更新タイトル  "
        
        // When
        val updatedBook = originalBook.updateBasicInfo(titleWithSpaces, validPrice, listOf(author1))
        
        // Then
        assertEquals("更新タイトル", updatedBook.title)
    }
    
    @Test
    fun `基本情報更新時に空のタイトルは許可されない`() {
        // Given
        val originalBook = Book.create("元のタイトル", validPrice, listOf(author1))
        val emptyTitle = ""
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            originalBook.updateBasicInfo(emptyTitle, validPrice, listOf(author1))
        }
        
        assertTrue(exception.message!!.contains("書籍タイトルは必須です"))
    }
    
    @Test
    fun `基本情報更新時に著者が空の場合は許可されない`() {
        // Given
        val originalBook = Book.create("元のタイトル", validPrice, listOf(author1))
        val emptyAuthors = emptyList<Author>()
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            originalBook.updateBasicInfo("新タイトル", validPrice, emptyAuthors)
        }
        
        assertTrue(exception.message!!.contains("最低1人の著者を指定してください"))
    }
    
    @Test
    fun `未出版の書籍を出版できる`() {
        // Given
        val book = Book.create("テスト書籍", validPrice, listOf(author1), PublicationStatus.UNPUBLISHED)
        
        // When
        val publishedBook = book.publish()
        
        // Then
        assertEquals(book.id, publishedBook.id)
        assertEquals(book.title, publishedBook.title)
        assertEquals(book.price, publishedBook.price)
        assertEquals(book.authors, publishedBook.authors)
        assertEquals(PublicationStatus.PUBLISHED, publishedBook.publicationStatus)
    }
    
    @Test
    fun `出版済みの書籍を出版しても状態は変わらない（冪等性）`() {
        // Given
        val publishedBook = Book.create("出版済み書籍", validPrice, listOf(author1), PublicationStatus.PUBLISHED)
        
        // When
        val stillPublishedBook = publishedBook.publish()
        
        // Then
        assertEquals(publishedBook.id, stillPublishedBook.id)
        assertEquals(publishedBook.title, stillPublishedBook.title)
        assertEquals(publishedBook.price, stillPublishedBook.price)
        assertEquals(publishedBook.authors, stillPublishedBook.authors)
        assertEquals(PublicationStatus.PUBLISHED, stillPublishedBook.publicationStatus)
    }
}
