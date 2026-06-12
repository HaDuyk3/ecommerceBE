# E-commerce Backend API

Backend RESTful cho hệ thống thương mại điện tử đa người bán (multi-seller marketplace), xây dựng bằng Spring Boot 3. Hỗ trợ nhiều người bán cùng đăng bán trên một danh mục sản phẩm chung, với xác thực JWT, phân quyền theo vai trò, giỏ hàng, đặt hàng, thanh toán (VNPay, MoMo, COD), khuyến mãi, đánh giá và quản trị toàn hệ thống.

## Tính năng chính

- **Xác thực & phân quyền:** đăng ký, đăng nhập, refresh token (JWT), đăng xuất. Ba vai trò: USER, SELLER, ADMIN.
- **Người bán (Seller):** đăng ký gian hàng, đăng bán sản phẩm với giá và tồn kho riêng, cập nhật thông tin gian hàng.
- **Sản phẩm & danh mục:** thông tin sản phẩm chung + nhiều người bán gắn giá/kho riêng (mô hình catalog dùng chung). Danh mục hỗ trợ phân cấp (parent–child).
- **Tìm kiếm & lọc nâng cao:** tìm kiếm theo từ khóa, lọc theo nhiều tiêu chí (giá, trạng thái, danh mục, rating, tồn kho) với JPA Specifications trên sản phẩm, đơn hàng, khuyến mãi, người dùng.
- **Địa chỉ giao hàng:** thêm, xóa địa chỉ; đánh dấu địa chỉ mặc định.
- **Giỏ hàng:** thêm, sửa số lượng, xóa item; phát hiện lệch giá (`PRICE_CHANGED`) và hết kho (`OUT_OF_STOCK`); `accept-price` làm mới snapshot; cờ `canCheckout` báo khi giỏ có vấn đề.
- **Đặt hàng:** đặt hàng từ giỏ, trừ kho an toàn (atomic), lưu snapshot giá và địa chỉ tại thời điểm đặt. Checkout trả `409` nếu giá đã thay đổi kể từ lần thêm vào giỏ.
- **Thanh toán:** VNPay và MoMo (verify chữ ký callback), COD (xác nhận đơn ngay, không cần callback gateway), in-flight lock Redis chống charge đôi khi F5 / mở tab mới.
- **Khuyến mãi:** seller tạo mã giảm giá (PERCENT / FIXED) có thời hạn cho từng listing; không cho phép hai khuyến mãi trùng lịch; `effectivePrice` tự cập nhật khi tạo, xóa, hoặc hết hạn khuyến mãi.
- **Đánh giá:** người mua đánh giá sản phẩm sau khi đơn đã `DELIVERED`; rating sản phẩm được tự động cập nhật bất đồng bộ.
- **Vòng đời đơn hàng đầy đủ:** PENDING → CONFIRMED (COD) / PAID (online) → SHIPPED → DELIVERED; có thể CANCELLED (user / job / admin) hoặc REFUNDED.
- **Scheduled jobs:** tự động hủy đơn PENDING quá hạn (30 phút) và hoàn kho; tự động hết hạn khuyến mãi và đồng bộ giá; dọn payment treo; đối soát số liệu gateway hàng ngày lúc 02:00.
- **Quản trị (Admin):** duyệt sản phẩm, khóa/mở khóa tài khoản, cập nhật trạng thái đơn hàng, tìm kiếm toàn hệ thống.

## Công nghệ

| Thành phần | Công nghệ |
|---|---|
| Ngôn ngữ | Java 17 |
| Framework | Spring Boot 3.2.5 (Web, Data JPA, Security, Validation) |
| Xác thực | JWT (jjwt 0.11.5), access token + refresh token |
| Database | MySQL (Spring Data JPA / Hibernate) |
| Cache / Lock | Redis (refresh token + payment in-flight lock) |
| Tài liệu API | Springdoc OpenAPI (Swagger UI) |
| Build | Maven |
| Khác | Lombok |

## Điểm kỹ thuật đáng chú ý

- **Trừ kho atomic:** `UPDATE ... WHERE stock >= quantity` — tránh oversell khi nhiều request đồng thời.
- **Server-side pricing:** giá luôn tính lại ở backend khi checkout; cart chỉ cache snapshot để phát hiện drift.
- **Snapshot pattern:** đơn hàng lưu giá đơn vị và địa chỉ tại thời điểm đặt; thay đổi sau không ảnh hưởng đơn cũ.
- **Payment in-flight lock (Redis SETNX, TTL 15 phút):** F5 / mở tab mới trả lại payment cũ, gateway chỉ được gọi đúng 1 lần.
- **Pessimistic lock trên Order:** chống race condition giữa `cancel` và callback thanh toán thành công.
- **Optimistic lock trên SellerProduct:** `@Version` field ngăn concurrent update.
- **Refresh token rotation:** mỗi lần refresh, token cũ bị xóa khỏi Redis và token mới được cấp.
- **Shipping discount:** subtotal ≥ 500.000 VND → giảm 50% phí vận chuyển (tính tự động bởi `ShippingService`).
- **Async rating update:** review mới trigger recalc bất đồng bộ, không chặn response.
- **Kiến trúc package-by-feature:** mỗi nghiệp vụ là một package độc lập, dễ bảo trì và mở rộng.

## Cấu trúc dự án

```
src/main/java/com/haduy/ecommerce/
├── auth/        # đăng nhập, JWT, refresh token, security config
├── user/        # thông tin người dùng, địa chỉ giao hàng
├── seller/      # đăng ký và quản lý gian hàng
├── product/     # sản phẩm, danh mục
├── offer/       # SellerProduct — giá và tồn kho của từng người bán
├── cart/        # giỏ hàng, phát hiện drift giá
├── order/       # đặt hàng, vòng đời đơn, OrderPendingTimeoutJob
├── payment/     # VNPay, MoMo, COD, in-flight lock, reconciliation job
├── promotion/   # mã khuyến mãi, ExpirePromotionJob
├── pricing/     # PricingService, ShippingService
├── review/      # đánh giá sản phẩm, async rating update
└── common/      # exception, enum, response wrapper, base entity
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

2. Cấu hình secret (chọn 1 trong 2 cách):

   **Cách A — file local (khuyến nghị cho dev):** sao chép file mẫu rồi điền giá trị thật:
   ```bash
   cp ecommerce/src/main/resources/application-local.properties.example \
      ecommerce/src/main/resources/application-local.properties
   ```
   Điền `spring.datasource.password`, `jwt.secret`, và các secret của VNPay / MoMo. File này đã được gitignore.

   **Cách B — biến môi trường:**
   ```bash
   # Linux / macOS
   export DB_PASSWORD=mat_khau_mysql_cua_ban
   export JWT_SECRET=mot_chuoi_base64_du_dai
   export VNPAY_HASH_SECRET=...
   export VNPAY_TMN_CODE=...
   export MOMO_SECRET_KEY=...
   export MOMO_ACCESS_KEY=...

   # Windows PowerShell
   $env:DB_PASSWORD="mat_khau_mysql_cua_ban"
   $env:JWT_SECRET="mot_chuoi_base64_du_dai"
   ```
   Để tạo JWT_SECRET ngẫu nhiên: `openssl rand -base64 64`

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
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/api/auth/register` | Đăng ký tài khoản | Public |
| POST | `/api/auth/login` | Đăng nhập, nhận JWT | Public |
| POST | `/api/auth/refresh-token` | Làm mới access token | Public |
| POST | `/api/auth/logout` | Đăng xuất | Public |

### Người dùng — `/api/users`
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/users/me` | Thông tin tài khoản hiện tại | USER |
| GET | `/api/users/me/addresses` | Danh sách địa chỉ giao hàng | USER |
| POST | `/api/users/me/addresses` | Thêm địa chỉ | USER |
| DELETE | `/api/users/me/addresses/{id}` | Xóa địa chỉ | USER |

### Sản phẩm — `/api/products`
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/products` | Tìm kiếm sản phẩm (keyword, categoryId, brand, minPrice, maxPrice, minRating, inStock) | Public |
| GET | `/api/products/{id}` | Chi tiết sản phẩm | Public |
| GET | `/api/products/{id}/sellers` | Danh sách seller của sản phẩm | Public |
| GET | `/api/products/{id}/reviews` | Đánh giá của sản phẩm | Public |
| POST | `/api/products` | Tạo sản phẩm mới (trạng thái PENDING chờ duyệt) | SELLER |

### Người bán — `/api/seller`
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/api/seller/register` | Đăng ký gian hàng | SELLER |
| GET | `/api/seller/me` | Thông tin gian hàng của mình | SELLER |
| PUT | `/api/seller/me` | Cập nhật gian hàng | SELLER |
| GET | `/api/seller/{id}` | Xem thông tin gian hàng | Public |
| POST | `/api/seller/products` | Đăng bán sản phẩm (basePrice, stock, shippingFee) | SELLER |
| PUT | `/api/seller/products/{id}` | Cập nhật listing | SELLER |
| GET | `/api/seller/products` | Danh sách listing của mình (productId, status, inStock) | SELLER |
| POST | `/api/seller/promotions` | Tạo khuyến mãi (PERCENT / FIXED, startAt, endAt) | SELLER |
| GET | `/api/seller/promotions` | Danh sách khuyến mãi (sellerProductId, lifecycle) | SELLER |
| DELETE | `/api/seller/promotions/{id}` | Xóa khuyến mãi | SELLER |

### Giỏ hàng — `/api/cart`
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| GET | `/api/cart` | Xem giỏ (kèm status từng item, canCheckout) | USER |
| POST | `/api/cart/items` | Thêm vào giỏ | USER |
| PUT | `/api/cart/items/{id}` | Cập nhật số lượng | USER |
| DELETE | `/api/cart/items/{id}` | Xóa item | USER |
| POST | `/api/cart/items/{id}/accept-price` | Chấp nhận giá mới, xóa flag PRICE_CHANGED | USER |

### Đơn hàng — `/api/orders`
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/api/orders/checkout` | Đặt hàng từ giỏ (409 nếu giá đã thay đổi) | USER |
| GET | `/api/orders` | Danh sách đơn của tôi (status, fromDate, toDate) | USER |
| GET | `/api/orders/{id}` | Chi tiết đơn | USER |
| PATCH | `/api/orders/{id}/cancel` | Hủy đơn (chỉ khi PENDING) | USER |

### Thanh toán — `/api/payments`
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/api/payments/init` | Khởi tạo thanh toán (VNPAY / MOMO / COD) | USER |
| GET | `/api/payments/order/{orderId}` | Xem trạng thái thanh toán của đơn | USER |
| POST | `/api/payments/vnpay/callback` | Callback VNPay (verify chữ ký) | Public |
| POST | `/api/payments/momo/callback` | Callback MoMo (verify chữ ký) | Public |
| POST | `/api/payments/callback` | Callback dev/test (không verify chữ ký) | Public |

### Đánh giá — `/api/reviews`
| Method | Endpoint | Mô tả | Auth |
|---|---|---|---|
| POST | `/api/reviews` | Đánh giá sản phẩm (yêu cầu đơn ở trạng thái DELIVERED) | USER |

### Quản trị — `/api/admin` (ADMIN)
| Method | Endpoint | Mô tả |
|---|---|---|
| GET | `/api/admin/products` | Danh sách sản phẩm (keyword, categoryId, status) |
| PATCH | `/api/admin/products/{id}/approve` | Duyệt sản phẩm |
| GET | `/api/admin/orders` | Danh sách đơn hàng (userId, status, fromDate, toDate) |
| PATCH | `/api/admin/orders/{id}/status` | Cập nhật trạng thái đơn hàng |
| GET | `/api/admin/users` | Danh sách người dùng (có filter) |
| GET | `/api/admin/users/{id}` | Chi tiết người dùng |
| PATCH | `/api/admin/users/{id}/ban` | Khóa tài khoản |
| PATCH | `/api/admin/users/{id}/activate` | Mở khóa tài khoản |

## Vòng đời đơn hàng

```
                ┌─────────────────────────────────┐
                ↓                                 │ (user/job/admin)
PENDING ──→ PAID (online payment success)    CANCELLED
       └──→ CONFIRMED (COD)

PAID / CONFIRMED ──→ SHIPPED (admin)
SHIPPED ──→ DELIVERED (admin) ──→ (user có thể đánh giá)

PAID / CONFIRMED / SHIPPED ──→ REFUNDED (admin)
```

## Vòng đời thanh toán

```
PENDING ──→ PROCESSING (gateway đang xử lý)
PROCESSING ──→ SUCCESS  (callback thành công)
           └──→ FAILED  (callback thất bại)
           └──→ EXPIRED (PaymentTimeoutJob)
SUCCESS ──→ REFUNDED
```

## Scheduled Jobs

| Job | Interval | Mô tả |
|---|---|---|
| `OrderPendingTimeoutJob` | Mỗi 5 phút | Hủy đơn PENDING quá 30 phút, hoàn kho |
| `ExpirePromotionJob` | Mỗi 5 phút | Hết hạn khuyến mãi ACTIVE, đồng bộ `effectivePrice` về `basePrice` |
| `PaymentTimeoutJob` | Mỗi 5 phút | Dọn payment PROCESSING quá 30 phút, query gateway, cập nhật trạng thái |
| `PaymentReconciliationJob` | Hàng ngày 02:00 | Đối soát số charge gateway với số payment SUCCESS trong DB |

## Ghi chú

- Callback VNPay và MoMo đã có verify chữ ký. Endpoint `/api/payments/callback` chỉ dùng cho dev/test, không xác thực.
- Phí vận chuyển giảm 50% khi subtotal ≥ 500.000 VND (tính tự động bởi `ShippingService`).
- CORS cho phép: `http://localhost:3000` và `http://localhost:5173`.
- Dự án tập trung vào backend; chưa có giao diện frontend.

## Tác giả

Nguyễn Phạm Hà Duy
