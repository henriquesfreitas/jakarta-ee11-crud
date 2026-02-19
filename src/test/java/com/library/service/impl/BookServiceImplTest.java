package com.library.service.impl;

import com.library.dto.BookDTO;
import com.library.mapper.BookMapper;
import com.library.model.Book;
import com.library.repository.BookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository repository;

    @Mock
    private BookMapper mapper;

    @InjectMocks
    private BookServiceImpl service;

    private Book book;
    private BookDTO bookDTO;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);
        book.setTitle("Test Book");
        book.setAuthor("Test Author");
        book.setPrice(10.0);
        book.setIsbn("1234567890123");

        bookDTO = new BookDTO();
        bookDTO.setId(1L);
        bookDTO.setTitle("Test Book");
        bookDTO.setAuthor("Test Author");
        bookDTO.setPrice(10.0);
        bookDTO.setIsbn("1234567890123");
    }

    @Test
    void getAllBooks_ShouldReturnListOfBookDTOs() {
        when(repository.findAll()).thenReturn(Arrays.asList(book));
        when(mapper.toDTO(book)).thenReturn(bookDTO);

        List<BookDTO> result = service.getAllBooks();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(bookDTO.getTitle(), result.get(0).getTitle());
        verify(repository, times(1)).findAll();
    }

    @Test
    void saveBook_NewBook_ShouldCallPersist() {
        BookDTO newBookDTO = new BookDTO();
        newBookDTO.setTitle("New Book");
        
        Book newBook = new Book();
        newBook.setTitle("New Book");

        when(mapper.toEntity(newBookDTO)).thenReturn(newBook);

        service.saveBook(newBookDTO);

        verify(repository, times(1)).save(newBook);
    }

    @Test
    void saveBook_ExistingBook_ShouldCallMerge() {
        when(repository.findById(1L)).thenReturn(Optional.of(book));
        
        service.saveBook(bookDTO);

        verify(repository, times(1)).findById(1L);
        verify(mapper, times(1)).updateEntityFromDTO(bookDTO, book);
        verify(repository, times(1)).save(book);
    }

    @Test
    void saveBook_ExistingBookNotFound_ShouldThrowException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.saveBook(bookDTO));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteBook_ExistingId_ShouldDelete() {
        when(repository.findById(1L)).thenReturn(Optional.of(book));

        service.deleteBook(1L);

        verify(repository, times(1)).delete(book);
    }

    @Test
    void deleteBook_NonExistingId_ShouldThrowException() {
        when(repository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> service.deleteBook(1L));
        verify(repository, never()).delete(any());
    }
}
