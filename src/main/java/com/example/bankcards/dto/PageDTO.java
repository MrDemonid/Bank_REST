package com.example.bankcards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

/**
 * Кастомный класс Page, поскольку Spring Boot выдает предупреждение
 * о нестабильном классе PageImpl.
 */
@Schema(description = "Страница выборки данных.")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDTO<T> {

    @Schema(description = "Список объектов выборки")
    private List<T> content;

    @Schema(description = "Доступных страниц для выборок (ceil(totalElements / size))")
    private int totalPages;

    @Schema(description = "Всего доступных элементов в БД для выборки")
    private long totalElements;

    @Schema(description = "Элементов на странице")
    private int size;

    @Schema(description = "Текущая страница (нумерация с нуля)")
    private int number;

    public PageDTO(Page<T> page) {
        page.getContent();
        this.content = page.getContent();
        this.totalPages = Math.max(page.getTotalPages(), 0);
        this.totalElements = page.getTotalElements();
        this.size = page.getSize();
        this.number = Math.max(page.getNumber(), 0);
    }

    public static <T> PageDTO<T> empty() {
        return new PageDTO<>(
                Collections.emptyList(), 0, 0, 0, 0
        );
    }

    public static boolean isEmpty(PageDTO<?> page) {
        return page.getContent().isEmpty();
    }

}

