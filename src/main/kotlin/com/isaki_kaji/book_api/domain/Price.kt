package com.isaki_kaji.book_api.domain

/**
 * 価格を表す値オブジェクト
 * 0以上の値のみを許可する
 */
@JvmInline
value class Price private constructor(private val value: Int) {
    
    companion object {
        fun of(value: Int): Price {
            require(value >= 0) { "価格は0以上である必要があります。入力値: $value" }
            return Price(value)
        }
        
        val ZERO = Price(0)
    }
    
    fun toInt(): Int = value
    
    override fun toString(): String = value.toString()
}
