package com.library.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record BookOrderDTO(Long bookId, LocalDateTime orderDate) implements Serializable {}
