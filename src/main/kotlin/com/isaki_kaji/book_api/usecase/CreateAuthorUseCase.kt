package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.dto.AuthorCreateRequest
import com.isaki_kaji.book_api.dto.AuthorResponse
import com.isaki_kaji.book_api.exception.DuplicateResourceException
import com.isaki_kaji.book_api.repository.AuthorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 著者作成ユースケース
 */
@Service
@Transactional
class CreateAuthorUseCase(
    private val authorRepository: AuthorRepository
) {
    
    /**
     * 著者を作成する
     */
    fun execute(request: AuthorCreateRequest): AuthorResponse {
        // 重複チェック
        val existingAuthor = authorRepository.findByNameAndBirthDate(request.name, request.birthDate)
        if (existingAuthor != null) {
            throw DuplicateResourceException(
                "同じ名前・生年月日の著者が既に存在します。名前: ${request.name}, 生年月日: ${request.birthDate}"
            )
        }
        
        val author = request.toDomain()
        val createdAuthor = authorRepository.create(author)
        return AuthorResponse.from(createdAuthor)
    }
}
