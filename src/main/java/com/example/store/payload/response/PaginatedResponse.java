package com.example.store.payload.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginatedResponse<T> {

    private List<T> data;
    private boolean hasPages;
    private int currentPage;
    private int totalPages;
    private long totalItems;

    /**
     * Creates a PaginatedResponse from a Spring Data Page object.
     * @param page The Page object
     * @param <T> The type of data in the page
     * @return A PaginatedResponse containing the data and pagination information
     */
    public static <T> PaginatedResponse<T> fromPage(Page<T> page) {
        return PaginatedResponse.<T>builder()
                .data(page.getContent())
                .hasPages(page.getTotalPages() > 0)
                .currentPage(page.getNumber())
                .totalPages(page.getTotalPages())
                .totalItems(page.getTotalElements())
                .build();
    }
}
