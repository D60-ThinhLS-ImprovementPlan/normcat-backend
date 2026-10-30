package com.normcat.modules.search.service;

import com.normcat.common.response.PageResponse;
import com.normcat.modules.search.dto.request.ProductSearchCriteria;
import com.normcat.modules.search.dto.response.ProductSearchResponse;

public interface ProductSearchService {

    /**
     * Executes high-performance product faceted search with criteria and pagination.
     *
     * @param criteria dynamic filter criteria (keyword, category, price, attributes, pagination)
     * @return paginated response containing ProductSearchResponse items
     */
    PageResponse<ProductSearchResponse> searchProducts(ProductSearchCriteria criteria);
}
