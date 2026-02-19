package com.library.service;

import com.library.dto.BookDTO;
import java.util.List;

public interface BookService {
    List<BookDTO> getAllBooks();
    List<BookDTO> getBooks(int first, int pageSize);
    int countBooks();
    void saveBook(BookDTO bookDTO);
    void deleteBook(Long id);
}
