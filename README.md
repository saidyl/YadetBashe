# یادت‌باشه 📒

اپلیکیشن مدیریت بدهکاری‌ها و طلبکاری‌ها با Kotlin، Room، Hilt و Material Design 3 — سازگار با اندروید ۷ به بالا.

## مشخصات فنی

| مورد | مقدار |
|------|-------|
| زبان | Kotlin |
| حداقل SDK | ۲۴ (اندروید ۷.۰) |
| هدف SDK | ۳۴ |
| معماری | MVVM |
| پایگاه داده | Room |
| DI | Hilt |
| ناوبری | Navigation Component + Safe Args |
| UI | Material 3 (ViewBinding) |
| یادآوری | WorkManager + Hilt Worker |
| تاریخ | ThreeTenABP + تبدیل شمسی اختصاصی |

## ساختار پروژه

```
app/src/main/java/yadetbashe/app/alisa/
├── data/
│   ├── dao/           # TransactionDao, PersonDao, ReminderDao
│   ├── model/         # Transaction, Person, Reminder
│   ├── repository/    # AppRepository (+ پشتیبان‌گیری JSON)
│   └── AppDatabase.kt
├── di/                # AppModule (Hilt)
├── ui/
│   ├── adapter/       # TransactionAdapter, PersonAdapter, ReminderAdapter
│   ├── dialogs/       # TransactionDialogFragment
│   ├── fragments/     # Transactions, People, Reports, Settings, PersonDetail
│   └── ...
├── utils/             # PersianDate, NotificationHelper, ReminderScheduler/Worker, JsonBackup
├── viewmodel/         # ViewModels با Hilt
└── MainActivity.kt / YadetBasheApp.kt
```

## قابلیت‌ها

- ✅ ثبت/ویرایش/حذف بدهکاری و طلبکاری با دسته‌بندی و یادداشت
- ✅ جستجو و فیلتر (همه / بدهکاری / طلبکاری)
- ✅ مدیریت افراد + مانده حساب خودکار هر فرد (+طلب / −بدهی)
- ✅ تاریخچه تراکنش هر فرد و حذف یادآوری‌ها
- ✅ گزارش ماهانه شمسی با نمودار میله‌ای (MPAndroidChart)
- ✅ یادآوری سررسید با WorkManager و اعلان (مجوز اندروید ۱۳+ مدیریت شده)
- ✅ پشتیبان‌گیری و بازیابی JSON
- ✅ حالت تاریک خودکار (DayNight)، اعداد و تاریخ فارسی

## اجرا

> **نکته:** فایل‌های `gradlew` و `gradle-wrapper.jar` را Android Studio هنگام اولین Sync به‌طور خودکار می‌سازد؛ اگر خط فرمان قبل از Sync لازم شد، در اندروید استودیو یک‌بار Sync اجرا کنید یا `gradle wrapper --gradle-version 8.4` را اجرا کنید.

1. پروژه را در **Android Studio (Hedgehog یا جدیدتر)** باز کنید.
2. **Gradle Sync** را اجرا کنید (نیاز به JDK 17).
3. روی شبیه‌ساز/دستگاه (API 24+) اجرا کنید.

## گرفتن فایل APK

**راه سریع (بدون نصب هیچ‌چیز):** پروژه را روی GitHub بگذارید — اکشن `Build APK` (پوشه `.github/workflows`) به‌طور خودکار هر دو APK دیباگ و ریلیس را می‌سازد؛ از بخش **Actions → Build APK → Artifacts** دانلود کنید.

**راه محلی:**

1. پروژه را در اندروید استودیو باز کنید و صبر کنید Sync تمام شود.
2. از منوی **Build > Build App Bundle(s) / APK(s) > Build APK(s)** — خروجی در این مسیرها ساخته می‌شود:
   - APK دیباگ: `app/build/outputs/apk/debug/app-debug.apk`
   - APK ریلیس: `app/build/outputs/apk/release/app-release.apk`

نکته: در این پروژه APK ریلیس به‌طور پیش‌فرض با کلید دیباگ امضا می‌شود تا مستقیم قابل نصب باشد. برای انتشار واقعی در Google Play، فایل `keystore.properties` بسازید و امضای اختصاصی تعریف کنید.

ساخت خط فرمان:

```bash
./gradlew assembleDebug      # APK دیباگ
./gradlew bundleRelease      # AAB انتشار
./gradlew test               # تست‌های واحد (تاریخ شمسی)
```

## انتشار در Google Play

1. در `app/build.gradle.kts` مقدار `versionCode`/`versionName` را افزایش دهید.
2. keystore بسازید: `keytool -genkey -v -keystore yadetbashe.jks -keyalg RSA -keysize 2048 -validity 10000 -alias yadetbashe`
3. `Build > Generate Signed Bundle / APK > Android App Bundle` را انتخاب کنید.
4. فایل AAB خروجی را در Play Console آپلود کنید (آیکون لانچر و اسکرین‌شات‌ها را آماده کنید).

## نکات فنی

- **تبدیل تاریخ شمسی**: الگوریتم استاندارد jdf در `utils/PersianDate.kt` — بدون وابستگی خارجی و با تست واحد.
- **یادآوری‌ها**: کارگر دوره‌ای روزانه (`ReminderWorker`) سررسیدهای ±۲ روز را چک می‌کند؛ بعد از بوت دستگاه با `BootReceiver` بازآرایی می‌شود.
- **پشتیبان‌گیری**: فایل JSON در `Android/data/yadetbashe.app.alisa/files/backups/` ذخیره می‌شود. (همگام‌سازی ابری می‌تواند در نسخه‌های بعد اضافه شود.)
- **KPها**: `TransactionType` شامل `DEBT`/`CREDIT` است؛ مانده = طلب‌های پرداخت‌نشده − بدهی‌های پرداخت‌نشده.
