package com.normcat.modules.category.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryNodeResponse {

    private Long id;
    private String name;
    private String slug;
    private String description;
    private Boolean isActive;

    @Builder.Default
    private List<CategoryNodeResponse> children = new ArrayList<>();
}
