package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.dto.AuthorUpdateRequest
import com.isaki_kaji.book_api.dto.AuthorResponse
import com.isaki_kaji.book_api.exception.ResourceNotFoundException
import com.isaki_kaji.book_api.exception.DuplicateResourceException
import com.isaki_kaji.book_api.repository.AuthorRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

/**
 * 著者更新ユースケース
 */
@Service
@Transactional
class UpdateAuthorUseCase(
    private val authorRepository: AuthorRepository
) {
    
    /**
     * 著者情報を更新する
     */
    fun execute(id: Long, request: AuthorUpdateRequest): AuthorResponse {
        // 既存著者の存在確認
        val existingAuthor = authorRepository.findById(id)
            ?: throw ResourceNotFoundException("著者が見つかりません。ID: $id")
        
        // 重複チェック（自分以外で同じ名前・生年月日の著者がいるかチェック）
        val duplicateAuthor = authorRepository.findByNameAndBirthDate(request.name, request.birthDate)
        if (duplicateAuthor != null && duplicateAuthor.id != id) {
            throw DuplicateResourceException(
                "同じ名前・生年月日の著者が既に存在します。名前: ${request.name}, 生年月日: ${request.birthDate}"
            )
        }
        
        val updatedAuthor = authorRepository.update(id, request.name, request.birthDate)
        return AuthorResponse.from(updatedAuthor)
    }
}
