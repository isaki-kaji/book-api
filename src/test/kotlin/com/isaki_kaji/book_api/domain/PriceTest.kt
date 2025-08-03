package com.isaki_kaji.book_api.domain

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

/**
 * Price値オブジェクトのユニットテスト
 * クライアントが期待する振る舞いを検証する
 */
class PriceTest {
    
    @Test
    fun `正常な価格の場合、Priceオブジェクトが作成される`() {
        // Given
        val validPrice = 1000
        
        // When
        val price = Price.of(validPrice)
        
        // Then
        assertEquals(1000, price.toInt())
        assertEquals("1000", price.toString())
    }
    
    @Test
    fun `価格が0の場合、Priceオブジェクトが作成される`() {
        // Given
        val zeroPrice = 0
        
        // When
        val price = Price.of(zeroPrice)
        
        // Then
        assertEquals(0, price.toInt())
        assertEquals("0", price.toString())
    }
    
    @Test
    fun `ZERO定数を使用して価格0のオブジェクトが取得できる`() {
        // When
        val price = Price.ZERO
        
        // Then
        assertEquals(0, price.toInt())
        assertEquals("0", price.toString())
    }
    
    @Test
    fun `負の価格の場合、例外が発生する`() {
        // Given
        val invalidPrice = -100
        
        // When & Then
        val exception = assertThrows<IllegalArgumentException> {
            Price.of(invalidPrice)
        }
        
        assertTrue(exception.message!!.contains("価格は0以上である必要があります"))
        assertTrue(exception.message!!.contains("-100"))
    }
}
