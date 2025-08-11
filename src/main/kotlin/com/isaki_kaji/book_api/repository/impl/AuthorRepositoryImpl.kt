package com.isaki_kaji.book_api.repository.impl

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.repository.AuthorRepository
import org.example.db.tables.Authors
import org.example.db.tables.BookAuthors
import org.jooq.DSLContext
import org.springframework.stereotype.Repository
import java.time.LocalDate

/**
 * 著者リポジトリの実装クラス
 */
@Repository
class AuthorRepositoryImpl(
    private val dsl: DSLContext
) : AuthorRepository {
    
    override fun create(author: Author): Author {
        val record = dsl.insertInto(Authors.AUTHORS)
            .set(Authors.AUTHORS.NAME, author.name)
            .set(Authors.AUTHORS.BIRTH_DATE, author.birthDate)
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("著者の作成に失敗しました")
        
        return Author(
            id = record.id!!,
            name = record.name!!,
            birthDate = record.birthDate
        )
    }
    
    override fun findById(id: Long): Author? {
        return dsl.selectFrom(Authors.AUTHORS)
            .where(Authors.AUTHORS.ID.eq(id))
            .fetchOne()
            ?.let { record ->
                Author(
                    id = record.id!!,
                    name = record.name!!,
                    birthDate = record.birthDate
                )
            }
    }
    
    override fun findByNameAndBirthDate(name: String, birthDate: LocalDate): Author? {
        return dsl.selectFrom(Authors.AUTHORS)
            .where(Authors.AUTHORS.NAME.eq(name))
            .and(Authors.AUTHORS.BIRTH_DATE.eq(birthDate))
            .fetchOne()
            ?.let { record ->
                Author(
                    id = record.id!!,
                    name = record.name!!,
                    birthDate = record.birthDate
                )
            }
    }
    
    override fun update(id: Long, name: String, birthDate: LocalDate): Author {
        val record = dsl.update(Authors.AUTHORS)
            .set(Authors.AUTHORS.NAME, name)
            .set(Authors.AUTHORS.BIRTH_DATE, birthDate)
            .where(Authors.AUTHORS.ID.eq(id))
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("著者の更新に失敗しました。ID: $id")
        
        return Author(
            id = record.id!!,
            name = record.name!!,
            birthDate = record.birthDate
        )
    }
    
    override fun findBookIdsByAuthorId(authorId: Long): List<Long> {
        return dsl.select(BookAuthors.BOOK_AUTHORS.BOOK_ID)
            .from(BookAuthors.BOOK_AUTHORS)
            .where(BookAuthors.BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .fetch { it.value1()!! }
    }
}
