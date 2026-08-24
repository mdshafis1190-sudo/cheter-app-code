# CHETER - Multi-Restaurant SaaS & Smart QR Table Ordering System

CHETER is a modern, full-stack Android & Cloud-native Smart QR Restaurant Management System built with **Kotlin**, **Jetpack Compose**, **Supabase Realtime**, and **Firebase**.

---

## 🌟 Key Features

1. **Multi-Hotel / Multi-Restaurant SaaS Architecture**
   - Dynamic hotel switching via URL parameter (`?hotel=hotel1`, `?hotel=hotel2`, etc.) and deep linking (`https://cheter.app/?hotel=hotel1&table=5`).
   - Strict tenant isolation for menu items, active orders, and kitchen display.

2. **Table-Specific QR Standees (T-1 to T-100)**
   - Digital QR generator for each individual table from Table 1 to Table 100.
   - Batch print/download tool for all 100 table stands.
   - Master Entrance QR for reception/general entry.

3. **Supabase Realtime Live Kitchen & Orders Display**
   - Instant order notification sound & visual alert when a customer orders.
   - Live order tracking statuses: `RECEIVED` ➔ `PREPARING` ➔ `READY` ➔ `SERVED` ➔ `PAID`.
   - Preparation timer with live countdown on kitchen display.

4. **Cashier, UPI QR & Bill Settlement**
   - Dynamic merchant UPI QR generation for instant customer payments.
   - Cash payment request and waiter collection confirmation flow.
   - Printable / Shareable digital GST tax invoices.

5. **Offline + Online Hybrid Engine**
   - Powered by Room local database caching with Supabase REST & WebSocket Realtime synchronization.

---

## 🚀 GitHub Setup & Push Instructions

### Step 1: Export or Clone your Project
You can push directly from your terminal:

```bash
# 1. Initialize Git repository
git init

# 2. Add all project files (.gitignore already protects build files & keystores)
git add .

# 3. Create your initial commit
git commit -m "feat: complete CHETER multi-restaurant QR ordering app"

# 4. Rename branch to main
git branch -M main

# 5. Connect to your GitHub repository
git remote add origin https://github.com/<YOUR_GITHUB_USERNAME>/<YOUR_REPOSITORY_NAME>.git

# 6. Push to GitHub
git push -u origin main
```

---

## 🛠️ Building & Running the Project

### Requirements:
- **Android Studio Ladybug (or newer)**
- **JDK 17+**
- **Android SDK API Level 34+**

### Steps:
1. Open Android Studio and select **File > Open**, then choose this project folder.
2. Allow Gradle to sync dependencies.
3. Select an emulator or connected physical Android device.
4. Click **Run (`Shift + F10`)**.

---

## ⚙️ Environment Configuration

In AI Studio / Local environment, configure your Supabase details in `app/src/main/java/com/example/supabase/SupabaseConfig.kt`:
```kotlin
object SupabaseConfig {
    const val SUPABASE_URL = "https://<your-supabase-project-id>.supabase.co"
    const val SUPABASE_ANON_KEY = "<your-supabase-anon-key>"
}
```

---

## 📄 License
This project is licensed under the MIT License.
