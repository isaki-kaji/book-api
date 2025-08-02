package com.isaki_kaji.book_api.dto

import com.isaki_kaji.book_api.domain.PublicationStatus
import jakarta.validation.constraints.*

/**
 * 出版状況更新リクエスト
 */
data class PublicationStatusUpdateRequest(
    @field:NotNull(message = "出版状況は必須です")
    val publicationStatus: PublicationStatus
)
