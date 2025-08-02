package com.isaki_kaji.book_api.dto

import com.isaki_kaji.book_api.domain.Author
import java.time.LocalDate
import jakarta.validation.constraints.*

/**
 * 著者作成リクエスト
 */
data class AuthorCreateRequest(
    @field:NotBlank(message = "著者名は必須です")
    @field:Size(max = 255, message = "著者名は255文字以内で入力してください")
    val name: String,
    
    @field:NotNull(message = "生年月日は必須です")
    @field:Past(message = "生年月日は現在日付より過去である必要があります")
    val birthDate: LocalDate
) {
    fun toDomain(): Author = Author.create(name, birthDate)
}
