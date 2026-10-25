# NORMCAT — DATABASE DESIGN & ERD v1

Tài liệu này định nghĩa chi tiết mô hình cơ sở dữ liệu v1 cho dự án **NormCat**, bao gồm sơ đồ quan hệ thực thể (ERD Mermaid), cấu trúc bảng, khóa chính/ngoại, ràng buộc toàn vẹn và chiến lược đánh index trên PostgreSQL 16.

---

## 1. SƠ ĐỒ THỰC THỂ (ERD MERMAID)

```mermaid
erDiagram
    CATEGORIES ||--o{ CATEGORIES : "has parent (parent_id)"
    CATEGORIES ||--o{ PRODUCTS : "contains"
    
    ATTRIBUTES ||--o{ ATTRIBUTE_VALUES : "has standard values"
    ATTRIBUTES ||--o{ ATTRIBUTE_ALIASES : "maps raw attribute names"
    
    ATTRIBUTE_VALUES ||--o{ ATTRIBUTE_VALUE_ALIASES : "maps raw value names"
    
    PRODUCTS ||--o{ PRODUCT_ATTRIBUTE_VALUES : "has normalized attributes"
    ATTRIBUTES ||--o{ PRODUCT_ATTRIBUTE_VALUES : "defines attribute of"
    ATTRIBUTE_VALUES ||--o{ PRODUCT_ATTRIBUTE_VALUES : "defines value of"

    CATEGORIES {
        bigint id PK
        varchar_100 name "Tên danh mục"
        varchar_120 slug UK "Slug duy nhất"
        bigint parent_id FK "Danh mục cha (nullable)"
        text description "Mô tả"
        boolean is_active "Trạng thái kích hoạt"
        timestamptz created_at
        timestamptz updated_at
    }

    ATTRIBUTES {
        bigint id PK
        varchar_50 code UK "Mã chuẩn (COLOR, SIZE, RAM)"
        varchar_100 name "Tên hiển thị chuẩn (Màu sắc, Kích cỡ)"
        varchar_20 data_type "STRING, NUMBER, BOOLEAN"
        boolean is_filterable "Hiển thị trên bộ lọc facet"
        timestamptz created_at
        timestamptz updated_at
    }

    ATTRIBUTE_VALUES {
        bigint id PK
        bigint attribute_id FK "Thuộc tính cha"
        varchar_50 code "Mã giá trị (RED, BURGUNDY, XL)"
        varchar_100 display_value "Tên hiển thị (Đỏ, Đỏ đô, XL)"
        int sort_order "Thứ tự sắp xếp hiển thị"
        timestamptz created_at
        timestamptz updated_at
    }

    ATTRIBUTE_ALIASES {
        bigint id PK
        bigint attribute_id FK "Thuộc tính chuẩn"
        varchar_100 raw_attribute_name UK "Tên gốc (màu, colour, màu sắc)"
        timestamptz created_at
    }

    ATTRIBUTE_VALUE_ALIASES {
        bigint id PK
        bigint attribute_id FK "Thuộc tính sở hữu"
        bigint attribute_value_id FK "Giá trị chuẩn"
        varchar_100 raw_value "Giá trị gốc (đỏ, red, màu đỏ)"
        timestamptz created_at
    }

    PRODUCTS {
        bigint id PK
        varchar_50 sku UK "Mã SKU duy nhất"
        varchar_255 name "Tên sản phẩm"
        bigint category_id FK "Danh mục chính"
        numeric_14_2 price "Giá bán niêm yết"
        varchar_20 status "ACTIVE, INACTIVE, DRAFT"
        jsonb raw_attributes "Dữ liệu gốc nhà cung cấp gửi"
        varchar_30 normalization_status "NORMALIZED, PARTIALLY_NORMALIZED, FAILED"
        timestamptz created_at
        timestamptz updated_at
        bigint version "Optimistic Locking"
    }

    PRODUCT_ATTRIBUTE_VALUES {
        bigint id PK
        bigint product_id FK "Sản phẩm"
        bigint attribute_id FK "Thuộc tính chuẩn"
        bigint attribute_value_id FK "Giá trị chuẩn"
        timestamptz created_at
    }
```

---

## 2. DATA DICTIONARY CHI TIẾT

### 2.1. Bảng `categories`
- **Mục đích:** Quản lý danh mục hàng hóa theo cây phân cấp.
- **Cột & Kiểu dữ liệu:**
  - `id`: `BIGSERIAL PRIMARY KEY`
  - `name`: `VARCHAR(100) NOT NULL`
  - `slug`: `VARCHAR(120) NOT NULL UNIQUE`
  - `parent_id`: `BIGINT NULL REFERENCES categories(id) ON DELETE SET NULL`
  - `description`: `TEXT NULL`
  - `is_active`: `BOOLEAN NOT NULL DEFAULT TRUE`
  - `created_at`: `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - `updated_at`: `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`

### 2.2. Bảng `attributes`
- **Mục đích:** Định nghĩa các thuộc tính chuẩn hóa của hệ thống.
- **Cột & Kiểu dữ liệu:**
  - `id`: `BIGSERIAL PRIMARY KEY`
  - `code`: `VARCHAR(50) NOT NULL UNIQUE` (ví dụ: `COLOR`, `SIZE`, `STORAGE`)
  - `name`: `VARCHAR(100) NOT NULL` (ví dụ: "Màu sắc", "Kích cỡ")
  - `data_type`: `VARCHAR(20) NOT NULL DEFAULT 'STRING'` (`STRING`, `NUMBER`, `BOOLEAN`)
  - `is_filterable`: `BOOLEAN NOT NULL DEFAULT TRUE`
  - `created_at`: `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - `updated_at`: `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`

### 2.3. Bảng `attribute_values`
- **Mục đích:** Định nghĩa các giá trị chuẩn hợp lệ của từng thuộc tính.
- **Cột & Kiểu dữ liệu:**
  - `id`: `BIGSERIAL PRIMARY KEY`
  - `attribute_id`: `BIGINT NOT NULL REFERENCES attributes(id) ON DELETE CASCADE`
  - `code`: `VARCHAR(50) NOT NULL` (ví dụ: `RED`, `BURGUNDY`, `XL`)
  - `display_value`: `VARCHAR(100) NOT NULL` (ví dụ: "Đỏ", "Đỏ đô", "Extra Large")
  - `sort_order`: `INT NOT NULL DEFAULT 0`
  - `created_at`: `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - `updated_at`: `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
- **Ràng buộc:**
  - `UNIQUE (attribute_id, code)`

### 2.4. Bảng `attribute_aliases`
- **Mục đích:** Ánh xạ tên thuộc tính từ nhà cung cấp về thuộc tính chuẩn.
- **Cột & Kiểu dữ liệu:**
  - `id`: `BIGSERIAL PRIMARY KEY`
  - `attribute_id`: `BIGINT NOT NULL REFERENCES attributes(id) ON DELETE CASCADE`
  - `raw_attribute_name`: `VARCHAR(100) NOT NULL UNIQUE` (lowercase & trim, ví dụ: `'màu'`, `'colour'`, `'màu sắc'`)
  - `created_at`: `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`

### 2.5. Bảng `attribute_value_aliases`
- **Mục đích:** Ánh xạ giá trị thô từ nhà cung cấp về giá trị chuẩn.
- **Cột & Kiểu dữ liệu:**
  - `id`: `BIGSERIAL PRIMARY KEY`
  - `attribute_id`: `BIGINT NOT NULL REFERENCES attributes(id) ON DELETE CASCADE`
  - `attribute_value_id`: `BIGINT NOT NULL REFERENCES attribute_values(id) ON DELETE CASCADE`
  - `raw_value`: `VARCHAR(100) NOT NULL` (lowercase & trim, ví dụ: `'đỏ'`, `'red'`, `'màu đỏ'`)
  - `created_at`: `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
- **Ràng buộc vàng (Data Integrity):**
  - `UNIQUE (attribute_id, raw_value)`
  - *Ý nghĩa:* Trong cùng một thuộc tính (ví dụ `COLOR`), chuỗi thô `'đỏ'` đã map vào `RED` thì không thể map vào `BURGUNDY`.

### 2.6. Bảng `products`
- **Mục đích:** Quản lý thông tin chính của sản phẩm, lưu trữ dữ liệu thô gốc.
- **Cột & Kiểu dữ liệu:**
  - `id`: `BIGSERIAL PRIMARY KEY`
  - `sku`: `VARCHAR(50) NOT NULL UNIQUE`
  - `name`: `VARCHAR(255) NOT NULL`
  - `category_id`: `BIGINT NOT NULL REFERENCES categories(id) ON DELETE RESTRICT`
  - `price`: `NUMERIC(14,2) NOT NULL`
  - `status`: `VARCHAR(20) NOT NULL DEFAULT 'ACTIVE'` (`ACTIVE`, `INACTIVE`, `DRAFT`)
  - `raw_attributes`: `JSONB NOT NULL DEFAULT '{}'::jsonb`
  - `normalization_status`: `VARCHAR(30) NOT NULL DEFAULT 'PENDING'` (`NORMALIZED`, `PARTIALLY_NORMALIZED`, `FAILED`)
  - `version`: `BIGINT NOT NULL DEFAULT 0` (Optimistic Locking)
  - `created_at`: `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
  - `updated_at`: `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`

### 2.7. Bảng `product_attribute_values`
- **Mục đích:** Cầu nối liên kết N-N giữa sản phẩm và giá trị chuẩn hóa, phục vụ bộ lọc đa chiều (Faceted Search).
- **Cột & Kiểu dữ liệu:**
  - `id`: `BIGSERIAL PRIMARY KEY`
  - `product_id`: `BIGINT NOT NULL REFERENCES products(id) ON DELETE CASCADE`
  - `attribute_id`: `BIGINT NOT NULL REFERENCES attributes(id) ON DELETE CASCADE`
  - `attribute_value_id`: `BIGINT NOT NULL REFERENCES attribute_values(id) ON DELETE CASCADE`
  - `created_at`: `TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP`
- **Ràng buộc:**
  - `UNIQUE (product_id, attribute_id, attribute_value_id)`

---

## 3. CHIẾN LƯỢC INDEXING (POSTGRESQL PERFORMANCE TUNING)

| Bảng | Tên Index | Cột Index | Loại Index | Mục đích kỹ thuật |
|---|---|---|---|---|
| `products` | `idx_products_category_status` | `(category_id, status)` | B-Tree | Tối ưu khi lọc danh sách sản phẩm theo danh mục và trạng thái ACTIVE. |
| `products` | `idx_products_price` | `(price)` | B-Tree | Tối ưu lọc theo khoảng giá `minPrice` - `maxPrice` và sắp xếp giá tăng/giảm. |
| `product_attribute_values` | `idx_pav_search` | `(attribute_id, attribute_value_id, product_id)` | B-Tree (Composite) | **Index cốt lõi cho Facet Search:** Khi filter `COLOR=RED`, Postgres chỉ cần Index Scan trên index này để lấy ngay danh sách `product_id`. |
| `product_attribute_values` | `idx_pav_product` | `(product_id)` | B-Tree | Tối ưu truy vấn lấy toàn bộ thuộc tính chuẩn của 1 sản phẩm khi xem chi tiết. |
| `attribute_value_aliases` | `idx_ava_lookup` | `(attribute_id, raw_value)` | B-Tree | Tối ưu cho Normalization Engine khi tra cứu alias cực nhanh với độ phức tạp $O(\log N)$. |
