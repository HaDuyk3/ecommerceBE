# E-commerce Backend API

Backend RESTful cho hệ thống thương mại điện tử đa người bán (multi-seller marketplace), xây dựng bằng Spring Boot 3. Hỗ trợ nhiều người bán cùng đăng bán trên một danh mục sản phẩm chung, với xác thực JWT, phân quyền theo vai trò, giỏ hàng, đặt hàng, thanh toán, khuyến mãi và đánh giá.

## Tính năng chính

- **Xác thực & phân quyền:** đăng ký, đăng nhập, refresh token (JWT). Ba vai trò: USER, SELLER, ADMIN.
- **Người bán (Seller):** đăng ký gian hàng, đăng bán sản phẩm với giá và tồn kho riêng.
- **Sản phẩm:** thông tin sản phẩm chung + nhiều người bán gắn giá/kho riêng (mô hình catalog dùng chung).
- **Giỏ hàng:** thêm, sửa số lượng, xóa item, xem giỏ với giá tính theo thời gian thực.
- **Đặt hàng:** đặt hàng từ giỏ, trừ kho an toàn (atomic), lưu snapshot giá và địa chỉ tại thời điểm đặt.
- **Thanh toán:** khởi tạo thanh toán và xử lý callback từ cổng thanh toán (giả lập).
- **Khuyến mãi:** người bán tạo mã giảm giá áp cho sản phẩm.
- **Đánh giá:** người mua đánh giá sản phẩm sau khi mua.
- **Quản trị (Admin):** duyệt sản phẩm, khóa/mở khóa tài khoản người dùng.

## Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Framework | Spring Boot 3.2.5 (Web, Data JPA, Security, Validation) |
| Xác thực | JWT (jjwt 0.11.5), access token + refresh token |
| Database | MySQL (Spring Data JPA / Hibernate) |
| Cache | Redis (lưu refresh token) |
| Tài liệu API | Springdoc OpenAPI (Swagger UI) |
| Build | Maven |
| Khác | Lombok |

## Điểm kỹ thuật đáng chú ý

- **Trừ kho an toàn (atomic):** dùng câu lệnh điều kiện `UPDATE ... WHERE stock >= quantity` để tránh bán quá hàng khi nhiều người mua đồng thời (chống race condition).
- **Server-side pricing:** giá luôn được tính lại ở backend, không tin giá gửi từ client — tránh gian lận giá.
- **Snapshot pattern:** đơn hàng lưu lại giá và địa chỉ tại thời điểm đặt, nên thay đổi giá/địa chỉ sau này không ảnh hưởng đơn cũ.
- **Refresh token rotation:** mỗi lần làm mới sẽ thay token cũ, lưu trên Redis.
- **Kiến trúc package-by-feature:** mỗi nghiệp vụ (auth, cart, order...) là một package độc lập, dễ bảo trì.

## Cấu trúc dự án

```
src/main/java/com/haduy/ecommerce/
├── auth/        # đăng nhập, JWT, refresh token, security config
├── user/        # thông tin người dùng, địa chỉ giao hàng
├── seller/      # đăng ký và quản lý gian hàng
├── product/     # sản phẩm, danh mục
├── offer/       # SellerProduct — giá và tồn kho của từng người bán
├── cart/        # giỏ hàng
├── order/       # đặt hàng, trạng thái đơn
├── payment/     # khởi tạo & callback thanh toán
├── promotion/   # mã khuyến mãi
├── pricing/     # tính giá, phí vận chuyển
├── review/      # đánh giá sản phẩm
└── common/      # exception, enum, response wrapper, cấu hình chung
```

## Yêu cầu môi trường

- Java 17+
- MySQL (đang chạy)
- Redis (đang chạy)
- Maven (hoặc dùng `mvnw` kèm theo)

## Cài đặt & chạy

1. Tạo database trong MySQL:
   ```sql
   CREATE DATABASE ecommerce CHARACTER SET utf8mb4;
   ```

2. Đặt 2 biến môi trường (kết nối MySQL và khóa ký JWT):
   ```bash
   # Linux / macOS
   export DB_PASSWORD=mat_khau_mysql_cua_ban
   export JWT_SECRET=mot_chuoi_bi_mat_du_dai_de_ky_jwt

   # Windows PowerShell
   $env:DB_PASSWORD="mat_khau_mysql_cua_ban"
   $env:JWT_SECRET="mot_chuoi_bi_mat_du_dai_de_ky_jwt"
   ```

3. Chạy ứng dụng:
   ```bash
   ./mvnw spring-boot:run
   ```
   Hibernate sẽ tự tạo bảng khi khởi động (`ddl-auto=update`).

4. Mở tài liệu API (Swagger UI):
   ```
   http://localhost:8080/swagger-ui.html
   ```

## API chính

### Xác thực — `/api/auth`
| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/register` | Đăng ký tài khoản |
| POST | `/login` | Đăng nhập, nhận JWT |
| POST | `/refresh-token` | Làm mới access token |
| POST | `/logout` | Đăng xuất |

### Sản phẩm — `/api/products`
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/products` | Danh sách sản phẩm |
| GET | `/api/products/{id}` | Chi tiết sản phẩm |
| POST | `/api/products` | Tạo sản phẩm (SELLER) |
| GET | `/api/products/{productId}/sellers` | Các người bán của 1 sản phẩm |

### Người bán — `/api/seller`
| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/seller/register` | Đăng ký gian hàng |
| POST | `/api/seller/products` | Đăng bán sản phẩm (giá + kho) |
| GET | `/api/seller/products` | Danh sách sản phẩm đang bán |

### Giỏ hàng — `/api/cart`
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/cart` | Xem giỏ |
| POST | `/api/cart/items` | Thêm vào giỏ |
| PUT | `/api/cart/items/{id}` | Cập nhật số lượng |
| DELETE | `/api/cart/items/{id}` | Xóa item |

### Đơn hàng — `/api/orders`
| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/orders/checkout` | Đặt hàng từ giỏ |
| GET | `/api/orders` | Danh sách đơn của tôi |
| GET | `/api/orders/{id}` | Chi tiết đơn |
| PATCH | `/api/orders/{id}/cancel` | Hủy đơn |

### Thanh toán — `/api/payments`
| Method | Endpoint | Mô tả |
|---|---|---|
| POST | `/api/payments/init` | Khởi tạo thanh toán |
| POST | `/api/payments/callback` | Callback từ cổng thanh toán |

### Khác
- Đánh giá: `POST /api/reviews`, `GET /api/products/{productId}/reviews`
- Khuyến mãi (SELLER): `/api/seller/promotions`
- Quản trị (ADMIN): duyệt sản phẩm, khóa/mở tài khoản

## Ghi chú

- Phần thanh toán hiện ở dạng giả lập (chưa tích hợp cổng thật như VNPAY/MOMO).
- Dự án tập trung vào backend; chưa có giao diện frontend.

## Tác giả

Nguyễn Phạm Hà Duy
