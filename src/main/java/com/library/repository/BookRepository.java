package com.library.repository;

import com.library.model.Book;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import java.util.Optional;

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
public class BookRepository {

    @PersistenceContext(unitName = "libraryPU")
    private EntityManager em;

    public List<Book> findAll() {
        log.debug("Querying all books from database");
        return em.createQuery("SELECT b FROM Book b", Book.class).getResultList();
    }

    public List<Book> findRange(int first, int pageSize) {
        log.debug("Querying books range: first={}, pageSize={}", first, pageSize);
        return em.createQuery("SELECT b FROM Book b", Book.class)
                .setFirstResult(first)
                .setMaxResults(pageSize)
                .getResultList();
    }

    public int count() {
        log.debug("Counting all books");
        return ((Number) em.createQuery("SELECT COUNT(b) FROM Book b").getSingleResult()).intValue();
    }

    public Optional<Book> findById(Long id) {
        log.debug("Finding book by ID: {}", id);
        return Optional.ofNullable(em.find(Book.class, id));
    }

    public void save(Book book) {
        if (book.getId() == null) {
            log.debug("Persisting new book: {}", book.getTitle());
            em.persist(book);
        } else {
            log.debug("Merging existing book: {}", book.getTitle());
            em.merge(book);
        }
    }

    public void delete(Book book) {
        log.debug("Removing book with ID: {}", book.getId());
        if (em.contains(book)) {
            em.remove(book);
        } else {
            em.remove(em.merge(book));
        }
    }
}
