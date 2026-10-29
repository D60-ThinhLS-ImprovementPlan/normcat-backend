package com.normcat.modules.search.specification;

import com.normcat.modules.category.entity.Category;
import com.normcat.modules.dictionary.entity.Attribute;
import com.normcat.modules.dictionary.entity.AttributeValue;
import com.normcat.modules.product.entity.Product;
import com.normcat.modules.product.entity.ProductAttributeValue;
import com.normcat.modules.product.entity.ProductStatus;
import com.normcat.modules.search.dto.request.ProductSearchCriteria;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ProductSearchSpecification implements Specification<Product> {

    private final ProductSearchCriteria criteria;

    @Override
    public Predicate toPredicate(Root<Product> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        List<Predicate> predicates = new ArrayList<>();

        // 1. Status ACTIVE (only active products appear in catalog search)
        predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

        if (criteria == null) {
            return cb.and(predicates.toArray(new Predicate[0]));
        }

        // 2. Keyword: case-insensitive search across name or sku
        if (StringUtils.hasText(criteria.getKeyword())) {
            String pattern = "%" + criteria.getKeyword().trim().toLowerCase() + "%";
            Predicate nameMatch = cb.like(cb.lower(root.get("name")), pattern);
            Predicate skuMatch = cb.like(cb.lower(root.get("sku")), pattern);
            predicates.add(cb.or(nameMatch, skuMatch));
        }

        // 3. Category slug filter
        if (StringUtils.hasText(criteria.getCategorySlug())) {
            Join<Product, Category> categoryJoin = root.join("category", JoinType.INNER);
            predicates.add(cb.equal(categoryJoin.get("slug"), criteria.getCategorySlug().trim()));
        }

        // 4. Price bounds
        if (criteria.getMinPrice() != null) {
            predicates.add(cb.greaterThanOrEqualTo(root.get("price"), criteria.getMinPrice()));
        }

        if (criteria.getMaxPrice() != null) {
            predicates.add(cb.lessThanOrEqualTo(root.get("price"), criteria.getMaxPrice()));
        }

        // 5. Faceted Search with dynamic attributes (AND logic across distinct facets)
        // Leveraging composite index idx_pav_search (attribute_id, attribute_value_id, product_id)
        if (criteria.getAttrs() != null && !criteria.getAttrs().isEmpty()) {
            for (Map.Entry<String, String> entry : criteria.getAttrs().entrySet()) {
                String attrCode = entry.getKey();
                String valueCode = entry.getValue();

                if (StringUtils.hasText(attrCode) && StringUtils.hasText(valueCode)) {
                    Subquery<Long> subquery = query.subquery(Long.class);
                    Root<ProductAttributeValue> pav = subquery.from(ProductAttributeValue.class);
                    Join<ProductAttributeValue, AttributeValue> av = pav.join("attributeValue", JoinType.INNER);
                    Join<AttributeValue, Attribute> a = av.join("attribute", JoinType.INNER);

                    subquery.select(pav.get("id"));
                    subquery.where(
                            cb.equal(pav.get("product"), root),
                            cb.equal(cb.upper(a.get("code")), attrCode.trim().toUpperCase()),
                            cb.equal(cb.upper(av.get("code")), valueCode.trim().toUpperCase())
                    );

                    predicates.add(cb.exists(subquery));
                }
            }
        }

        return cb.and(predicates.toArray(new Predicate[0]));
    }
}
