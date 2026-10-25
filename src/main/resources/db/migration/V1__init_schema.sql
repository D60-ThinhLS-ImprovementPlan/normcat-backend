-- ==============================================================================
-- NormCat Database Migration V1: Initial Schema, Constraints, and Indexes
-- Target Database: PostgreSQL 16
-- ==============================================================================

-- 1. Categories Table (Hierarchical tree)
CREATE TABLE categories (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(120) NOT NULL UNIQUE,
    parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 2. Standard Attributes Table
CREATE TABLE attributes (
    id BIGSERIAL PRIMARY KEY,
    code VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    data_type VARCHAR(20) NOT NULL DEFAULT 'STRING' CHECK (data_type IN ('STRING', 'NUMBER', 'BOOLEAN')),
    is_multi_valued BOOLEAN NOT NULL DEFAULT FALSE,
    is_filterable BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 3. Standard Attribute Values Table
CREATE TABLE attribute_values (
    id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT NOT NULL REFERENCES attributes(id) ON DELETE CASCADE,
    code VARCHAR(50) NOT NULL,
    display_value VARCHAR(100) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0 CHECK (sort_order >= 0),
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_attribute_value_code UNIQUE (attribute_id, code),
    CONSTRAINT uq_attribute_value_composite UNIQUE (id, attribute_id)
);

-- 4. Attribute Aliases Table (Mapping vendor raw attribute names to standard attributes)
CREATE TABLE attribute_aliases (
    id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT NOT NULL REFERENCES attributes(id) ON DELETE CASCADE,
    raw_attribute_name VARCHAR(100) NOT NULL,
    normalized_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_attr_alias_normalized UNIQUE (normalized_name)
);

-- 5. Attribute Value Aliases Table (Mapping vendor raw values to standard values)
CREATE TABLE attribute_value_aliases (
    id BIGSERIAL PRIMARY KEY,
    attribute_id BIGINT NOT NULL,
    attribute_value_id BIGINT NOT NULL,
    raw_value VARCHAR(100) NOT NULL,
    normalized_value VARCHAR(100) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Composite Foreign Key ensures attribute_value_id strictly belongs to attribute_id
    CONSTRAINT fk_val_alias_composite FOREIGN KEY (attribute_value_id, attribute_id)
        REFERENCES attribute_values(id, attribute_id) ON DELETE CASCADE,
    CONSTRAINT uq_val_alias_normalized UNIQUE (attribute_id, normalized_value)
);

-- 6. Products Table
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT,
    price NUMERIC(14,2) NOT NULL CHECK (price >= 0),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE', 'DRAFT')),
    raw_attributes JSONB NOT NULL DEFAULT '{}'::jsonb,
    normalization_status VARCHAR(30) NOT NULL DEFAULT 'PENDING' 
        CHECK (normalization_status IN ('PENDING', 'NORMALIZED', 'PARTIALLY_NORMALIZED', 'FAILED')),
    version BIGINT NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_products_category_status ON products (category_id, status);
CREATE INDEX idx_products_price ON products (price);

-- 7. Product Attribute Values Junction Table (Normalized facet bridge)
CREATE TABLE product_attribute_values (
    id BIGSERIAL PRIMARY KEY,
    product_id BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    attribute_id BIGINT NOT NULL,
    attribute_value_id BIGINT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- Composite Foreign Key ensures attribute_value_id strictly belongs to attribute_id
    CONSTRAINT fk_pav_attribute_value_composite FOREIGN KEY (attribute_value_id, attribute_id)
        REFERENCES attribute_values(id, attribute_id) ON DELETE CASCADE,
    CONSTRAINT uq_product_attribute_value UNIQUE (product_id, attribute_id, attribute_value_id)
);

-- Composite Index for Faceted Search: filter by attribute + value to locate products
CREATE INDEX idx_pav_search ON product_attribute_values (attribute_id, attribute_value_id, product_id);

-- Lookup Index for Product Details: retrieve all standardized attributes for a product
CREATE INDEX idx_pav_product ON product_attribute_values (product_id);
