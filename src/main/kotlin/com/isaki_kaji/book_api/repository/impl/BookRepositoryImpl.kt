package com.isaki_kaji.book_api.repository.impl

import com.isaki_kaji.book_api.domain.Author
import com.isaki_kaji.book_api.domain.Book
import com.isaki_kaji.book_api.domain.Price
import com.isaki_kaji.book_api.domain.PublicationStatus
import com.isaki_kaji.book_api.repository.BookRepository
import org.example.db.tables.Authors.AUTHORS
import org.example.db.tables.BookAuthors.BOOK_AUTHORS
import org.example.db.tables.Books.BOOKS
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
        val record = dsl.insertInto(BOOKS)
            .set(BOOKS.TITLE, book.title)
            .set(BOOKS.PRICE, book.price.toInt())
            .set(BOOKS.PUBLICATION_STATUS, book.publicationStatus.ordinal.toShort())
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("書籍の作成に失敗しました")
        
        return Book(
            id = record.id,
            title = record.title,
            price = Price.of(record.price),
            publicationStatus = PublicationStatus.entries[record.publicationStatus.toInt()]
        )
    }
    
    override fun findByIdWithAuthors(id: Long): Book? {
        val result = dsl.select(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLICATION_STATUS,
            AUTHORS.ID,
            AUTHORS.NAME,
            AUTHORS.BIRTH_DATE
        )
            .from(BOOKS)
            .leftJoin(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
            .leftJoin(AUTHORS).on(BOOK_AUTHORS.AUTHOR_ID.eq(AUTHORS.ID))
            .where(BOOKS.ID.eq(id))
            .fetch()
        
        if (result.isEmpty()) return null
        
        val firstRecord = result.first()
        val authors = result.mapNotNull { record ->
            if (record.get(AUTHORS.ID) != null) {
                Author(
                    id = record.get(AUTHORS.ID),
                    name = record.get(AUTHORS.NAME),
                    birthDate = record.get(AUTHORS.BIRTH_DATE)
                )
            } else null
        }.distinctBy { it.id }
        
        return Book(
            id = firstRecord.get(BOOKS.ID),
            title = firstRecord.get(BOOKS.TITLE),
            price = Price.of(firstRecord.get(BOOKS.PRICE)),
            publicationStatus = PublicationStatus.entries[firstRecord.get(BOOKS.PUBLICATION_STATUS).toInt()],
            authors = authors
        )
    }
    
    override fun updateBasicInfo(id: Long, title: String, price: Price): Book {
        val record = dsl.update(BOOKS)
            .set(BOOKS.TITLE, title)
            .set(BOOKS.PRICE, price.toInt())
            .where(BOOKS.ID.eq(id))
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("書籍の更新に失敗しました。ID: $id")
        
        return Book(
            id = record.id,
            title = record.title,
            price = Price.of(record.price),
            publicationStatus = PublicationStatus.entries[record.publicationStatus.toInt()]
        )
    }
    
    override fun updatePublicationStatus(id: Long, publicationStatus: PublicationStatus): Book {
        val record = dsl.update(BOOKS)
            .set(BOOKS.PUBLICATION_STATUS, publicationStatus.ordinal.toShort())
            .where(BOOKS.ID.eq(id))
            .returning()
            .fetchOne()
            ?: throw IllegalStateException("出版状況の更新に失敗しました。ID: $id")
        
        return Book(
            id = record.id,
            title = record.title,
            price = Price.of(record.price),
            publicationStatus = PublicationStatus.entries[record.publicationStatus.toInt()]
        )
    }
    
    override fun findByIds(ids: List<Long>): List<Book> {
        if (ids.isEmpty()) return emptyList()
        
        val result = dsl.select(
            BOOKS.ID,
            BOOKS.TITLE,
            BOOKS.PRICE,
            BOOKS.PUBLICATION_STATUS,
            AUTHORS.ID,
            AUTHORS.NAME,
            AUTHORS.BIRTH_DATE
        )
            .from(BOOKS)
            .leftJoin(BOOK_AUTHORS).on(BOOKS.ID.eq(BOOK_AUTHORS.BOOK_ID))
            .leftJoin(AUTHORS).on(BOOK_AUTHORS.AUTHOR_ID.eq(AUTHORS.ID))
            .where(BOOKS.ID.`in`(ids))
            .fetch()
        
        return result.groupBy { it.get(BOOKS.ID) }
            .map { (bookId, records) ->
                val firstRecord = records.first()
                val authors = records.mapNotNull { record ->
                    if (record.get(AUTHORS.ID) != null) {
                        Author(
                            id = record.get(AUTHORS.ID),
                            name = record.get(AUTHORS.NAME),
                            birthDate = record.get(AUTHORS.BIRTH_DATE)
                        )
                    } else null
                }.distinctBy { it.id }
                
                Book(
                    id = bookId,
                    title = firstRecord.get(BOOKS.TITLE),
                    price = Price.of(firstRecord.get(BOOKS.PRICE)),
                    publicationStatus = PublicationStatus.entries[firstRecord.get(BOOKS.PUBLICATION_STATUS).toInt()],
                    authors = authors
                )
            }
    }
}
