package com.isaki_kaji.book_api.repository.impl

import com.isaki_kaji.book_api.repository.BookAuthorRepository
import org.example.db.tables.BookAuthors
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

/**
 * 書籍著者関連付けリポジトリの実装クラス
 */
@Repository
class BookAuthorRepositoryImpl(
    private val dsl: DSLContext
) : BookAuthorRepository {
    
    override fun createRelations(bookId: Long, authorIds: List<Long>) {
        if (authorIds.isEmpty()) return
        
        val batch = authorIds.map { authorId ->
            dsl.insertInto(BookAuthors.BOOK_AUTHORS)
                .set(BookAuthors.BOOK_AUTHORS.BOOK_ID, bookId)
                .set(BookAuthors.BOOK_AUTHORS.AUTHOR_ID, authorId)
        }
        
        dsl.batch(batch).execute()
    }
    
    override fun deleteByBookId(bookId: Long) {
        dsl.deleteFrom(BookAuthors.BOOK_AUTHORS)
            .where(BookAuthors.BOOK_AUTHORS.BOOK_ID.eq(bookId))
            .execute()
    }
}
