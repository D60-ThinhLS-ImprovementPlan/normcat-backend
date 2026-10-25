# NormCat — Nền Tảng Quản Lý Danh Mục & Chuẩn Hóa Dữ Liệu Sản Phẩm

[![Java 21](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.org/projects/jdk/21/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.3+-green.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-16-blue.svg)](https://www.postgresql.org/)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

> Nền tảng Modular Monolith chuẩn Production xây dựng trên **Java 21** và **Spring Boot 3.3+**, chuyên giải quyết bài toán chuẩn hóa dữ liệu thuộc tính sản phẩm từ nhiều nhà cung cấp (Multi-vendor Attribute Normalization) bằng từ điển quy tắc tường minh và cung cấp bộ lọc tìm kiếm đa diện (Faceted Search) hiệu năng cao trên PostgreSQL.

---

## 📌 Bối Cảnh Nghiệp Vụ & Vấn Đề Thực Tế

Trong các sàn thương mại điện tử hoặc hệ thống bán lẻ đa kênh, dữ liệu sản phẩm nhập từ nhiều nhà cung cấp thường gặp tình trạng hỗn loạn (Attribute Entropy):
- Nhà cung cấp A gửi: `{"Colour": "Red", "Size": "XL"}`
- Nhà cung cấp B gửi: `{"Màu sắc": "Đỏ", "Kích cỡ": "Extra Large"}`
- Nhà cung cấp C gửi: `{"Màu": "Đỏ đô"}`

**NormCat** giải quyết triệt để vấn đề này thông qua cơ chế chuẩn hóa dựa trên từ điển quy tắc xác thực (Deterministic Rule-based Dictionary):
- **Chuẩn hóa tên thuộc tính:** Gom các bí danh (`Colour`, `Màu sắc`, `Màu`) về thuộc tính chuẩn `COLOR`.
- **Chuẩn hóa giá trị thuộc tính:** Gom các bí danh (`Red`, `Đỏ`) về giá trị chuẩn `RED`.
- **Bảo toàn ngữ nghĩa (Semantic Integrity):** Tuyệt đối không gộp nhầm các giá trị có sắc thái khác nhau (ví dụ: `Đỏ đô` không bao giờ bị gộp vào `RED` trừ khi được cấu hình rõ ràng thành giá trị riêng biệt `BURGUNDY`).
- **Truy vết kép (Dual Storage):** Lưu trữ song song dữ liệu gốc (Raw Attributes JSONB) để đối soát audit và dữ liệu quan hệ chuẩn hóa (Relational EAV) để phục vụ bộ lọc tìm kiếm.

---

## 🏗️ Kiến Trúc & Công Nghệ

- **Ngôn ngữ & Framework:** Java 21, Spring Boot 3.3+
- **Kiến trúc:** Modular Monolith (phân tách domain rạch ròi, sẵn sàng tách Microservice)
- **Cơ sở dữ liệu:** PostgreSQL 16 (tận dụng B-Tree Composite Index, GIN, JSONB)
- **Quản lý dữ liệu:** Spring Data JPA, Hibernate, Flyway Database Migration
- **Tài liệu API & Giao diện Test:** Springdoc OpenAPI 3 (Swagger UI tích hợp sẵn tại `/swagger-ui.html`)
- **Frontend Demo:** React + TypeScript (triển khai Firebase Hosting)
- **Đóng gói & Vận hành:** Docker, Docker Compose (chạy 1 lệnh duy nhất)
- **Hạ tầng & Triển khai:** Cloud VPS (Backend & DB), Cloudflare (Domain, SSL, DNS & Proxy)
- **Kiểm thử tự động:** JUnit 5, Mockito, AssertJ, Testcontainers (PostgreSQL thật)

---

## 🚀 Hướng Dẫn Khởi Chạy Nhanh (Local Setup)

### Yêu cầu môi trường
- Docker & Docker Compose
- Java 21 & Maven 3.9+ (Tùy chọn nếu chạy bằng Docker)

### 1. Clone repository
```bash
git clone https://github.com/D60-ThinhLS-ImprovementPlan/product-catalog-search.git
cd product-catalog-search
```

### 2. Khởi chạy bằng Docker Compose
```bash
docker compose up --build -d
```
Ứng dụng sẽ sẵn sàng phục vụ tại:
- **API Base URL:** `http://localhost:8080/api/v1`
- **Swagger Documentation:** `http://localhost:8080/swagger-ui.html`

---

## 📚 Tài Liệu Kỹ Thuật Chi Tiết

- [Thiết kế Kiến trúc Hệ thống & Package Structure](docs/architecture/system-design.md)
- [Thiết kế Cơ sở Dữ liệu & ERD v1 (PostgreSQL)](docs/architecture/erd-v1.md)

---

## 🌿 Quy Chuẩn Phân Nhánh Git (Git Flow)

Repository tuân thủ nghiêm ngặt quy trình Git Flow:
- `master`: Nhánh ổn định, sẵn sàng deploy hoặc trình diễn demo.
- `develop`: Nhánh tích hợp các tính năng đang phát triển (được bảo vệ, không push trực tiếp).
- `feature/<issue-number>-<ten-tinh-nang>`: Nhánh phát triển tính năng mới.
- `fix/<issue-number>-<ten-loi>`: Nhánh sửa lỗi.
- `docs/<issue-number>-<mo-ta>`: Nhánh cập nhật tài liệu kỹ thuật.

---

## 📄 Bản Quyền
Dự án được phát hành theo giấy phép MIT License.
