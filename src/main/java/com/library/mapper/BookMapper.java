package com.library.mapper;

import com.library.dto.BookDTO;
import com.library.model.Book;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "cdi")
public interface BookMapper {

    BookDTO toDTO(Book book);

    Book toEntity(BookDTO dto);

    void updateEntityFromDTO(BookDTO dto, @MappingTarget Book entity);
}
