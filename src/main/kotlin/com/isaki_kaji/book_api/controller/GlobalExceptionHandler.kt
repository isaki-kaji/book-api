package com.isaki_kaji.book_api.controller

import com.isaki_kaji.book_api.dto.ErrorDetail
import com.isaki_kaji.book_api.dto.ErrorResponse
import com.isaki_kaji.book_api.dto.ValidationErrorDetail
import com.isaki_kaji.book_api.exception.BusinessRuleViolationException
import com.isaki_kaji.book_api.exception.DuplicateResourceException
import com.isaki_kaji.book_api.exception.ResourceNotFoundException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

/**
 * グローバル例外ハンドラー
 */
@RestControllerAdvice
class GlobalExceptionHandler {
    
    private val logger = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    
    /**
     * バリデーションエラーのハンドリング
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        logger.warn("Validation error: {}", ex.message)
        
        val details = ex.bindingResult.fieldErrors.map { fieldError ->
            ValidationErrorDetail(
                field = fieldError.field,
                message = fieldError.defaultMessage ?: "入力値が不正です"
            )
        }
        
        val errorDetail = ErrorDetail.validationError("バリデーションエラーが発生しました", details)
        return ResponseEntity.badRequest().body(ErrorResponse(errorDetail))
    }
    
    /**
     * リソースが見つからない場合のハンドリング
     */
    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleResourceNotFoundException(ex: ResourceNotFoundException): ResponseEntity<ErrorResponse> {
        logger.warn("Resource not found: {}", ex.message)
        
        val errorDetail = ErrorDetail.notFound(ex.message ?: "リソースが見つかりません")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErrorResponse(errorDetail))
    }
    
    /**
     * 重複リソースエラーのハンドリング
     */
    @ExceptionHandler(DuplicateResourceException::class)
    fun handleDuplicateResourceException(ex: DuplicateResourceException): ResponseEntity<ErrorResponse> {
        logger.warn("Duplicate resource: {}", ex.message)
        
        val errorDetail = ErrorDetail.validationError(
            ex.message ?: "重複するリソースが存在します", 
            emptyList()
        )
        return ResponseEntity.badRequest().body(ErrorResponse(errorDetail))
    }
    
    /**
     * ビジネスルール違反のハンドリング
     */
    @ExceptionHandler(BusinessRuleViolationException::class)
    fun handleBusinessRuleViolationException(ex: BusinessRuleViolationException): ResponseEntity<ErrorResponse> {
        logger.warn("Business rule violation: {}", ex.message)
        
        val errorDetail = ErrorDetail.businessRuleViolation(ex.message ?: "ビジネスルール違反です")
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(ErrorResponse(errorDetail))
    }
    
    /**
     * その他の予期しないエラーのハンドリング
     */
    @ExceptionHandler(Exception::class)
    fun handleGenericException(ex: Exception): ResponseEntity<ErrorResponse> {
        logger.error("Unexpected error occurred", ex)
        
        val errorDetail = ErrorDetail.internalError("サーバー内部エラーが発生しました")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorResponse(errorDetail))
    }
}
