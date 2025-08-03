package com.isaki_kaji.book_api.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

/**
 * PublicationStatus enumのユニットテスト
 * 書籍出版のビジネスルールを検証する
 */
class PublicationStatusTest {
    
    @Test
    fun `未出版の書籍を出版すると出版済みになる`() {
        // Given
        val unpublishedStatus = PublicationStatus.UNPUBLISHED
        
        // When
        val publishedStatus = unpublishedStatus.publish()
        
        // Then
        assertEquals(PublicationStatus.PUBLISHED, publishedStatus)
    }
    
    @Test
    fun `出版済みの書籍を出版しても出版済みのまま（冪等性）`() {
        // Given
        val publishedStatus = PublicationStatus.PUBLISHED
        
        // When
        val stillPublishedStatus = publishedStatus.publish()
        
        // Then
        assertEquals(PublicationStatus.PUBLISHED, stillPublishedStatus)
    }
}
