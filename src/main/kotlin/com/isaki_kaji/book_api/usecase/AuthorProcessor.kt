package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.dto.AuthorRequest
import com.isaki_kaji.book_api.repository.AuthorRepository
import org.springframework.stereotype.Component

/**
 * 著者処理の共通ロジック
 */
@Component
class AuthorProcessor(
    private val authorRepository: AuthorRepository
) {
    
    /**
     * 著者リクエストを処理し、重複チェック + 新規作成 or 既存使用を行う
     */
    fun processAuthors(authorRequests: List<AuthorRequest>): List<Author> {
        return authorRequests.map { authorRequest ->
            // 名前と生年月日で既存著者をチェック
            val existingAuthor = authorRepository.findByNameAndBirthDate(
                authorRequest.name,
                authorRequest.birthDate
            )
            
            if (existingAuthor != null) {
                // 既存著者を使用
                existingAuthor
            } else {
                // 新規著者を作成
                val newAuthor = Author.create(authorRequest.name, authorRequest.birthDate)
                authorRepository.create(newAuthor)
            }
        }
    }
}
