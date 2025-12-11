package net.flaim.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationRequest {
    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 20;

    private String sortBy;
    private String sortDirection;

    private String search;
    private Boolean emailVerified;
}