package com.library.mapper;

import com.library.dto.BookDTO;
import com.library.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import static org.junit.jupiter.api.Assertions.*;

class BookMapperTest {

    private BookMapper mapper;

    @BeforeEach
    void setUp() {
        // Since we use componentModel="cdi", MapStruct generates a class with @ApplicationScoped.
        // In a unit test, we can just instantiate the implementation directly if available,
        // or use the Mappers factory if we didn't use CDI.
        // However, with CDI, the factory might not work as expected if there are dependencies.
        // But here, BookMapper has no dependencies.
        mapper = Mappers.getMapper(BookMapper.class);
    }

    @Test
    void toDTO_ShouldMapAllFields() {
        Book book = new Book();
        book.setId(1L);
        book.setTitle("Test Title");
        book.setAuthor("Test Author");
        book.setPrice(19.99);
        book.setIsbn("1234567890123");

        BookDTO dto = mapper.toDTO(book);

        assertNotNull(dto);
        assertEquals(book.getId(), dto.getId());
        assertEquals(book.getTitle(), dto.getTitle());
        assertEquals(book.getAuthor(), dto.getAuthor());
        assertEquals(book.getPrice(), dto.getPrice());
        assertEquals(book.getIsbn(), dto.getIsbn());
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        BookDTO dto = new BookDTO();
        dto.setId(1L);
        dto.setTitle("Test Title");
        dto.setAuthor("Test Author");
        dto.setPrice(19.99);
        dto.setIsbn("1234567890123");

        Book book = mapper.toEntity(dto);

        assertNotNull(book);
        assertEquals(dto.getId(), book.getId());
        assertEquals(dto.getTitle(), book.getTitle());
        assertEquals(dto.getAuthor(), book.getAuthor());
        assertEquals(dto.getPrice(), book.getPrice());
        assertEquals(dto.getIsbn(), book.getIsbn());
    }
}
