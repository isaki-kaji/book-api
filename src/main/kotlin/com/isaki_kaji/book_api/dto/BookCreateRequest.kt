package com.isaki_kaji.book_api.dto

import com.isaki_kaji.book_api.domain.Price
import com.isaki_kaji.book_api.domain.PublicationStatus
import jakarta.validation.Valid
import jakarta.validation.constraints.*

/**
 * 書籍作成リクエスト
 */
data class BookCreateRequest(
    @field:NotBlank(message = "書籍タイトルは必須です")
    @field:Size(max = 255, message = "書籍タイトルは255文字以内で入力してください")
    val title: String,
    
    @field:NotNull(message = "価格は必須です")
    @field:Min(value = 0, message = "価格は0以上である必要があります")
    val price: Int,
    
    @field:Valid
    @field:NotEmpty(message = "最低1人の著者を指定してください")
    val authors: List<AuthorRequest>,
    
    @field:NotNull(message = "出版状況は必須です")
    val publicationStatus: PublicationStatus = PublicationStatus.UNPUBLISHED
) {
    fun getPrice(): Price = Price.of(price)
}
