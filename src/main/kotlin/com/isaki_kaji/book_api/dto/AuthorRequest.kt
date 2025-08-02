package com.isaki_kaji.book_api.dto

import java.time.LocalDate
import jakarta.validation.constraints.*

/**
 * 書籍作成・更新時の著者情報
 */
data class AuthorRequest(
    @field:NotBlank(message = "著者名は必須です")
    @field:Size(max = 255, message = "著者名は255文字以内で入力してください")
    val name: String,
    
    @field:NotNull(message = "生年月日は必須です")
    @field:Past(message = "生年月日は現在日付より過去である必要があります")
    val birthDate: LocalDate
)
