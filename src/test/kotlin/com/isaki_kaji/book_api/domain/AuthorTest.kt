package com.isaki_kaji.book_api.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate

/**
 * Author ドメインモデルのユニットテスト
 * 著者作成・更新時のビジネスルールを検証する
 */
class AuthorTest {
    
    @Test
    fun `正常な値で著者を作成できる`() {
        // Given
        val name = "夏目漱石"
        val birthDate = LocalDate.of(1867, 2, 9)
        
        // When
        val author = Author.create(name, birthDate)
        
        // Then
        assertNull(author.id)
        assertEquals("夏目漱石", author.name)
        assertEquals(LocalDate.of(1867, 2, 9), author.birthDate)
    }
    
    @Test
    fun `著者名の前後の空白は除去される`() {
        // Given
        val nameWithSpaces = "  太宰治  "
        val birthDate = LocalDate.of(1909, 6, 19)
        
        // When
        val author = Author.create(nameWithSpaces, birthDate)
        
        // Then
        assertEquals("太宰治", author.name)
    }
    
    @Test
    fun `空の著者名では作成できない`() {
        // Given
        val emptyName = ""
        val birthDate = LocalDate.of(1900, 1, 1)
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            Author.create(emptyName, birthDate)
        }
        
        assertTrue(exception.message!!.contains("著者名は必須です"))
    }
    
    @Test
    fun `空白のみの著者名では作成できない`() {
        // Given
        val blankName = "   "
        val birthDate = LocalDate.of(1900, 1, 1)
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            Author.create(blankName, birthDate)
        }
        
        assertTrue(exception.message!!.contains("著者名は必須です"))
    }
    
    @Test
    fun `未来の生年月日では作成できない`() {
        // Given
        val name = "未来の著者"
        val futureDate = LocalDate.now().plusDays(1)
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            Author.create(name, futureDate)
        }
        
        assertTrue(exception.message!!.contains("生年月日は現在日付より過去である必要があります"))
        assertTrue(exception.message!!.contains(futureDate.toString()))
    }
    
    @Test
    fun `今日の日付では作成できない`() {
        // Given
        val name = "今日生まれ"
        val today = LocalDate.now()
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            Author.create(name, today)
        }
        
        assertTrue(exception.message!!.contains("生年月日は現在日付より過去である必要があります"))
    }
    
    @Test
    fun `昨日の日付では作成できる`() {
        // Given
        val name = "昨日生まれ"
        val yesterday = LocalDate.now().minusDays(1)
        
        // When
        val author = Author.create(name, yesterday)
        
        // Then
        assertEquals("昨日生まれ", author.name)
        assertEquals(yesterday, author.birthDate)
    }
    
    @Test
    fun `著者情報を正常に更新できる`() {
        // Given
        val originalAuthor = Author.create("元の名前", LocalDate.of(1900, 1, 1))
        val newName = "新しい名前"
        val newBirthDate = LocalDate.of(1950, 5, 15)
        
        // When
        val updatedAuthor = originalAuthor.update(newName, newBirthDate)
        
        // Then
        assertEquals(originalAuthor.id, updatedAuthor.id) // IDは変わらない
        assertEquals("新しい名前", updatedAuthor.name)
        assertEquals(LocalDate.of(1950, 5, 15), updatedAuthor.birthDate)
    }
    
    @Test
    fun `更新時も著者名の前後の空白は除去される`() {
        // Given
        val originalAuthor = Author.create("元の名前", LocalDate.of(1900, 1, 1))
        val nameWithSpaces = "  芥川龍之介  "
        val newBirthDate = LocalDate.of(1892, 3, 1)
        
        // When
        val updatedAuthor = originalAuthor.update(nameWithSpaces, newBirthDate)
        
        // Then
        assertEquals("芥川龍之介", updatedAuthor.name)
    }
    
    @Test
    fun `更新時に空の著者名は許可されない`() {
        // Given
        val originalAuthor = Author.create("元の名前", LocalDate.of(1900, 1, 1))
        val emptyName = ""
        val newBirthDate = LocalDate.of(1950, 1, 1)
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            originalAuthor.update(emptyName, newBirthDate)
        }
        
        assertTrue(exception.message!!.contains("著者名は必須です"))
    }
    
    @Test
    fun `更新時に未来の生年月日は許可されない`() {
        // Given
        val originalAuthor = Author.create("元の名前", LocalDate.of(1900, 1, 1))
        val newName = "新しい名前"
        val futureDate = LocalDate.now().plusDays(1)
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            originalAuthor.update(newName, futureDate)
        }
        
        assertTrue(exception.message!!.contains("生年月日は現在日付より過去である必要があります"))
        assertTrue(exception.message!!.contains(futureDate.toString()))
    }
}
