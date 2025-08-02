package com.isaki_kaji.book_api.controller

import com.isaki_kaji.book_api.dto.AuthorCreateRequest
import com.isaki_kaji.book_api.dto.AuthorUpdateRequest
import com.isaki_kaji.book_api.dto.AuthorResponse
import com.isaki_kaji.book_api.dto.AuthorBooksResponse
import com.isaki_kaji.book_api.usecase.CreateAuthorUseCase
import com.isaki_kaji.book_api.usecase.UpdateAuthorUseCase
import com.isaki_kaji.book_api.usecase.GetAuthorBooksUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 著者関連のRESTコントローラー
 */
@RestController
@RequestMapping("/api/authors")
class AuthorController(
    private val createAuthorUseCase: CreateAuthorUseCase,
    private val updateAuthorUseCase: UpdateAuthorUseCase,
    private val getAuthorBooksUseCase: GetAuthorBooksUseCase
) {
    
    /**
     * 著者を作成する
     * POST /api/authors
     */
    @PostMapping
    fun createAuthor(@Valid @RequestBody request: AuthorCreateRequest): ResponseEntity<AuthorResponse> {
        val response = createAuthorUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    /**
     * 著者情報を更新する
     * PUT /api/authors/{id}
     */
    @PutMapping("/{id}")
    fun updateAuthor(
        @PathVariable id: Long,
        @Valid @RequestBody request: AuthorUpdateRequest
    ): ResponseEntity<AuthorResponse> {
        val response = updateAuthorUseCase.execute(id, request)
        return ResponseEntity.ok(response)
    }
    
    /**
     * 著者に紐づく書籍一覧を取得する
     * GET /api/authors/{id}/books
     */
    @GetMapping("/{id}/books")
    fun getAuthorBooks(@PathVariable id: Long): ResponseEntity<AuthorBooksResponse> {
        val response = getAuthorBooksUseCase.execute(id)
        return ResponseEntity.ok(response)
    }
}
