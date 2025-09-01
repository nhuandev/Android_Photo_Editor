# Tính Năng Thanh Toán Premium - Hướng Dẫn Sử Dụng

## Tổng Quan
Tính năng thanh toán premium cho phép người dùng mua các gói subscription để truy cập các tính năng nâng cao của ứng dụng. Hệ thống sử dụng Google Play Billing Library để xử lý thanh toán.

## Các Gói Premium

### 1. Gói Tuần (Weekly)
- **ID**: `premium_weekly`
- **Giá**: 49.000đ/tuần
- **Thời hạn**: 7 ngày
- **Loại**: Subscription

### 2. Gói Năm (Yearly)
- **ID**: `premium_yearly`
- **Giá**: 299.000đ/năm
- **Thời hạn**: 365 ngày
- **Loại**: Subscription

### 3. Gói Trọn Đời (Lifetime)
- **ID**: `lifetime_premium`
- **Giá**: 999.000đ
- **Thời hạn**: Vĩnh viễn
- **Loại**: Subscription

## Cấu Trúc Code

### 1. PurchaseActivity.kt
Activity chính xử lý giao diện mua hàng:
- Hiển thị danh sách các gói
- Xử lý việc chọn gói
- Khởi tạo quá trình thanh toán
- Hiển thị trạng thái hiện tại

### 2. BillingHelper.kt
Helper class xử lý tương tác với Google Play Billing:
- Kết nối BillingClient
- Khởi tạo quá trình mua hàng
- Xử lý kết quả mua hàng
- Xác minh và xác nhận giao dịch

### 3. PremiumManager.kt
Quản lý trạng thái premium của người dùng:
- Lưu trữ thông tin gói trong SharedPreferences
- Kiểm tra trạng thái premium
- Tính toán thời gian còn lại
- Quản lý thông tin gói

## Cách Sử Dụng

### 1. Khởi tạo BillingHelper
```kotlin
val billingHelper = BillingHelper(this)
billingHelper.startConnection {
    // BillingClient đã sẵn sàng
}
```

### 2. Khởi tạo PremiumManager
```kotlin
val premiumManager = PremiumManager(this)
```

### 3. Kiểm tra trạng thái premium
```kotlin
if (premiumManager.isPremium()) {
    // Người dùng đã có gói premium
    val premiumType = premiumManager.getPremiumType()
    val remainingDays = premiumManager.getRemainingDays()
} else {
    // Người dùng chưa có gói premium
}
```

### 4. Khởi tạo quá trình mua hàng
```kotlin
billingHelper.launchPurchase(
    activity = this,
    productId = "lifetime_premium",
    productType = "subs"
) { success, message ->
    if (success) {
        // Mua hàng thành công
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
    } else {
        // Mua hàng thất bại
        Toast.makeText(this, "Lỗi: $message", Toast.LENGTH_LONG).show()
    }
}
```

## Cấu Hình Google Play Console

### 1. Tạo sản phẩm
1. Đăng nhập Google Play Console
2. Chọn ứng dụng của bạn
3. Vào "Sản phẩm" > "Sản phẩm trong ứng dụng"
4. Tạo sản phẩm mới với ID tương ứng:
   - `premium_weekly`
   - `premium_yearly`
   - `lifetime_premium`

### 2. Cấu hình giá
- Đặt giá theo định dạng tiền tệ Việt Nam (VND)
- Cấu hình thời hạn subscription
- Thiết lập thời gian dùng thử (nếu có)

### 3. Cấu hình quyền
- Đảm bảo ứng dụng có quyền `com.android.vending.BILLING`
- Cấu hình tài khoản test để test thanh toán

## Xử Lý Lỗi

### 1. Lỗi kết nối BillingClient
```kotlin
override fun onBillingServiceDisconnected() {
    isBillingReady = false
    // Thử kết nối lại sau một khoảng thời gian
    Handler(Looper.getMainLooper()).postDelayed({
        startConnection(onReady)
    }, 5000)
}
```

### 2. Lỗi mua hàng
```kotlin
billingHelper.launchPurchase(...) { success, message ->
    if (!success) {
        when (message) {
            "Không thể khởi tạo thanh toán" -> {
                // Xử lý lỗi khởi tạo
            }
            "Không tìm thấy thông tin sản phẩm" -> {
                // Xử lý lỗi sản phẩm
            }
            else -> {
                // Xử lý lỗi khác
            }
        }
    }
}
```

## Testing

### 1. Test với tài khoản test
- Sử dụng tài khoản Google test
- Thêm tài khoản vào danh sách test
- Test các trường hợp mua hàng thành công/thất bại

### 2. Test các trường hợp
- Mua hàng thành công
- Hủy mua hàng
- Mua hàng thất bại
- Mất kết nối mạng
- Ứng dụng bị đóng giữa chừng

## Bảo Mật

### 1. Xác minh giao dịch
- Luôn xác minh giao dịch với Google Play
- Lưu trữ purchase token an toàn
- Kiểm tra tính hợp lệ của giao dịch

### 2. Bảo vệ dữ liệu
- Mã hóa thông tin nhạy cảm
- Không lưu trữ thông tin thanh toán
- Sử dụng SharedPreferences với MODE_PRIVATE

## Troubleshooting

### 1. BillingClient không kết nối được
- Kiểm tra kết nối internet
- Kiểm tra quyền trong manifest
- Kiểm tra cấu hình Google Play Console

### 2. Không thể mua hàng
- Kiểm tra ID sản phẩm
- Kiểm tra cấu hình giá
- Kiểm tra tài khoản test

### 3. Gói không được kích hoạt
- Kiểm tra logic xác minh giao dịch
- Kiểm tra PremiumManager
- Kiểm tra SharedPreferences

## Tài Liệu Tham Khảo

- [Google Play Billing Library Documentation](https://developer.android.com/google/play/billing)
- [In-App Billing Best Practices](https://developer.android.com/google/play/billing/billing_best_practices)
- [Subscription Management](https://developer.android.com/google/play/billing/billing_subscriptions)
