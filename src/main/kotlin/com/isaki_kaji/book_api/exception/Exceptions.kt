package com.isaki_kaji.book_api.exception

/**
 * リソースが見つからない場合の例外
 */
class ResourceNotFoundException(message: String) : RuntimeException(message)

/**
 * 重複するデータが存在する場合の例外
 */
class DuplicateResourceException(message: String) : RuntimeException(message)

/**
 * ビジネスルール違反の例外
 */
class BusinessRuleViolationException(message: String) : RuntimeException(message)
