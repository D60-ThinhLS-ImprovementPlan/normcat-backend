package com.normcat.modules.product.service;

import com.normcat.modules.product.dto.request.CreateProductRequest;
import com.normcat.modules.product.dto.response.ProductResponse;

public interface ProductService {

    ProductResponse createProduct(CreateProductRequest request);

    ProductResponse getProductById(Long id);

    ProductResponse getProductBySku(String sku);
}
