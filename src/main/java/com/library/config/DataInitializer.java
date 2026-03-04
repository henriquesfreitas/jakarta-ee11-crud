package com.library.config;

import com.library.dto.BookDTO;
import com.library.model.BookStatus;
import com.library.service.BookService;
import jakarta.annotation.PostConstruct;
import jakarta.ejb.Singleton;
import jakarta.ejb.Startup;
import jakarta.inject.Inject;
import lombok.extern.slf4j.Slf4j;

@Singleton
@Startup
@Slf4j
public class DataInitializer {

    @Inject
    private BookService bookService;

    @PostConstruct
    public void init() {
        if (bookService.countBooks() > 0) {
            log.info("Database already populated. Skipping initialization.");
            return;
        }

        log.info("Initializing database with sample data...");

        createBook("The Great Gatsby", "F. Scott Fitzgerald", 15.99, "9780743273565", BookStatus.AVAILABLE);
        createBook("1984", "George Orwell", 12.50, "9780451524935", BookStatus.AVAILABLE);
        createBook("To Kill a Mockingbird", "Harper Lee", 14.99, "9780061120084", BookStatus.SOLD);
        createBook("Pride and Prejudice", "Jane Austen", 9.99, "9780141439518", BookStatus.AVAILABLE);
        createBook("The Catcher in the Rye", "J.D. Salinger", 11.00, "9780316769488", BookStatus.AVAILABLE);

        log.info("Database initialization completed.");
    }

    private void createBook(String title, String author, double price, String isbn, BookStatus status) {
        BookDTO book = new BookDTO();
        book.setTitle(title);
        book.setAuthor(author);
        book.setPrice(price);
        book.setIsbn(isbn);
        book.setStatus(status);
        
        try {
            bookService.saveBook(book);
            log.debug("Created book: {}", title);
        } catch (Exception e) {
            log.error("Failed to create book: {}", title, e);
        }
    }
}
