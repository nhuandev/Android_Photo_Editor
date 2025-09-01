# Hướng dẫn sử dụng tính năng In-App Purchase

## Tổng quan
Tính năng thanh toán in-app đã được tích hợp với Google Play Billing Library để xử lý 3 gói premium:
- **Gói Tuần** (`premium_weekly`): 49.000đ/tuần
- **Gói Năm** (`premium_yearly`): 299.000đ/năm  
- **Gói Trọn Đời** (`lifetime_premium`): 999.000đ

## Cấu trúc code

### 1. BillingHelper.kt
- Quản lý kết nối với Google Play Billing
- Xử lý các giao dịch thanh toán
- Tự động acknowledge purchase
- Lưu trạng thái premium

### 2. PremiumManager.kt
- Quản lý trạng thái premium trong SharedPreferences
- Xử lý thời gian hết hạn cho các gói
- Cung cấp thông tin hiển thị gói

### 3. PurchaseActivity.kt
- UI để chọn gói premium
- Xử lý sự kiện chọn gói
- Khởi tạo quá trình thanh toán
- **Tích hợp sẵn chức năng kiểm tra trạng thái premium**
- **Tự động kiểm tra khi mở activity và khi quay lại**

## Cách sử dụng

### 1. Khởi tạo BillingHelper
```kotlin
val billingHelper = BillingHelper(this)
billingHelper.startConnection {
    // BillingClient đã sẵn sàng
}
```

### 2. Khởi tạo thanh toán
```kotlin
billingHelper.launchPurchase(
    activity = this,
    productId = "premium_weekly", // hoặc premium_yearly, lifetime_premium
    onSuccess = { productId ->
        // Thanh toán thành công
    },
    onError = { errorMessage ->
        // Xử lý lỗi
    }
)
```

### 3. Kiểm tra trạng thái premium
```kotlin
val premiumManager = PremiumManager(this)
if (premiumManager.isPremium()) {
    // Người dùng đã có premium
    val premiumType = premiumManager.getPremiumType()
    // Hiển thị tính năng premium
}
```

## Tính năng tích hợp trong PurchaseActivity

### **Tự động kiểm tra trạng thái:**
- Khi mở `PurchaseActivity`, tự động kiểm tra xem người dùng đã có premium chưa
- Nếu đã có premium, hiển thị thông báo "Bạn đã có Premium [Loại]!"
- Khi quay lại từ thanh toán, tự động kiểm tra lại trạng thái

### **Chức năng chính:**
1. **Chọn gói**: Radio buttons để chọn 1 trong 3 gói premium
2. **Hiển thị thông tin**: Tự động cập nhật thông tin gói được chọn
3. **Thanh toán**: Nút "Tiếp tục" để khởi tạo thanh toán
4. **Kiểm tra trạng thái**: Tự động kiểm tra và thông báo trạng thái premium

## Cấu hình Google Play Console

### 1. Tạo sản phẩm
- Đăng nhập Google Play Console
- Chọn ứng dụng của bạn
- Vào "Sản phẩm" > "In-app products"
- Tạo 3 sản phẩm với ID tương ứng:
  - `premium_weekly`
  - `premium_yearly` 
  - `lifetime_premium`

### 2. Cấu hình giá
- Đặt giá cho từng gói
- Chọn loại sản phẩm: "Managed product" cho gói trọn đời, "Subscription" cho gói định kỳ
- Cập nhật giá trong `PremiumManager.getPackagePrice()` để khớp với Play Console

### 3. Test
- Sử dụng tài khoản test để kiểm tra thanh toán
- Upload APK test lên Play Console
- Test thanh toán với tài khoản test

## Xử lý lỗi thường gặp

### 1. BillingClient không kết nối được
- Kiểm tra kết nối internet
- Kiểm tra Google Play Services
- Kiểm tra quyền trong AndroidManifest.xml

### 2. Sản phẩm không tìm thấy
- Kiểm tra ID sản phẩm có khớp với Play Console
- Kiểm tra sản phẩm đã được publish
- Kiểm tra quốc gia/region

### 3. Thanh toán thất bại
- Kiểm tra tài khoản Google
- Kiểm tra phương thức thanh toán
- Kiểm tra log để debug

## Lưu ý quan trọng

1. **Acknowledge Purchase**: Luôn acknowledge purchase để tránh refund
2. **Trạng thái Premium**: Lưu trạng thái locally để offline access
3. **Thời gian hết hạn**: Xử lý đúng thời gian hết hạn cho gói định kỳ
4. **Test**: Luôn test kỹ trước khi release
5. **Compliance**: Tuân thủ chính sách của Google Play

## Tích hợp vào app

Để tích hợp tính năng premium vào app:

1. Sử dụng `PremiumManager.isPremium()` để kiểm tra quyền truy cập
2. Ẩn/hiện tính năng dựa trên trạng thái premium
3. Cập nhật UI khi trạng thái premium thay đổi
4. Xử lý restore purchase nếu cần

## Luồng hoạt động

### **Khi mở PurchaseActivity:**
1. Khởi tạo BillingHelper và PremiumManager
2. Kết nối với Google Play Billing
3. **Tự động kiểm tra trạng thái premium hiện tại**
4. Nếu đã có premium → hiển thị thông báo
5. Nếu chưa có → hiển thị giao diện chọn gói

### **Khi chọn gói và thanh toán:**
1. Người dùng chọn gói (Weekly/Yearly/Lifetime)
2. Bấm "Tiếp tục"
3. Khởi tạo thanh toán Google Play
4. Sau khi thành công → lưu trạng thái premium → đóng activity

### **Khi quay lại từ thanh toán:**
1. Tự động kiểm tra lại trạng thái premium
2. Cập nhật UI nếu cần

## Hỗ trợ
Nếu gặp vấn đề, kiểm tra:
- Log của BillingHelper
- Log của PurchaseActivity (Premium status)
- Google Play Console
- Tài liệu Google Play Billing
