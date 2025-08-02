package com.isaki_kaji.book_api.controller

import com.isaki_kaji.book_api.dto.BookCreateRequest
import com.isaki_kaji.book_api.dto.BookUpdateRequest
import com.isaki_kaji.book_api.dto.BookResponse
import com.isaki_kaji.book_api.dto.PublicationStatusUpdateRequest
import com.isaki_kaji.book_api.usecase.CreateBookUseCase
import com.isaki_kaji.book_api.usecase.UpdateBookUseCase
import com.isaki_kaji.book_api.usecase.UpdatePublicationStatusUseCase
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

/**
 * 書籍関連のRESTコントローラー
 */
@RestController
@RequestMapping("/api/books")
class BookController(
    private val createBookUseCase: CreateBookUseCase,
    private val updateBookUseCase: UpdateBookUseCase,
    private val updatePublicationStatusUseCase: UpdatePublicationStatusUseCase
) {
    
    /**
     * 書籍を作成する（著者も同時登録可能）
     * POST /api/books
     */
    @PostMapping
    fun createBook(@Valid @RequestBody request: BookCreateRequest): ResponseEntity<BookResponse> {
        val response = createBookUseCase.execute(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    /**
     * 書籍情報を更新する（タイトル、価格、著者）
     * PUT /api/books/{id}
     */
    @PutMapping("/{id}")
    fun updateBook(
        @PathVariable id: Long,
        @Valid @RequestBody request: BookUpdateRequest
    ): ResponseEntity<BookResponse> {
        val response = updateBookUseCase.execute(id, request)
        return ResponseEntity.ok(response)
    }
    
    /**
     * 出版状況を更新する
     * PATCH /api/books/{id}/publication-status
     */
    @PatchMapping("/{id}/publication-status")
    fun updatePublicationStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: PublicationStatusUpdateRequest
    ): ResponseEntity<BookResponse> {
        val response = updatePublicationStatusUseCase.execute(id, request)
        return ResponseEntity.ok(response)
    }
}
