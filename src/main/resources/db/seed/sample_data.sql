-- ==============================================================================
-- NormCat Database Migration V2: Seed Comprehensive Catalog & Dictionary Data
-- Target Database: PostgreSQL 16
-- ==============================================================================

-- ------------------------------------------------------------------------------
-- 1. SEED CATEGORIES (Hierarchical Tree)
-- ------------------------------------------------------------------------------
INSERT INTO categories (id, name, slug, parent_id, description, is_active) VALUES
(1, 'Thiết bị điện tử', 'thiet-bi-dien-tu', NULL, 'Thiết bị công nghệ và điện tử tiêu dùng', TRUE),
(2, 'Điện thoại di động', 'dien-thoai', 1, 'Điện thoại thông minh, smartphone chính hãng', TRUE),
(3, 'Máy tính xách tay', 'laptop', 1, 'Laptop văn phòng, gaming, đồ họa cao cấp', TRUE),
(4, 'Phụ kiện công nghệ', 'phu-kien', 1, 'Phụ kiện điện thoại, máy tính, cáp sạc, ốp lưng', TRUE);

-- ------------------------------------------------------------------------------
-- 2. SEED ATTRIBUTES (Canonical Attribute Catalog)
-- ------------------------------------------------------------------------------
INSERT INTO attributes (id, code, name, data_type, is_multi_valued, is_filterable) VALUES
(1, 'COLOR', 'Màu sắc', 'STRING', FALSE, TRUE),
(2, 'RAM', 'Bộ nhớ RAM', 'STRING', FALSE, TRUE),
(3, 'STORAGE', 'Dung lượng lưu trữ', 'STRING', FALSE, TRUE),
(4, 'BRAND', 'Thương hiệu', 'STRING', FALSE, TRUE);

-- ------------------------------------------------------------------------------
-- 3. SEED ATTRIBUTE VALUES (Canonical Attribute Values)
-- ------------------------------------------------------------------------------
-- COLOR (attribute_id = 1)
INSERT INTO attribute_values (id, attribute_id, code, display_value, sort_order) VALUES
(1, 1, 'BLACK', 'Đen', 1),
(2, 1, 'WHITE', 'Trắng', 2),
(3, 1, 'BLUE', 'Xanh dương', 3),
(4, 1, 'TITANIUM', 'Titan tự nhiên', 4);

-- RAM (attribute_id = 2)
INSERT INTO attribute_values (id, attribute_id, code, display_value, sort_order) VALUES
(5, 2, '8GB', '8 GB', 1),
(6, 2, '12GB', '12 GB', 2),
(7, 2, '16GB', '16 GB', 3),
(8, 2, '18GB', '18 GB', 4);

-- STORAGE (attribute_id = 3)
INSERT INTO attribute_values (id, attribute_id, code, display_value, sort_order) VALUES
(9, 3, '128GB', '128 GB', 1),
(10, 3, '256GB', '256 GB', 2),
(11, 3, '512GB', '512 GB', 3),
(12, 3, '1TB', '1 TB', 4);

-- BRAND (attribute_id = 4)
INSERT INTO attribute_values (id, attribute_id, code, display_value, sort_order) VALUES
(13, 4, 'APPLE', 'Apple', 1),
(14, 4, 'SAMSUNG', 'Samsung', 2),
(15, 4, 'DELL', 'Dell', 3);

-- ------------------------------------------------------------------------------
-- 4. SEED ATTRIBUTE ALIASES (Normalized key dictionary)
-- ------------------------------------------------------------------------------
INSERT INTO attribute_aliases (attribute_id, raw_attribute_name, normalized_name) VALUES
-- COLOR (1)
(1, 'Màu sắc', 'màu sắc'),
(1, 'Màu', 'màu'),
(1, 'Colour', 'colour'),
(1, 'Color', 'color'),

-- RAM (2)
(2, 'Bộ nhớ RAM', 'bộ nhớ ram'),
(2, 'RAM', 'ram'),
(2, 'Bộ nhớ trong (RAM)', 'bộ nhớ trong (ram)'),
(2, 'Memory', 'memory'),

-- STORAGE (3)
(3, 'Dung lượng', 'dung lượng'),
(3, 'Bộ nhớ trong', 'bộ nhớ trong'),
(3, 'Ổ cứng', 'ổ cứng'),
(3, 'Storage', 'storage'),
(3, 'ROM', 'rom'),

-- BRAND (4)
(4, 'Thương hiệu', 'thương hiệu'),
(4, 'Hãng sản xuất', 'hãng sản xuất'),
(4, 'Brand', 'brand'),
(4, 'Hãng', 'hãng');

-- ------------------------------------------------------------------------------
-- 5. SEED ATTRIBUTE VALUE ALIASES (Normalized value dictionary)
-- ------------------------------------------------------------------------------
-- COLOR Values
INSERT INTO attribute_value_aliases (attribute_id, attribute_value_id, raw_value, normalized_value) VALUES
(1, 1, 'Đen', 'đen'),
(1, 1, 'Black', 'black'),
(1, 1, 'Màu đen', 'màu đen'),
(1, 1, 'Đen Không Gian', 'đen không gian'),
(1, 2, 'Trắng', 'trắng'),
(1, 2, 'White', 'white'),
(1, 2, 'Bạc', 'bạc'),
(1, 3, 'Xanh', 'xanh'),
(1, 3, 'Xanh dương', 'xanh dương'),
(1, 3, 'Blue', 'blue'),
(1, 3, 'Navy', 'navy'),
(1, 4, 'Titan', 'titan'),
(1, 4, 'Natural Titanium', 'natural titanium'),
(1, 4, 'Xám Titan', 'xám titan');

-- RAM Values
INSERT INTO attribute_value_aliases (attribute_id, attribute_value_id, raw_value, normalized_value) VALUES
(2, 5, '8GB', '8gb'),
(2, 5, '8G', '8g'),
(2, 5, '8 gb', '8 gb'),
(2, 5, '8 gigabytes', '8 gigabytes'),
(2, 6, '12GB', '12gb'),
(2, 6, '12G', '12g'),
(2, 6, '12 gb', '12 gb'),
(2, 7, '16GB', '16gb'),
(2, 7, '16G', '16g'),
(2, 7, '16 gb', '16 gb'),
(2, 8, '18GB', '18gb'),
(2, 8, '18G', '18g');

-- STORAGE Values
INSERT INTO attribute_value_aliases (attribute_id, attribute_value_id, raw_value, normalized_value) VALUES
(3, 9, '128GB', '128gb'),
(3, 9, '128G', '128g'),
(3, 9, '128 gb', '128 gb'),
(3, 10, '256GB', '256gb'),
(3, 10, '256G', '256g'),
(3, 10, '256 gb', '256 gb'),
(3, 11, '512GB', '512gb'),
(3, 11, '512G', '512g'),
(3, 11, '512 gb', '512 gb'),
(3, 12, '1TB', '1tb'),
(3, 12, '1T', '1t'),
(3, 12, '1 tb', '1 tb'),
(3, 12, '1024GB', '1024gb');

-- BRAND Values
INSERT INTO attribute_value_aliases (attribute_id, attribute_value_id, raw_value, normalized_value) VALUES
(4, 13, 'Apple', 'apple'),
(4, 13, 'Táo', 'táo'),
(4, 14, 'Samsung', 'samsung'),
(4, 15, 'Dell', 'dell');

-- ------------------------------------------------------------------------------
-- 6. SEED PRODUCTS (Dual Storage: Raw JSONB + Relational Normalized Mapping)
-- ------------------------------------------------------------------------------
INSERT INTO products (id, sku, name, category_id, price, status, raw_attributes, normalization_status, version) VALUES
(1, 'IPHONE-15-PRO-128-TI', 'iPhone 15 Pro 128GB Titan Tự Nhiên', 2, 25990000.00, 'ACTIVE', 
 '{"Màu sắc": "Titan", "Bộ nhớ": "8G", "Dung lượng": "128GB", "Hãng": "Apple"}'::jsonb, 'NORMALIZED', 0),

(2, 'IPHONE-15-PRO-256-BLK', 'iPhone 15 Pro 256GB Đen Không Gian', 2, 28990000.00, 'ACTIVE', 
 '{"Colour": "Black", "RAM": "8GB", "Bộ nhớ trong": "256G", "Brand": "Apple"}'::jsonb, 'NORMALIZED', 0),

(3, 'IPHONE-15-128-BLU', 'iPhone 15 128GB Xanh Dương', 2, 19990000.00, 'ACTIVE', 
 '{"Màu": "Xanh", "RAM": "8G", "Dung lượng": "128GB", "Thương hiệu": "Apple"}'::jsonb, 'NORMALIZED', 0),

(4, 'SS-S24-ULTRA-256-TI', 'Samsung Galaxy S24 Ultra 256GB Xám Titan', 2, 27490000.00, 'ACTIVE', 
 '{"Màu sắc": "Xám Titan", "Bộ nhớ RAM": "12G", "ROM": "256GB", "Hãng sản xuất": "Samsung"}'::jsonb, 'NORMALIZED', 0),

(5, 'SS-S24-128-BLK', 'Samsung Galaxy S24 128GB Đen Onyx', 2, 17990000.00, 'ACTIVE', 
 '{"Màu sắc": "Đen", "RAM": "8GB", "Dung lượng": "128GB", "Brand": "Samsung"}'::jsonb, 'NORMALIZED', 0),

(6, 'MACBOOK-AIR-M3-16-256', 'MacBook Air 13 inch M3 16GB 256GB Đen', 3, 29990000.00, 'ACTIVE', 
 '{"Color": "Black", "Memory": "16GB", "Ổ cứng": "256GB", "Hãng": "Apple"}'::jsonb, 'NORMALIZED', 0),

(7, 'MACBOOK-PRO-14-M3-18-512', 'MacBook Pro 14 inch M3 Pro 18GB 512GB Titan', 3, 49990000.00, 'ACTIVE', 
 '{"Màu sắc": "Titan", "RAM": "18G", "Storage": "512GB", "Brand": "Apple"}'::jsonb, 'NORMALIZED', 0),

(8, 'DELL-XPS-13-16-512', 'Dell XPS 13 9340 16GB 512GB Trắng', 3, 38990000.00, 'ACTIVE', 
 '{"Màu": "Trắng", "Bộ nhớ": "16G", "Ổ cứng": "512G", "Hãng sản xuất": "Dell"}'::jsonb, 'NORMALIZED', 0),

(9, 'CASE-IP15-SILICONE-NAVY', 'Ốp lưng Silicone iPhone 15 Xanh Navy', 4, 450000.00, 'ACTIVE', 
 '{"Màu sắc": "Navy", "Chất liệu": "Silicone Cao Cấp", "Hãng": "Apple"}'::jsonb, 'PARTIALLY_NORMALIZED', 0);

-- ------------------------------------------------------------------------------
-- 7. SEED PRODUCT ATTRIBUTE VALUES (Faceted Search Links)
-- ------------------------------------------------------------------------------
-- Product 1: iPhone 15 Pro 128GB Titan (COLOR: TITANIUM, RAM: 8GB, STORAGE: 128GB, BRAND: APPLE)
INSERT INTO product_attribute_values (product_id, attribute_id, attribute_value_id) VALUES
(1, 1, 4), (1, 2, 5), (1, 3, 9), (1, 4, 13);

-- Product 2: iPhone 15 Pro 256GB Black (COLOR: BLACK, RAM: 8GB, STORAGE: 256GB, BRAND: APPLE)
INSERT INTO product_attribute_values (product_id, attribute_id, attribute_value_id) VALUES
(2, 1, 1), (2, 2, 5), (2, 3, 10), (2, 4, 13);

-- Product 3: iPhone 15 128GB Blue (COLOR: BLUE, RAM: 8GB, STORAGE: 128GB, BRAND: APPLE)
INSERT INTO product_attribute_values (product_id, attribute_id, attribute_value_id) VALUES
(3, 1, 3), (3, 2, 5), (3, 3, 9), (3, 4, 13);

-- Product 4: Samsung S24 Ultra 256GB Titan (COLOR: TITANIUM, RAM: 12GB, STORAGE: 256GB, BRAND: SAMSUNG)
INSERT INTO product_attribute_values (product_id, attribute_id, attribute_value_id) VALUES
(4, 1, 4), (4, 2, 6), (4, 3, 10), (4, 4, 14);

-- Product 5: Samsung S24 128GB Black (COLOR: BLACK, RAM: 8GB, STORAGE: 128GB, BRAND: SAMSUNG)
INSERT INTO product_attribute_values (product_id, attribute_id, attribute_value_id) VALUES
(5, 1, 1), (5, 2, 5), (5, 3, 9), (5, 4, 14);

-- Product 6: MacBook Air M3 16GB 256GB Black (COLOR: BLACK, RAM: 16GB, STORAGE: 256GB, BRAND: APPLE)
INSERT INTO product_attribute_values (product_id, attribute_id, attribute_value_id) VALUES
(6, 1, 1), (6, 2, 7), (6, 3, 10), (6, 4, 13);

-- Product 7: MacBook Pro 14 M3 Pro 18GB 512GB (COLOR: TITANIUM, RAM: 18GB, STORAGE: 512GB, BRAND: APPLE)
INSERT INTO product_attribute_values (product_id, attribute_id, attribute_value_id) VALUES
(7, 1, 4), (7, 2, 8), (7, 3, 11), (7, 4, 13);

-- Product 8: Dell XPS 13 16GB 512GB White (COLOR: WHITE, RAM: 16GB, STORAGE: 512GB, BRAND: DELL)
INSERT INTO product_attribute_values (product_id, attribute_id, attribute_value_id) VALUES
(8, 1, 2), (8, 2, 7), (8, 3, 11), (8, 4, 15);

-- Product 9: Case iPhone 15 Navy (COLOR: BLUE, BRAND: APPLE) [Chất liệu is unmapped]
INSERT INTO product_attribute_values (product_id, attribute_id, attribute_value_id) VALUES
(9, 1, 3), (9, 4, 13);

-- ------------------------------------------------------------------------------
-- 8. SYNCHRONIZE POSTGRESQL SEQUENCES (Prevent primary key collision on inserts)
-- ------------------------------------------------------------------------------
SELECT setval(pg_get_serial_sequence('categories', 'id'), COALESCE((SELECT MAX(id) FROM categories), 1));
SELECT setval(pg_get_serial_sequence('attributes', 'id'), COALESCE((SELECT MAX(id) FROM attributes), 1));
SELECT setval(pg_get_serial_sequence('attribute_values', 'id'), COALESCE((SELECT MAX(id) FROM attribute_values), 1));
SELECT setval(pg_get_serial_sequence('attribute_aliases', 'id'), COALESCE((SELECT MAX(id) FROM attribute_aliases), 1));
SELECT setval(pg_get_serial_sequence('attribute_value_aliases', 'id'), COALESCE((SELECT MAX(id) FROM attribute_value_aliases), 1));
SELECT setval(pg_get_serial_sequence('products', 'id'), COALESCE((SELECT MAX(id) FROM products), 1));
SELECT setval(pg_get_serial_sequence('product_attribute_values', 'id'), COALESCE((SELECT MAX(id) FROM product_attribute_values), 1));
