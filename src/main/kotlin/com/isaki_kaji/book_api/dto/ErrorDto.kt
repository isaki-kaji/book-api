package com.isaki_kaji.book_api.dto

/**
 * バリデーションエラーの詳細情報
 */
data class ValidationErrorDetail(
    val field: String,
    val message: String
)

/**
 * エラーレスポンス
 */
data class ErrorResponse(
    val error: ErrorDetail
)

/**
 * エラー詳細情報
 */
data class ErrorDetail(
    val code: String,
    val message: String,
    val details: List<ValidationErrorDetail> = emptyList()
) {
    companion object {
        fun validationError(message: String, details: List<ValidationErrorDetail>): ErrorDetail {
            return ErrorDetail(
                code = "VALIDATION_ERROR",
                message = message,
                details = details
            )
        }
        
        fun notFound(message: String): ErrorDetail {
            return ErrorDetail(
                code = "NOT_FOUND",
                message = message
            )
        }
        
        fun businessRuleViolation(message: String): ErrorDetail {
            return ErrorDetail(
                code = "BUSINESS_RULE_VIOLATION",
                message = message
            )
        }
        
        fun internalError(message: String): ErrorDetail {
            return ErrorDetail(
                code = "INTERNAL_ERROR",
                message = message
            )
        }
    }
}
