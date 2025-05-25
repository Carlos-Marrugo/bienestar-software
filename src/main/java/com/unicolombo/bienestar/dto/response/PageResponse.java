package com.unicolombo.bienestar.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

public class PageResponse<T> {

    private Pagination pagination;
    private List<T> data;

    public PageResponse(Page<T> page) {
        this.pagination = new Pagination(
                page.getTotalElements(),
                page.getTotalPages(),
                page.getNumber()
        );
        this.data = page.getContent();
    }

    @Getter
    @AllArgsConstructor
    public static class Pagination {
        private long totalItems;
        private int totalPages;
        private int currentPage;
    }

    public List<T> getData() {
        return data;
    }

    public Pagination getPagination() {
        return pagination;
    }
}

