package com.library.service.impl;

import com.library.dto.BookDTO;
import com.library.dto.BookOrderDTO;
import com.library.event.BookUpdateEvent;
import com.library.mapper.BookMapper;
import com.library.messaging.OrderProducer;
import com.library.model.Book;
import com.library.model.BookStatus;
import com.library.repository.BookRepository;
import com.library.service.BookService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.event.Event;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
public class BookServiceImpl implements BookService {

    @Inject
    private BookRepository repository;

    @Inject
    private BookMapper mapper;

    @Inject
    private OrderProducer orderProducer;

    @Inject
    private Event<BookUpdateEvent> bookUpdateEvent;

    @Override
    public List<BookDTO> getAllBooks() {
        log.debug("Fetching all books");
        return repository.findAll().stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<BookDTO> getBooks(int first, int pageSize) {
        log.debug("Fetching books page: first={}, pageSize={}", first, pageSize);
        return repository.findRange(first, pageSize).stream()
                .map(mapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public int countBooks() {
        return repository.count();
    }

    @Override
    @Transactional
    public void saveBook(BookDTO bookDTO) {
        log.info("Saving book: {}", bookDTO.getTitle());
        Book book;
        if (bookDTO.getId() != null) {
            log.debug("Updating existing book with ID: {}", bookDTO.getId());
            book = repository.findById(bookDTO.getId())
                    .orElseThrow(() -> {
                        log.error("Book not found with ID: {}", bookDTO.getId());
                        return new IllegalArgumentException("Book not found with ID: " + bookDTO.getId());
                    });
            mapper.updateEntityFromDTO(bookDTO, book);
        } else {
            log.debug("Creating new book");
            book = mapper.toEntity(bookDTO);
            // Ensure new books are always AVAILABLE
            book.setStatus(BookStatus.AVAILABLE);
        }
        repository.save(book);
        log.info("Book saved successfully");
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        log.info("Deleting book with ID: {}", id);
        Book book = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book not found with ID: {}", id);
                    return new IllegalArgumentException("Book not found with ID: " + id);
                });
        repository.delete(book);
        log.info("Book deleted successfully");
    }

    @Override
    @Transactional
    public void buyBook(Long id) {
        log.info("Initiating purchase for book ID: {}", id);
        // Send to JMS Queue
        BookOrderDTO order = new BookOrderDTO(id, LocalDateTime.now());
        orderProducer.sendOrder(order);
        log.info("Purchase order sent to queue");
    }

    @Override
    @Transactional
    public void processPurchase(Long id) {
        log.info("Processing purchase for book ID: {}", id);
        Book book = repository.findById(id)
                .orElseThrow(() -> {
                    log.error("Book not found with ID: {}", id);
                    return new IllegalArgumentException("Book not found with ID: " + id);
                });
        
        if (book.getStatus() == BookStatus.SOLD) {
            log.warn("Book ID {} is already sold. Skipping update.", id);
            // In a real system, you might want to handle this (e.g., refund)
            return;
        }
        
        book.setStatus(BookStatus.SOLD);
        repository.save(book);
        log.info("Book ID {} marked as SOLD", id);
        
        // Fire CDI event for WebSocket update
        bookUpdateEvent.fire(new BookUpdateEvent("Book " + id + " sold"));
    }
}
