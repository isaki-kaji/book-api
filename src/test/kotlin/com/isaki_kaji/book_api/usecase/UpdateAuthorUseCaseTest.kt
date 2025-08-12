package com.isaki_kaji.book_api.usecase

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.dto.AuthorUpdateRequest
import com.isaki_kaji.book_api.exception.DuplicateResourceException
import com.isaki_kaji.book_api.exception.ResourceNotFoundException
import com.isaki_kaji.book_api.repository.AuthorRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.springframework.test.context.ActiveProfiles
import java.time.LocalDate
import kotlin.test.assertEquals

@ActiveProfiles("test")
@ExtendWith(MockitoExtension::class)
class UpdateAuthorUseCaseTest {

    @Mock
    private lateinit var authorRepository: AuthorRepository
    
    private lateinit var updateAuthorUseCase: UpdateAuthorUseCase

    @BeforeEach
    fun setUp() {
        updateAuthorUseCase = UpdateAuthorUseCase(authorRepository)
    }

    @Test
    fun `著者更新が正常に実行される`() {
        // Given
        val authorId = 1L
        val updateRequest = AuthorUpdateRequest(
            name = "夏目漱石（更新）",
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        val existingAuthor = Author(
            id = 1L,
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        val updatedAuthor = Author(
            id = 1L,
            name = "夏目漱石（更新）",
            birthDate = LocalDate.of(1867, 2, 9)
        )

        `when`(authorRepository.findById(authorId)).thenReturn(existingAuthor)
        `when`(authorRepository.findByNameAndBirthDate("夏目漱石（更新）", LocalDate.of(1867, 2, 9))).thenReturn(null)
        `when`(authorRepository.update(authorId, "夏目漱石（更新）", LocalDate.of(1867, 2, 9))).thenReturn(updatedAuthor)

        // When
        val result = updateAuthorUseCase.execute(authorId, updateRequest)

        // Then
        assertEquals(1L, result.id)
        assertEquals("夏目漱石（更新）", result.name)
        assertEquals(LocalDate.of(1867, 2, 9), result.birthDate)
        
        verify(authorRepository).findById(authorId)
        verify(authorRepository).findByNameAndBirthDate("夏目漱石（更新）", LocalDate.of(1867, 2, 9))
        verify(authorRepository).update(authorId, "夏目漱石（更新）", LocalDate.of(1867, 2, 9))
    }

    @Test
    fun `存在しない著者IDで更新を試みた場合に例外が発生する`() {
        // Given
        val nonExistentId = 999L
        val updateRequest = AuthorUpdateRequest(
            name = "存在しない著者",
            birthDate = LocalDate.of(1900, 1, 1)
        )

        `when`(authorRepository.findById(nonExistentId)).thenReturn(null)

        // When & Then
        val exception = assertThrows<ResourceNotFoundException> {
            updateAuthorUseCase.execute(nonExistentId, updateRequest)
        }
        assertEquals("著者が見つかりません。ID: $nonExistentId", exception.message)
        
        verify(authorRepository).findById(nonExistentId)
        verify(authorRepository, never()).findByNameAndBirthDate(any(), any())
        verify(authorRepository, never()).update(any(), any(), any())
    }

    @Test
    fun `同じ名前・生年月日の別の著者が存在する場合に例外が発生する`() {
        // Given
        val authorId = 1L
        val updateRequest = AuthorUpdateRequest(
            name = "森鴎外",
            birthDate = LocalDate.of(1862, 1, 19)
        )
        
        val existingAuthor = Author(
            id = 1L,
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        val duplicateAuthor = Author(
            id = 2L,
            name = "森鴎外",
            birthDate = LocalDate.of(1862, 1, 19)
        )

        `when`(authorRepository.findById(authorId)).thenReturn(existingAuthor)
        `when`(authorRepository.findByNameAndBirthDate("森鴎外", LocalDate.of(1862, 1, 19))).thenReturn(duplicateAuthor)

        // When & Then
        val exception = assertThrows<DuplicateResourceException> {
            updateAuthorUseCase.execute(authorId, updateRequest)
        }
        assertEquals(
            "同じ名前・生年月日の著者が既に存在します。名前: 森鴎外, 生年月日: 1862-01-19",
            exception.message
        )
        
        verify(authorRepository).findById(authorId)
        verify(authorRepository).findByNameAndBirthDate("森鴎外", LocalDate.of(1862, 1, 19))
        verify(authorRepository, never()).update(any(), any(), any())
    }

    @Test
    fun `自分自身と同じ名前・生年月日で更新する場合は正常に実行される`() {
        // Given
        val authorId = 1L
        val updateRequest = AuthorUpdateRequest(
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        val existingAuthor = Author(
            id = 1L,
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9)
        )
        
        val updatedAuthor = Author(
            id = 1L,
            name = "夏目漱石",
            birthDate = LocalDate.of(1867, 2, 9)
        )

        `when`(authorRepository.findById(authorId)).thenReturn(existingAuthor)
        `when`(authorRepository.findByNameAndBirthDate("夏目漱石", LocalDate.of(1867, 2, 9))).thenReturn(existingAuthor)
        `when`(authorRepository.update(authorId, "夏目漱石", LocalDate.of(1867, 2, 9))).thenReturn(updatedAuthor)

        // When
        val result = updateAuthorUseCase.execute(authorId, updateRequest)

        // Then
        assertEquals(1L, result.id)
        assertEquals("夏目漱石", result.name)
        assertEquals(LocalDate.of(1867, 2, 9), result.birthDate)
        
        verify(authorRepository).findById(authorId)
        verify(authorRepository).findByNameAndBirthDate("夏目漱石", LocalDate.of(1867, 2, 9))
        verify(authorRepository).update(authorId, "夏目漱石", LocalDate.of(1867, 2, 9))
    }
}