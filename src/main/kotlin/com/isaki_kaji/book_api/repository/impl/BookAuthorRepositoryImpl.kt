package com.isaki_kaji.book_api.repository.impl

import com.isaki_kaji.book_api.repository.BookAuthorRepository
import org.example.db.tables.BookAuthors.BOOK_AUTHORS
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

/**
 * 書籍著者関連リポジトリの実装クラス
 */
@Repository
class BookAuthorRepositoryImpl(
    private val dsl: DSLContext
) : BookAuthorRepository {
    
    override fun createRelations(bookId: Long, authorIds: List<Long>) {
        val queries = authorIds.map { authorId ->
            dsl.insertInto(BOOK_AUTHORS)
                .set(BOOK_AUTHORS.BOOK_ID, bookId)
                .set(BOOK_AUTHORS.AUTHOR_ID, authorId)
        }
        
        dsl.batch(queries).execute()
    }
    
    override fun deleteByBookId(bookId: Long) {
        dsl.deleteFrom(BOOK_AUTHORS)
            .where(BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .execute()
    }
}
