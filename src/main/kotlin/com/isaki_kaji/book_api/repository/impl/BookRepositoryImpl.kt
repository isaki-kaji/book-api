package com.isaki_kaji.book_api.repository.impl

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.domain.Book
import com.isaki_kaji.book_api.domain.Price
import com.isaki_kaji.book_api.domain.PublicationStatus
import com.isaki_kaji.book_api.repository.BookRepository
import org.example.db.tables.Authors
import org.example.db.tables.BookAuthors
import org.example.db.tables.Books
import org.jooq.DSLContext
import org.springframework.stereotype.Repository

/**
 * 書籍リポジトリの実装クラス
 */
@Repository
class BookRepositoryImpl(
    private val dsl: DSLContext
) : BookRepository {
    
    override fun create(book: Book): Book {
        val record = dsl.insertInto(Books.BOOKS)
            .set(Books.BOOKS.TITLE, book.title)
            .set(Books.BOOKS.PRICE, book.price.toInt())
            .set(Books.BOOKS.PUBLICATION_STATUS, book.publicationStatus.ordinal.toShort())
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("書籍の作成に失敗しました")
        
        return Book(
            id = record.id!!,
            title = record.title!!,
            price = Price.of(record.price!!),
            publicationStatus = PublicationStatus.entries[record.publicationStatus!!.toInt()]
        )
    }
    
    override fun findByIdWithAuthors(id: Long): Book? {
        val result = dsl
            .select(
                Books.BOOKS.ID,
                Books.BOOKS.TITLE,
                Books.BOOKS.PRICE,
                Books.BOOKS.PUBLICATION_STATUS,
                Authors.AUTHORS.ID,
                Authors.AUTHORS.NAME,
                Authors.AUTHORS.BIRTH_DATE
            )
            .from(Books.BOOKS)
            .leftJoin(BookAuthors.BOOK_AUTHORS).on(Books.BOOKS.ID.eq(BookAuthors.BOOK_AUTHORS.BOOK_ID))
            .leftJoin(Authors.AUTHORS).on(BookAuthors.BOOK_AUTHORS.AUTHOR_ID.eq(Authors.AUTHORS.ID))
            .where(Books.BOOKS.ID.eq(id))
            .fetch()
        
        if (result.isEmpty()) {
            return null
        }
        
        val bookRecord = result.first()
        val authors = result.mapNotNull { record ->
            val authorId = record.get(Authors.AUTHORS.ID)
            if (authorId != null) {
                Author(
                    id = authorId,
                    name = record.get(Authors.AUTHORS.NAME)!!,
                    birthDate = record.get(Authors.AUTHORS.BIRTH_DATE)
                )
            } else null
        }.distinctBy { it.id }
        
        return Book(
            id = bookRecord.get(Books.BOOKS.ID)!!,
            title = bookRecord.get(Books.BOOKS.TITLE)!!,
            price = Price.of(bookRecord.get(Books.BOOKS.PRICE)!!),
            publicationStatus = PublicationStatus.entries[bookRecord.get(Books.BOOKS.PUBLICATION_STATUS)!!.toInt()],
            authors = authors
        )
    }
    
    override fun updateBasicInfo(id: Long, title: String, price: Price): Book {
        val record = dsl.update(Books.BOOKS)
            .set(Books.BOOKS.TITLE, title)
            .set(Books.BOOKS.PRICE, price.toInt())
            .where(Books.BOOKS.ID.eq(id))
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("書籍の更新に失敗しました。ID: $id")
        
        return Book(
            id = record.id!!,
            title = record.title!!,
            price = Price.of(record.price!!),
            publicationStatus = PublicationStatus.entries[record.publicationStatus!!.toInt()]
        )
    }
    
    override fun updatePublicationStatus(id: Long, publicationStatus: PublicationStatus): Book {
        val record = dsl.update(Books.BOOKS)
            .set(Books.BOOKS.PUBLICATION_STATUS, publicationStatus.ordinal.toShort())
            .where(Books.BOOKS.ID.eq(id))
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("書籍の更新に失敗しました。ID: $id")
        
        return Book(
            id = record.id!!,
            title = record.title!!,
            price = Price.of(record.price!!),
            publicationStatus = PublicationStatus.entries[record.publicationStatus!!.toInt()]
        )
    }
    
    override fun findByIds(ids: List<Long>): List<Book> {
        if (ids.isEmpty()) return emptyList()
        
        return dsl.selectFrom(Books.BOOKS)
            .where(Books.BOOKS.ID.`in`(ids))
            .fetch()
            .map { record ->
                Book(
                    id = record.id!!,
                    title = record.title!!,
                    price = Price.of(record.price!!),
                    publicationStatus = PublicationStatus.entries[record.publicationStatus!!.toInt()]
                )
            }
    }
    
    override fun existsByTitleAndAuthorIds(title: String, authorIds: List<Long>): Boolean {
        if (authorIds.isEmpty()) {
            return false
        }
        
        val booksWithTitle = dsl.select(Books.BOOKS.ID)
            .from(Books.BOOKS)
            .where(Books.BOOKS.TITLE.eq(title))
            .fetch()
            .map { it.get(Books.BOOKS.ID)!! }
        
        if (booksWithTitle.isEmpty()) {
            return false
        }
        
        val authorIdsSet = authorIds.toSet()
        for (bookId in booksWithTitle) {
            val bookAuthorIds = dsl.select(BookAuthors.BOOK_AUTHORS.AUTHOR_ID)
                .from(BookAuthors.BOOK_AUTHORS)
                .where(BookAuthors.BOOK_AUTHORS.BOOK_ID.eq(bookId))
                .fetch()
                .map { it.get(BookAuthors.BOOK_AUTHORS.AUTHOR_ID)!! }
                .toSet()
            
            if (bookAuthorIds == authorIdsSet) {
                return true
            }
        }
        
        return false
    }
    
    override fun existsByTitleAndAuthorIdsExcluding(title: String, authorIds: List<Long>, excludeBookId: Long): Boolean {
        if (authorIds.isEmpty()) {
            return false
        }
        
        val booksWithTitle = dsl.select(Books.BOOKS.ID)
            .from(Books.BOOKS)
            .where(Books.BOOKS.TITLE.eq(title))
            .and(Books.BOOKS.ID.ne(excludeBookId))
            .fetch()
            .map { it.get(Books.BOOKS.ID)!! }
        
        if (booksWithTitle.isEmpty()) {
            return false
        }
        
        val authorIdsSet = authorIds.toSet()
        for (bookId in booksWithTitle) {
            val bookAuthorIds = dsl.select(BookAuthors.BOOK_AUTHORS.AUTHOR_ID)
                .from(BookAuthors.BOOK_AUTHORS)
                .where(BookAuthors.BOOK_AUTHORS.BOOK_ID.eq(bookId))
                .fetch()
                .map { it.get(BookAuthors.BOOK_AUTHORS.AUTHOR_ID)!! }
                .toSet()
            
            if (bookAuthorIds == authorIdsSet) {
                return true
            }
        }
        
        return false
    }
}
