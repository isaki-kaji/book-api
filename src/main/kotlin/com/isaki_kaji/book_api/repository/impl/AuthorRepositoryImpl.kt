package com.isaki_kaji.book_api.repository.impl

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.repository.AuthorRepository
import org.example.db.tables.Authors.AUTHORS
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
        val record = dsl.insertInto(AUTHORS)
            .set(AUTHORS.NAME, author.name)
            .set(AUTHORS.BIRTH_DATE, author.birthDate)
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("著者の作成に失敗しました")
        
        return Author(
            id = record.id,
            name = record.name,
            birthDate = record.birthDate
        )
    }
    
    override fun findById(id: Long): Author? {
        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.ID.eq(id))
            .fetchOne()
            ?.let { record ->
                Author(
                    id = record.id,
                    name = record.name,
                    birthDate = record.birthDate
                )
            }
    }
    
    override fun findByNameAndBirthDate(name: String, birthDate: LocalDate): Author? {
        return dsl.selectFrom(AUTHORS)
            .where(AUTHORS.NAME.eq(name))
            .and(AUTHORS.BIRTH_DATE.eq(birthDate))
            .fetchOne()
            ?.let { record ->
                Author(
                    id = record.id,
                    name = record.name,
                    birthDate = record.birthDate
                )
            }
    }
    
    override fun update(id: Long, name: String, birthDate: LocalDate): Author {
        val record = dsl.update(AUTHORS)
            .set(AUTHORS.NAME, name)
            .set(AUTHORS.BIRTH_DATE, birthDate)
            .where(AUTHORS.ID.eq(id))
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("著者の更新に失敗しました。ID: $id")
        
        return Author(
            id = record.id,
            name = record.name,
            birthDate = record.birthDate
        )
    }
    
    override fun findBookIdsByAuthorId(authorId: Long): List<Long> {
        return dsl.select(org.example.db.tables.BookAuthors.BOOK_AUTHORS.BOOK_ID)
            .from(org.example.db.tables.BookAuthors.BOOK_AUTHORS)
            .where(org.example.db.tables.BookAuthors.BOOK_AUTHORS.AUTHOR_ID.eq(authorId))
            .fetch { it.value1() }
    }
}
