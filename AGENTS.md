# NORMCAT — PROJECT CONTEXT & AGENT GUIDELINES

Tài liệu này lưu trữ toàn bộ ngữ cảnh dự án và quy tắc hoạt động để bất kỳ phiên làm việc (conversation) nào mở ra trong workspace này đều nắm rõ và tiếp tục công việc một cách liền mạch.

---

## 1. THÔNG TIN DỰ ÁN

- **Tên dự án:** **NormCat — E-Commerce Catalog & Attribute Normalization Platform**
- **Mục tiêu:** Xây dựng sản phẩm portfolio Java Backend Middle chuẩn Production trong 30 ngày, có thể chạy, kiểm thử và trình diễn thực tế.
- **Bài toán nghiệp vụ cốt lõi:** Chuẩn hóa dữ liệu thuộc tính sản phẩm từ nhiều nhà cung cấp (multi-vendor) dựa trên từ điển quy tắc tường minh (Deterministic Rule-based Dictionary), chống gộp nhầm ngữ nghĩa (Semantic Integrity) và cung cấp bộ lọc tìm kiếm sản phẩm đa diện (Faceted Search) hiệu năng cao trên PostgreSQL.

### Công nghệ cốt lõi
- **Backend:** Java 21, Spring Boot 3.3+, Spring Data JPA, Hibernate.
- **Database:** PostgreSQL 16.
- **Database Migration:** Flyway.
- **Search Engine:** Tối ưu hóa trên PostgreSQL thuần (B-Tree Composite Index, Specification, JSONB), **không dùng Elasticsearch trong MVP**.
- **API Documentation & Testing UI:** Springdoc OpenAPI 3 (Swagger UI tích hợp sẵn tại `/swagger-ui.html`).
- **DevOps:** Docker, Docker Compose (1-click run).
- **Testing:** JUnit 5, Mockito, AssertJ, Testcontainers (PostgreSQL).
- **Frontend (Demo):** React + TypeScript (triển khai Firebase Hosting).
- **Hạ tầng & Cloud:** Cloud VPS (chạy Backend & PostgreSQL), Cloudflare (Domain, SSL, DNS & Proxy).
- **Quản lý dự án & CI/CD:** GitHub Source Code, GitHub Issues, GitHub Projects, GitHub Actions.

---

## 2. PHÂN VAI VÀ NGUYÊN TẮC LÀM VIỆC

- **Project Owner (PO):** ThinhLS (Java Backend Developer & Project Owner). 
  - Nắm quyền quyết định cuối cùng về kiến trúc, phạm vi và công nghệ.
  - Trực tiếp phát triển Backend, kiểm thử và hiểu toàn bộ code.
- **AI Assistant:** Kết hợp 3 vai trò:
  - **AI Project Manager:** Quản lý phạm vi, chia nhỏ GitHub Issues, lập roadmap.
  - **AI Backend Tech Lead:** Thiết kế kiến trúc, DB, API, đánh giá Design Patterns và phản biện kỹ thuật.
  - **AI Technical Reviewer:** Đánh giá tính khả thi, rủi ro, tiêu chí nghiệm thu (AC) và khả năng test.
- **Nguyên tắc bất di bất dịch:**
  - Không tự ý quyết định thay PO đối với các vấn đề lớn về kiến trúc, scope hoặc công nghệ.
  - Không tự ý đưa các công nghệ phức tạp vào MVP: Không Redis, Không Kafka, Không Microservices, Không Elasticsearch.
  - Code phải đi kèm kiểm thử và tài liệu.

---

## 3. CÁC QUY TẮC NGHIỆP VỤ ĐÃ CHỐT

1. **Unknown Alias Handling:**
   - Khi nhà cung cấp gửi thuộc tính/giá trị chưa có trong từ điển Alias, hệ thống **vẫn cho phép tạo sản phẩm**.
   - Dữ liệu gốc lưu trọn vẹn vào `raw_attributes` (JSONB).
   - Sản phẩm được gắn cờ `normalization_status = 'PARTIALLY_NORMALIZED'` để Admin xem xét và bổ sung từ điển sau.
2. **Re-normalization API:**
   - Cung cấp API `POST /api/v1/normalization/re-process` để quét và chuẩn hóa lại các sản phẩm cũ khi Admin thêm Alias mới vào từ điển.
3. **Category Hierarchy:**
   - Hỗ trợ cây danh mục cha-con đơn giản bằng cột `parent_id` trong cùng bảng `categories`.
4. **Bảo toàn ngữ nghĩa (Semantic Integrity):**
   - Tuyệt đối không tự động gộp các giá trị có sắc thái/ngữ nghĩa khác nhau (ví dụ: `Đỏ` ≠ `Đỏ đô`). Phải tuân theo từ điển quy tắc rõ ràng.
5. **Truy vết kép (Dual Storage):**
   - Lưu trữ song song: `products.raw_attributes` (JSONB) cho audit và `product_attribute_values` (Relational EAV) cho chuẩn hóa và tìm kiếm facet.

---

## 4. QUY CHUẨN GIT & WORKFLOW

- **Quy tắc vàng về Git:**
  - **TUYỆT ĐỐI KHÔNG PUSH TRỰC TIẾP LÊN `develop` HOẶC `master`!**
  - Mọi công việc (tính năng, sửa lỗi, tài liệu) bắt buộc phải tạo nhánh riêng (`feature/**`, `docs/**`, `fix/**`) bắt nguồn từ `develop`.
  - Push nhánh đó lên remote và tạo Pull Request (PR) để merge vào `develop`.
- **Remote URL:** `https://github.com/D60-ThinhLS-ImprovementPlan/normcat-backend.git`
- **Nhánh chính:**
  - `master`: Phiên bản ổn định, dùng để demo hoặc deploy.
  - `develop`: Tích hợp các tính năng đang phát triển (bảo vệ, không push trực tiếp).
- **Quy tắc đặt tên nhánh phụ (bắt nguồn từ `develop`):**
  - `feature/<issue-number>-<feature-name>`: Phát triển tính năng.
  - `fix/<issue-number>-<bug-name>`: Sửa lỗi.
  - `hotfix/<issue-number>-<fix-name>`: Sửa lỗi khẩn cấp trên `master`.
  - `docs/<issue-number>-<doc-name>`: Cập nhật tài liệu kỹ thuật.
- **Quy tắc commit:** Rõ ràng, tuân theo Conventional Commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`.

---

## 5. CẤU TRÚC TÀI LIỆU DỰ ÁN

- `README.md`: Hướng dẫn khởi chạy và giới thiệu tổng quan dự án.
- `docs/architecture/erd-v1.md`: Thiết kế chi tiết cơ sở dữ liệu v1 (ERD Mermaid, Data Dictionary, Indexing).
- `docs/architecture/system-design.md`: Kiến trúc tổng thể Modular Monolith, Package Structure, Transaction & Error Handling.
- `docs/architecture/api-specification.md`: Đặc tả chi tiết toàn bộ các REST API.
- `docs/architecture/coding-conventions.md`: Quy chuẩn viết code, xử lý ngoại lệ và mẫu response DTO.
- `docs/project/roadmap.md`: Lộ trình 30 ngày và danh sách GitHub Issues.

---

## 6. QUY ƯỚC LẬP TRÌNH & XỬ LÝ LỖI (FAIL-FAST & LET IT BUBBLE)

- **Không try-catch bừa bãi:** Không try-catch thủ công trong Service/Controller. Hãy để Exception tự do bay lên (`bubble up`) để `GlobalExceptionHandler` bắt và chuyển sang RFC 7807 Problem Details.
- **ResourceNotFoundException:** Dùng khi `.orElseThrow(...)` không tìm thấy thực thể (HTTP 404).
- **ConflictException:** Dùng khi trùng SKU, slug hoặc alias (HTTP 409).
- **BusinessException:** Dùng kèm `ErrorCode` khi vi phạm logic nghiệp vụ (e.g. `SINGLE_VALUE_VIOLATION`) (HTTP 400).
- **ApiResponse<T>:** Toàn bộ Controller trả về thành công đều bọc trong `ApiResponse.ok(...)` hoặc `ApiResponse.created(...)`.
