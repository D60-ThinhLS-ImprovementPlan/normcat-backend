# NORMCAT — TÀI LIỆU ĐẶC TẢ API (API SPECIFICATION)

Tài liệu này đặc tả chi tiết toàn bộ các RESTful API của dự án **NormCat**, bao gồm Endpoint, Method, DTO Request/Response, Validation, Quy tắc nghiệp vụ và Mã lỗi tương ứng.

---

## 1. TỔNG QUAN HỆ THỐNG API

- **Base URL:** `/api/v1`
- **Định dạng dữ liệu:** `application/json` (UTF-8)
- **Chuẩn phản hồi lỗi:** RFC 7807 Problem Details

---

## 2. MODULE 1: QUẢN LÝ DANH MỤC (CATEGORY APIs)

### 2.1. Tạo mới danh mục
- **Endpoint:** `POST /api/v1/categories`
- **Mục đích:** Tạo danh mục gốc hoặc danh mục con trong cây phân cấp.
- **Request Body:**
  ```json
  {
    "name": "Áo sơ mi nam",
    "slug": "ao-so-mi-nam",
    "parentId": 1,
    "description": "Các loại áo sơ mi nam thời trang"
  }
  ```
- **Validation:**
  - `name`: `@NotBlank`, độ dài 2-100 ký tự.
  - `slug`: `@NotBlank`, regex `^[a-z0-9-]+$`.
- **Response (201 Created):**
  ```json
  {
    "id": 5,
    "name": "Áo sơ mi nam",
    "slug": "ao-so-mi-nam",
    "parentId": 1,
    "description": "Các loại áo sơ mi nam thời trang",
    "isActive": true,
    "createdAt": "2026-10-25T15:00:00Z"
  }
  ```
- **Lỗi có thể xảy ra:** `400 Bad Request` (Validation fail), `404 Not Found` (`parentId` không tồn tại), `409 Conflict` (`slug` đã tồn tại).

### 2.2. Lấy cây phân cấp danh mục
- **Endpoint:** `GET /api/v1/categories/tree`
- **Mục đích:** Trả về danh sách danh mục theo cấu trúc cây đa cấp lồng nhau.
- **Response (200 OK):**
  ```json
  [
    {
      "id": 1,
      "name": "Thời trang nam",
      "slug": "thoi-trang-nam",
      "children": [
        {
          "id": 5,
          "name": "Áo sơ mi nam",
          "slug": "ao-so-mi-nam",
          "children": []
        }
      ]
    }
  ]
  ```

---

## 3. MODULE 2: TỪ ĐIỂN QUY TẮC CHUẨN HÓA (DICTIONARY APIs)

### 3.1. Tạo thuộc tính chuẩn
- **Endpoint:** `POST /api/v1/dictionary/attributes`
- **Request Body:**
  ```json
  {
    "code": "COLOR",
    "name": "Màu sắc",
    "dataType": "STRING",
    "isFilterable": true
  }
  ```
- **Validation:** `code`: `@NotBlank`, regex `^[A-Z0-9_]+$`; `name`: `@NotBlank`.
- **Response (201 Created):** Trả về chi tiết thuộc tính vừa tạo.
- **Lỗi:** `409 Conflict` nếu mã `code` đã tồn tại.

### 3.2. Thêm giá trị chuẩn cho thuộc tính
- **Endpoint:** `POST /api/v1/dictionary/attributes/{attributeId}/values`
- **Request Body:**
  ```json
  {
    "code": "RED",
    "displayValue": "Đỏ",
    "sortOrder": 1
  }
  ```
- **Response (201 Created):** Trả về giá trị chuẩn vừa tạo.
- **Lỗi:** `404 Not Found` (không tìm thấy attributeId), `409 Conflict` (trùng lặp mã `code` trong cùng thuộc tính).

### 3.3. Thêm bí danh (Alias) cho tên thuộc tính
- **Endpoint:** `POST /api/v1/dictionary/attributes/{attributeId}/aliases`
- **Request Body:**
  ```json
  {
    "rawAttributeName": "màu sắc"
  }
  ```
- **Nghiệp vụ:** Hệ thống tự động `trim()` và chuyển thành chữ thường. `rawAttributeName` là duy nhất toàn hệ thống.
- **Lỗi:** `409 Conflict` nếu bí danh này đã được gán cho một thuộc tính khác.

### 3.4. Thêm bí danh (Alias) cho giá trị chuẩn
- **Endpoint:** `POST /api/v1/dictionary/values/{valueId}/aliases`
- **Request Body:**
  ```json
  {
    "rawValue": "đỏ"
  }
  ```
- **Nghiệp vụ:** Kiểm tra trong cùng thuộc tính cha, `rawValue` không được trỏ vào 2 giá trị chuẩn khác nhau.
- **Lỗi:** `409 Conflict` nếu bí danh đã tồn tại trong thuộc tính này.

---

## 4. MODULE 3 & 4: TIẾP NHẬN SẢN PHẨM & CHUẨN HÓA (PRODUCT & NORMALIZATION APIs)

### 4.1. Tiếp nhận và tự động chuẩn hóa sản phẩm (Ingestion API)
- **Endpoint:** `POST /api/v1/products`
- **Request Body:**
  ```json
  {
    "sku": "TSHIRT-RED-XL",
    "name": "Áo Thun Cotton Cổ Tròn",
    "categoryId": 5,
    "price": 199000,
    "rawAttributes": {
      "Màu": "Đỏ",
      "Kích cỡ": "XL",
      "Chất liệu": "Cotton 100%"
    }
  }
  ```
- **Validation:** `sku`, `name`, `categoryId` không được để trống; `price` >= 0; `rawAttributes` không được null.
- **Quy trình xử lý nội bộ:**
  1. Kiểm tra SKU duy nhất.
  2. Lưu bản ghi vào bảng `products` với toàn vẹn `rawAttributes` (JSONB).
  3. Kích hoạt `NormalizationEngine`: Tra cứu từng cặp key-value qua từ điển Alias.
  4. Lưu các thuộc tính đã map thành công vào `product_attribute_values`.
  5. Đánh dấu trạng thái:
     - `NORMALIZED`: Nếu 100% thuộc tính được map thành công.
     - `PARTIALLY_NORMALIZED`: Nếu có ít nhất 1 thuộc tính chưa có trong từ điển Alias (Unknown Alias).
- **Response (201 Created):**
  ```json
  {
    "id": 101,
    "sku": "TSHIRT-RED-XL",
    "name": "Áo Thun Cotton Cổ Tròn",
    "categoryId": 5,
    "price": 199000,
    "normalizationStatus": "NORMALIZED",
    "rawAttributes": {
      "Màu": "Đỏ",
      "Kích cỡ": "XL",
      "Chất liệu": "Cotton 100%"
    },
    "normalizedAttributes": [
      {
        "attributeCode": "COLOR",
        "attributeName": "Màu sắc",
        "valueCode": "RED",
        "displayValue": "Đỏ"
      },
      {
        "attributeCode": "SIZE",
        "attributeName": "Kích cỡ",
        "valueCode": "XL",
        "displayValue": "XL"
      }
    ]
  }
  ```

### 4.2. Quét tái chuẩn hóa sản phẩm (Re-normalization API)
- **Endpoint:** `POST /api/v1/normalization/re-process`
- **Mục đích:** Quét lại các sản phẩm đang ở trạng thái `PARTIALLY_NORMALIZED` khi Admin vừa bổ sung thêm Alias mới vào từ điển.
- **Response (200 OK):**
  ```json
  {
    "totalScanned": 20,
    "newlyNormalized": 15,
    "stillPending": 5
  }
  ```

---

## 5. MODULE 5: BỘ LỌC TÌM KIẾM ĐA DIỆN (FACETED SEARCH API)

### 5.1. Tìm kiếm và lọc sản phẩm
- **Endpoint:** `GET /api/v1/search/products`
- **Query Parameters:**
  - `keyword`: Tìm theo tên hoặc SKU (`ao thun`).
  - `categorySlug`: Lọc theo danh mục (`ao-so-mi-nam`).
  - `minPrice`, `maxPrice`: Lọc theo khoảng giá.
  - `attrs[CODE]=VALUE_CODE`: Lọc theo thuộc tính chuẩn hóa (ví dụ: `attrs[COLOR]=RED&attrs[SIZE]=XL`).
  - `page`: Số trang (bắt đầu từ 0).
  - `size`: Số phần tử trên trang (mặc định 20).
  - `sort`: `price,asc` hoặc `createdAt,desc`.
- **Response (200 OK):**
  ```json
  {
    "content": [
      {
        "id": 101,
        "sku": "TSHIRT-RED-XL",
        "name": "Áo Thun Cotton Cổ Tròn",
        "price": 199000,
        "categoryName": "Áo sơ mi nam",
        "normalizedAttributes": {
          "COLOR": "Đỏ",
          "SIZE": "XL"
        }
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1,
    "totalPages": 1,
    "isLast": true
  }
  ```
