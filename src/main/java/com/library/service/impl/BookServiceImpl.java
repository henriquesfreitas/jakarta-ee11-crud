package com.library.service.impl;

import com.library.dto.BookDTO;
import com.library.mapper.BookMapper;
import com.library.model.Book;
import com.library.repository.BookRepository;
import com.library.service.BookService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ApplicationScoped
/*
In modern Jakarta EE (CDI), using @ApplicationScoped for Services and Repositories is the standard "best practice" for Stateless components.
Here is why:
        1. Performance (Singleton Pattern)
•
@ApplicationScoped: The container creates only one instance of the class for the entire lifetime of the application. This instance is shared by all users and all requests.
        •
@RequestScoped: The container would create a new instance for every single HTTP request and destroy it afterwards. This creates unnecessary garbage collection pressure.
Since your Service and Repository classes do not hold any user-specific state (fields like currentUser or shoppingCart), there is no need to create new ones constantly.
        2. Thread Safety & The EntityManager Magic
You might wonder: "If 100 users use the same Repository instance, won't the EntityManager get mixed up?"
No, because of how @PersistenceContext works:
        •
The EntityManager injected into an @ApplicationScoped bean is actually a Proxy (a smart wrapper).
        •
When a thread calls repository.save(), the Proxy looks up the specific EntityManager associated with that current thread/transaction.
•
This makes it completely thread-safe, even though the Repository instance itself is a singleton.
3. Comparison with other scopes
•
@Stateless (EJB): Used to be the default. It uses a "pool" of instances. It's heavier and less popular now that CDI (@ApplicationScoped + @Transactional) is the standard.
        •
@RequestScoped: Good if your service needs to store request-specific data in fields, but usually, we pass that data as method arguments (DTOs).
In summary: We use @ApplicationScoped because it is the most efficient (memory/CPU) choice for stateless logic classes in CDI.
        */
public class BookServiceImpl implements BookService {

    @Inject
    private BookRepository repository;

    @Inject
    private BookMapper mapper;

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
}
