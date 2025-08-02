package com.isaki_kaji.book_api.domain

import java.time.LocalDate

/**
 * 著者ドメインモデル
 */
data class Author(
    val id: Long?,
    val name: String,
    val birthDate: LocalDate
) {
    companion object {
        /**
         * 新しい著者を作成する
         * 生年月日は現在日付より過去である必要がある
         */
        fun create(name: String, birthDate: LocalDate): Author {
            require(name.isNotBlank()) { "著者名は必須です" }
            require(birthDate.isBefore(LocalDate.now())) { 
                "生年月日は現在日付より過去である必要があります。入力値: $birthDate" 
            }
            
            return Author(
                id = null,
                name = name.trim(),
                birthDate = birthDate
            )
        }
    }
    
    /**
     * 著者情報を更新する
     */
    fun update(newName: String, newBirthDate: LocalDate): Author {
        require(newName.isNotBlank()) { "著者名は必須です" }
        require(newBirthDate.isBefore(LocalDate.now())) { 
            "生年月日は現在日付より過去である必要があります。入力値: $newBirthDate" 
        }
        
        return copy(
            name = newName.trim(),
            birthDate = newBirthDate
        )
    }
}
