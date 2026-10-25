# NORMCAT — CODING CONVENTIONS & EXCEPTION HANDLING GUIDELINES

Tài liệu này quy chuẩn hóa toàn bộ quy tắc viết code, xử lý ngoại lệ (Exception Handling) và chuẩn hóa phản hồi API (API Response Envelope) cho toàn bộ dự án **NormCat Backend**.

---

## 1. NGUYÊN TẮC CỐT LÕI: "FAIL-FAST & LET IT BUBBLE"

1. **Tuyệt đối không try-catch bừa bãi:** 
   Tại tầng Controller và Service, không viết khối `try-catch` chỉ để in lỗi (`e.printStackTrace()`) hoặc log suông rồi nuốt lỗi.
2. **Chủ động bắn ngoại lệ nghiệp vụ (Fail-Fast):**
   Khi dữ liệu đầu vào hoặc trạng thái hệ thống vi phạm quy tắc nghiệp vụ, lập tức ném ngoại lệ tương ứng (`throw new ...Exception(...)`).
3. **Để ngoại lệ tự do bay lên (`Let it bubble`):**
   Ngoại lệ sẽ được `GlobalExceptionHandler` (`@RestControllerAdvice`) tự động chặn lại, chuyển thành HTTP status code và payload JSON chuẩn **RFC 7807 Problem Details**, đồng thời kích hoạt rollback tự động của Spring `@Transactional`.

---

## 2. QUY ƯỚC SỬ DỤNG TỪNG LOẠI EXCEPTION

### 2.1. `ResourceNotFoundException` (HTTP 404 NOT FOUND)
- **Khi nào dùng:** Khi tìm kiếm thực thể qua `id`, `slug`, `code`, `sku` mà không có trong Database.
- **Quy tắc viết code trong Service:**
  ```java
  // Tìm theo ID
  Category category = categoryRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Category", "id", id));

  // Tìm theo Slug
  Category category = categoryRepository.findBySlug(slug)
      .orElseThrow(() -> new ResourceNotFoundException("Category", "slug", slug));

  // Tìm theo SKU
  Product product = productRepository.findBySku(sku)
      .orElseThrow(() -> new ResourceNotFoundException("Product", "sku", sku));
  ```
- **JSON RFC 7807 trả về Client:**
  ```json
  {
    "timestamp": "2026-10-25T17:15:00Z",
    "status": 404,
    "error": "Not Found",
    "code": "RESOURCE_NOT_FOUND",
    "message": "Category not found with id: '99'",
    "path": "/api/v1/categories/99"
  }
  ```

---

### 2.2. `ConflictException` (HTTP 409 CONFLICT)
- **Khi nào dùng:** Khi phát hiện trùng lặp thực thể trước khi thực hiện Insert/Update.
- **Quy tắc viết code trong Service:**
  ```java
  // Kiểm tra trùng SKU
  if (productRepository.existsBySku(request.getSku())) {
      throw new ConflictException("Product", "sku", request.getSku());
  }

  // Kiểm tra trùng Slug danh mục
  if (categoryRepository.existsBySlug(request.getSlug())) {
      throw new ConflictException("Category", "slug", request.getSlug());
  }

  // Kiểm tra xung đột Alias
  if (attributeAliasRepository.existsByNormalizedRawName(normalizedKey)) {
      throw new ConflictException(ErrorCode.ALIAS_CONFLICT, 
          "The alias '" + rawKey + "' already maps to another attribute");
  }
  ```

---

### 2.3. `BusinessException` (HTTP 400 BAD REQUEST hoặc Custom Status)
- **Khi nào dùng:** Khi vi phạm logic nghiệp vụ chuyên sâu (Invariants) mà DTO validation không bắt được.
- **Quy tắc viết code trong Service:** Luôn truyền kèm một `ErrorCode` định danh:
  ```java
  // Vi phạm quy tắc thuộc tính đơn trị (Single-valued invariant)
  if (!attribute.isMultiValued() && values.size() > 1) {
      throw new BusinessException(ErrorCode.SINGLE_VALUE_VIOLATION, 
          "Attribute '" + attribute.getCode() + "' is single-valued and cannot accept multiple values");
  }

  // Vi phạm cấu trúc cây (danh mục tự trỏ vào chính nó)
  if (parentId != null && parentId.equals(currentCategoryId)) {
      throw new BusinessException(ErrorCode.CYCLIC_CATEGORY_HIERARCHY, 
          "A category cannot be its own parent");
  }
  ```

---

### 2.4. Validation đầu vào tại DTO (`@Valid`)
- **Tại Controller:** Luôn đặt `@Valid` trước `@RequestBody`:
  ```java
  @PostMapping
  public ResponseEntity<ApiResponse<CategoryResponse>> create(
          @Valid @RequestBody CreateCategoryRequest request) { ... }
  ```
- **Tại Request DTO (Record):** Sử dụng các Jakarta Validation annotations:
  ```java
  public record CreateCategoryRequest(
      @NotBlank(message = "Category name must not be blank")
      @Size(min = 2, max = 100, message = "Category name must be between 2 and 100 characters")
      String name,

      @NotBlank(message = "Slug must not be blank")
      @Pattern(regexp = "^[a-z0-9-]+$", message = "Slug must be lowercase alphanumeric and hyphens")
      String slug,

      Long parentId
  ) {}
  ```
- **Cơ chế tự động:** `GlobalExceptionHandler` bắt `MethodArgumentNotValidException` và tự động trả về:
  ```json
  {
    "status": 400,
    "error": "Bad Request",
    "code": "VALIDATION_FAILED",
    "message": "Request validation failed for one or more fields",
    "errors": [
      {
        "field": "name",
        "rejectedValue": "",
        "message": "Category name must not be blank"
      }
    ]
  }
  ```

---

## 3. QUY ƯỚC PHẢN HỒI THÀNH CÔNG (`ApiResponse<T>`)

Toàn bộ Controller trả về kết quả bọc trong `ApiResponse<T>`:

```java
// 1. Phản hồi GET hoặc UPDATE (200 OK)
@GetMapping("/{id}")
public ResponseEntity<ApiResponse<ProductResponse>> getById(@PathVariable Long id) {
    ProductResponse data = productService.findById(id);
    return ResponseEntity.ok(ApiResponse.ok(data));
}

// 2. Phản hồi POST tạo mới (201 Created)
@PostMapping
public ResponseEntity<ApiResponse<ProductResponse>> create(
        @Valid @RequestBody CreateProductRequest request) {
    ProductResponse data = productService.createProduct(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created("Product created and normalized successfully", data));
}
```

---

## 4. QUY ƯỚC QUẢN TRỊ GIAO DỊCH (TRANSACTION MANAGEMENT)

1. **Phương thức đọc (Query):** Luôn khai báo `@Transactional(readOnly = true)` để Hibernate tối ưu Dirty Checking và giải phóng database connection nhanh nhất.
2. **Phương thức ghi (Command):** Luôn khai báo `@Transactional(rollbackFor = Exception.class)` trên Service method để đảm bảo tính nguyên tử (Atomicity).
