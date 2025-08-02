package com.isaki_kaji.book_api.dto

import com.isaki_kaji.book_api.domain.Price
import jakarta.validation.Valid
import jakarta.validation.constraints.*

/**
 * 書籍更新リクエスト
 */
data class BookUpdateRequest(
    @field:NotBlank(message = "書籍タイトルは必須です")
    @field:Size(max = 255, message = "書籍タイトルは255文字以内で入力してください")
    val title: String,
    
    @field:NotNull(message = "価格は必須です")
    @field:Min(value = 0, message = "価格は0以上である必要があります")
    val price: Int,
    
    @field:Valid
    @field:NotEmpty(message = "最低1人の著者を指定してください")
    val authors: List<AuthorRequest>
) {
    fun getPrice(): Price = Price.of(price)
}
