package com.library.view;

import com.library.dto.BookDTO;
import com.library.service.BookService;
import org.primefaces.model.FilterMeta;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;

import java.util.List;
import java.util.Map;

public class BookLazyDataModel extends LazyDataModel<BookDTO> {

    private final BookService service;

    public BookLazyDataModel(BookService service) {
        this.service = service;
    }

    @Override
    public int count(Map<String, FilterMeta> filterBy) {
        // In a real scenario, you would pass filterBy to the service
        return service.countBooks();
    }

    @Override
    public List<BookDTO> load(int first, int pageSize, Map<String, SortMeta> sortBy, Map<String, FilterMeta> filterBy) {
        // In a real scenario, you would pass sortBy and filterBy to the service
        return service.getBooks(first, pageSize);
    }

    @Override
    public String getRowKey(BookDTO object) {
        return String.valueOf(object.getId());
    }

    @Override
    public BookDTO getRowData(String rowKey) {
        // This is used for selection. 
        // Since we don't have the full list in memory, we might need to fetch it or parse the ID.
        // For simple selection, returning null often works if rowKey is enough, 
        // but ideally, we should fetch the object if needed.
        // However, fetching from DB here might be expensive if called frequently.
        // A common pattern is to iterate over the *current page* list (wrappedData) if available.
        
        List<BookDTO> books = (List<BookDTO>) getWrappedData();
        if (books != null) {
            for (BookDTO book : books) {
                if (String.valueOf(book.getId()).equals(rowKey)) {
                    return book;
                }
            }
        }
        return null;
    }
}
