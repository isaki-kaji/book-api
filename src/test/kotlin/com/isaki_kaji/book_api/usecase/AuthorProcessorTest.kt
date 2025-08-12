package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.dto.AuthorRequest
import com.isaki_kaji.book_api.repository.AuthorRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import kotlin.test.assertEquals

/**
 * AuthorProcessorのユニットテスト
 * ロンドン学派のアプローチ: repositoryをモック化してビジネスロジックのみテスト
 */
@ActiveProfiles("test")
@ExtendWith(MockitoExtension::class)
class AuthorProcessorTest {

    @Mock
    private lateinit var authorRepository: AuthorRepository
    
    private lateinit var authorProcessor: AuthorProcessor

    @BeforeEach
    fun setUp() {
        authorProcessor = AuthorProcessor(authorRepository)
    }

    @Test
    fun `既存著者が存在する場合は既存著者を返す`() {
        // Given
        val authorRequest = AuthorRequest(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        val existingAuthor = Author(
            id = 1L,
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        `when`(authorRepository.findByNameAndBirthDate("夏目漱石", LocalDate.of(1867, 2, 9)))
            .thenReturn(existingAuthor)

        // When
        val result = authorProcessor.processAuthors(listOf(authorRequest))

        // Then
        assertEquals(1, result.size)
        assertEquals(1L, result[0].id)
        assertEquals("夏目漱石", result[0].name)
        assertEquals(LocalDate.of(1867, 2, 9), result[0].birthDate)
        
        verify(authorRepository).findByNameAndBirthDate("夏目漱石", LocalDate.of(1867, 2, 9))
        verify(authorRepository, never()).create(any())
    }

    @Test
    fun `既存著者が存在しない場合は新規作成する`() {
        // Given
        val authorRequest = AuthorRequest(
            name = "芥川龍之介",
            birthDate = LocalDate.of(1892, 3, 1)
        )
        
        val createdAuthor = Author(
            id = 2L,
            name = "芥川龍之介",
            birthDate = LocalDate.of(1892, 3, 1)
        )
        
        `when`(authorRepository.findByNameAndBirthDate("芥川龍之介", LocalDate.of(1892, 3, 1)))
            .thenReturn(null)
        `when`(authorRepository.create(any())).thenReturn(createdAuthor)

        // When
        val result = authorProcessor.processAuthors(listOf(authorRequest))

        // Then
        assertEquals(1, result.size)
        assertEquals(2L, result[0].id)
        assertEquals("芥川龍之介", result[0].name)
        assertEquals(LocalDate.of(1892, 3, 1), result[0].birthDate)
        
        verify(authorRepository).findByNameAndBirthDate("芥川龍之介", LocalDate.of(1892, 3, 1))
        verify(authorRepository).create(any())
    }

    @Test
    fun `複数著者の混在（既存・新規）を正しく処理する`() {
        // Given
        val existingAuthorRequest = AuthorRequest(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        val newAuthorRequest = AuthorRequest(
            name = "芥川龍之介",
            birthDate = LocalDate.of(1892, 3, 1)
        )
        
        val existingAuthor = Author(
            id = 1L,
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        val createdAuthor = Author(
            id = 2L,
            name = "芥川龍之介",
            birthDate = LocalDate.of(1892, 3, 1)
        )
        
        // 既存著者は見つかる
        `when`(authorRepository.findByNameAndBirthDate("夏目漱石", LocalDate.of(1867, 2, 9)))
            .thenReturn(existingAuthor)
        // 新規著者は見つからない
        `when`(authorRepository.findByNameAndBirthDate("芥川龍之介", LocalDate.of(1892, 3, 1)))
            .thenReturn(null)
        `when`(authorRepository.create(any())).thenReturn(createdAuthor)

        // When
        val result = authorProcessor.processAuthors(listOf(existingAuthorRequest, newAuthorRequest))

        // Then
        assertEquals(2, result.size)
        
        // 既存著者のチェック
        assertEquals(1L, result[0].id)
        assertEquals("夏目漱石", result[0].name)
        
        // 新規作成著者のチェック
        assertEquals(2L, result[1].id)
        assertEquals("芥川龍之介", result[1].name)
        
        verify(authorRepository).findByNameAndBirthDate("夏目漱石", LocalDate.of(1867, 2, 9))
        verify(authorRepository).findByNameAndBirthDate("芥川龍之介", LocalDate.of(1892, 3, 1))
        verify(authorRepository, times(1)).create(any())
    }

    @Test
    fun `同じ著者リクエストが複数回来た場合でも正しく処理する`() {
        // Given
        val authorRequest1 = AuthorRequest(
            name = "村上春樹",
            birthDate = LocalDate.of(1949, 1, 12)
        )
        
        val authorRequest2 = AuthorRequest(
            name = "村上春樹",
            birthDate = LocalDate.of(1949, 1, 12)
        )
        
        val existingAuthor = Author(
            id = 3L,
            name = "村上春樹",
            birthDate = LocalDate.of(1949, 1, 12)
        )
        
        `when`(authorRepository.findByNameAndBirthDate("村上春樹", LocalDate.of(1949, 1, 12)))
            .thenReturn(existingAuthor)

        // When
        val result = authorProcessor.processAuthors(listOf(authorRequest1, authorRequest2))

        // Then
        assertEquals(2, result.size)
        assertEquals(3L, result[0].id)
        assertEquals(3L, result[1].id)
        assertEquals("村上春樹", result[0].name)
        assertEquals("村上春樹", result[1].name)
        
        // 同じ著者に対して2回検索が呼ばれる（キャッシュしていない）
        verify(authorRepository, times(2)).findByNameAndBirthDate("村上春樹", LocalDate.of(1949, 1, 12))
        verify(authorRepository, never()).create(any())
    }

    @Test
    fun `空のリストを渡した場合は空のリストが返される`() {
        // Given
        val emptyList = emptyList<AuthorRequest>()

        // When
        val result = authorProcessor.processAuthors(emptyList)

        // Then
        assertEquals(0, result.size)
        
        verify(authorRepository, never()).findByNameAndBirthDate(any(), any())
        verify(authorRepository, never()).create(any())
    }

    @Test
    fun `新規著者作成時に同じ名前でも生年月日が異なる場合は別著者として作成される`() {
        // Given
        val authorRequest1 = AuthorRequest(
            name = "田中太郎",
            birthDate = LocalDate.of(1980, 5, 15)
        )
        
        val authorRequest2 = AuthorRequest(
            name = "田中太郎",
            birthDate = LocalDate.of(1990, 5, 15) // 生年月日が異なる
        )
        
        val createdAuthor1 = Author(
            id = 4L,
            name = "田中太郎",
            birthDate = LocalDate.of(1980, 5, 15)
        )
        
        val createdAuthor2 = Author(
            id = 5L,
            name = "田中太郎",
            birthDate = LocalDate.of(1990, 5, 15)
        )
        
        `when`(authorRepository.findByNameAndBirthDate("田中太郎", LocalDate.of(1980, 5, 15)))
            .thenReturn(null)
        `when`(authorRepository.findByNameAndBirthDate("田中太郎", LocalDate.of(1990, 5, 15)))
            .thenReturn(null)
        `when`(authorRepository.create(any()))
            .thenReturn(createdAuthor1)
            .thenReturn(createdAuthor2)

        // When
        val result = authorProcessor.processAuthors(listOf(authorRequest1, authorRequest2))

        // Then
        assertEquals(2, result.size)
        assertEquals(4L, result[0].id)
        assertEquals(5L, result[1].id)
        assertEquals("田中太郎", result[0].name)
        assertEquals("田中太郎", result[1].name)
        assertEquals(LocalDate.of(1980, 5, 15), result[0].birthDate)
        assertEquals(LocalDate.of(1990, 5, 15), result[1].birthDate)
        
        verify(authorRepository).findByNameAndBirthDate("田中太郎", LocalDate.of(1980, 5, 15))
        verify(authorRepository).findByNameAndBirthDate("田中太郎", LocalDate.of(1990, 5, 15))
        verify(authorRepository, times(2)).create(any())
    }
}
