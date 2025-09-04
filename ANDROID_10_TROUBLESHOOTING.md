# Hướng dẫn khắc phục sự cố cho Android 10

## Vấn đề thường gặp trên Android 10

### 1. **BillingClient không kết nối được**
**Triệu chứng:**
- Log: "BillingClient chưa sẵn sàng"
- Toast: "Thiết bị này không hỗ trợ thanh toán Google Play"

**Nguyên nhân:**
- Google Play Services không được cập nhật
- Thiết bị không có Google Play Store
- Quyền bị hạn chế

**Giải pháp:**
```bash
# Kiểm tra Google Play Services
adb shell pm list packages | grep google
adb shell pm list packages | grep play

# Kiểm tra version
adb shell dumpsys package com.google.android.gms | grep versionName
```

### 2. **Sản phẩm không tìm thấy**
**Triệu chứng:**
- Log: "Product details not found"
- Toast: "Sản phẩm không khả dụng trên thiết bị này"

**Nguyên nhân:**
- Sản phẩm chưa được publish trên Play Console
- ID sản phẩm không khớp
- Quốc gia/region không hỗ trợ

**Giải pháp:**
```kotlin
// Kiểm tra ID sản phẩm
Log.d("Billing", "Đang query sản phẩm: $productId")

// Kiểm tra response code
when (billingResult.responseCode) {
    BillingClient.BillingResponseCode.ITEM_UNAVAILABLE -> {
        // Sản phẩm không khả dụng
    }
    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
        // Dịch vụ không khả dụng
    }
}
```

### 3. **Lỗi khởi tạo thanh toán**
**Triệu chứng:**
- Log: "Failed to launch billing flow"
- Toast: "Không thể khởi tạo thanh toán"

**Nguyên nhân:**
- BillingClient chưa sẵn sàng
- Activity context không hợp lệ
- Quyền bị thiếu

**Giải pháp:**
```kotlin
// Kiểm tra BillingClient
if (billingClient == null) {
    Log.e(TAG, "BillingClient chưa được khởi tạo")
    return
}

// Kiểm tra response code
val responseCode = billingClient?.launchBillingFlow(activity, billingFlowParams)
when (responseCode) {
    BillingClient.BillingResponseCode.OK -> {
        // Thành công
    }
    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
        // Thanh toán không khả dụng
    }
    else -> {
        // Lỗi khác
    }
}
```

## Cải thiện tương thích Android 10

### 1. **Xử lý lỗi tốt hơn**
```kotlin
try {
    // Code thanh toán
} catch (e: Exception) {
    Log.e(TAG, "Lỗi thanh toán", e)
    onError("Lỗi: ${e.message}")
}
```

### 2. **Retry mechanism**
```kotlin
override fun onBillingServiceDisconnected() {
    Log.w(TAG, "Billing service disconnected, đang thử kết nối lại...")
    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
        startConnection(onReady)
    }, 5000) // Retry after 5 seconds
}
```

### 3. **UI Thread safety**
```kotlin
runOnUiThread {
    Toast.makeText(this, "Thông báo", Toast.LENGTH_SHORT).show()
    binding.btnContinue.isEnabled = true
}
```

## Kiểm tra tương thích

### 1. **Kiểm tra thiết bị**
```bash
# Kiểm tra Android version
adb shell getprop ro.build.version.release

# Kiểm tra Google Play Services
adb shell pm list packages | grep gms

# Kiểm tra quyền
adb shell dumpsys package com.google.android.gms | grep permission
```

### 2. **Kiểm tra log**
```bash
# Filter log theo tag
adb logcat | grep "BillingHelper"
adb logcat | grep "PurchaseActivity"

# Filter theo level
adb logcat *:E | grep "Billing"
```

### 3. **Test với thiết bị thật**
- Sử dụng thiết bị Android 10 thật
- Kiểm tra Google Play Store có hoạt động
- Test với tài khoản Google thật

## Cấu hình Google Play Console

### 1. **Tạo sản phẩm test**
- Tạo sản phẩm với trạng thái "Draft"
- Upload APK test
- Thêm tài khoản test

### 2. **Cấu hình quốc gia**
- Kiểm tra quốc gia hỗ trợ
- Cập nhật giá theo quốc gia
- Test với VPN nếu cần

### 3. **Test purchase**
- Sử dụng tài khoản test
- Test với các phương thức thanh toán khác nhau
- Kiểm tra email xác nhận

## Debug tips

### 1. **Log chi tiết**
```kotlin
Log.d(TAG, "Billing setup finished: ${billingResult.responseCode}")
Log.d(TAG, "Query result: ${billingResult.responseCode}, products: ${productDetailsList?.size ?: 0}")
Log.d(TAG, "Launch billing flow result: $responseCode")
```

### 2. **Error handling**
```kotlin
when (billingResult.responseCode) {
    BillingClient.BillingResponseCode.OK -> {
        // Success
    }
    BillingClient.BillingResponseCode.BILLING_UNAVAILABLE -> {
        // Device doesn't support billing
    }
    BillingClient.BillingResponseCode.SERVICE_UNAVAILABLE -> {
        // Service unavailable
    }
    else -> {
        // Other errors
    }
}
```

### 3. **State management**
```kotlin
// Disable button until ready
binding.btnContinue.isEnabled = false

// Enable when billing ready
billingHelper.startConnection {
    runOnUiThread {
        binding.btnContinue.isEnabled = true
    }
}
```

## Liên hệ hỗ trợ

Nếu vẫn gặp vấn đề:

1. **Kiểm tra log** để xác định lỗi cụ thể
2. **Test trên thiết bị khác** để loại trừ vấn đề thiết bị
3. **Kiểm tra Google Play Console** để đảm bảo cấu hình đúng
4. **Tham khảo tài liệu Google** về Google Play Billing
5. **Liên hệ Google Support** nếu cần thiết

## Tài liệu tham khảo

- [Google Play Billing Library](https://developer.android.com/google/play/billing)
- [Android 10 Compatibility](https://developer.android.com/about/versions/10)
- [Google Play Console Help](https://support.google.com/googleplay/android-developer)

