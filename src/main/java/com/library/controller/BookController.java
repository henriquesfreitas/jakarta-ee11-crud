package com.library.controller;

import com.library.dto.BookDTO;
import com.library.service.BookService;
import com.library.util.ExceptionUtil;
import com.library.util.MessageUtil;
import com.library.view.BookLazyDataModel;
import jakarta.annotation.PostConstruct;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.primefaces.PrimeFaces;
import org.primefaces.model.LazyDataModel;

import java.io.Serializable;

@Slf4j
@Named
@ViewScoped
public class BookController implements Serializable {

    @Inject
    private BookService service;

    @Inject
    private MessageUtil messageUtil;

    @Inject
    private ExceptionUtil exceptionUtil;

    @Getter
    private LazyDataModel<BookDTO> lazyModel;

    @Getter @Setter
    private BookDTO currentBook;

    @PostConstruct
    public void init() {
        log.info("Initializing BookController");
        this.currentBook = new BookDTO();
        this.lazyModel = new BookLazyDataModel(service);
    }

    public void prepareCreate() {
        log.debug("Preparing to create a new book");
        this.currentBook = new BookDTO();
    }

    public void save() {
        log.info("Attempting to save book: {}", currentBook.getTitle());
        try {
            service.saveBook(currentBook);
            this.currentBook = new BookDTO(); // Reset form
            messageUtil.addInfoMessage("Success", "Book Saved");
            PrimeFaces.current().executeScript("PF('bookDialog').hide()");
        } catch (Exception e) {
            exceptionUtil.handleException(e);
        }
    }

    public void delete(BookDTO book) {
        log.info("Attempting to delete book with ID: {}", book.getId());
        try {
            service.deleteBook(book.getId());
            messageUtil.addWarnMessage("Deleted", "Book Removed");
        } catch (Exception e) {
             exceptionUtil.handleException(e);
        }
    }
}
