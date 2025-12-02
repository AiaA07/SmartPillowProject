# Local Data Storage Guide - SmartPillow Project

## Overview
The SmartPillow app uses SQLite database to store user data locally on the Android device. The database persists between app sessions, so data is retained even after closing the app.

## Database Structure

### Database Name
- `smartpillow.db` (stored in the app's private directory)

### Users Table Columns
| Column Name | Type | Description |
|------------|------|-------------|
| id | INTEGER (Primary Key) | Auto-incrementing user ID |
| username | TEXT | User's username |
| password | TEXT | User's password |
| email | TEXT | User's email address |
| phone | TEXT | User's phone number |
| gender | TEXT | User's gender |
| age | INTEGER | User's age |
| height | INTEGER | User's height (in cm) |
| weight | INTEGER | User's weight (in kg) |
| sleep_duration | INTEGER | Hours of sleep (0 by default) |
| sleep_quality | INTEGER | Sleep quality score (0 by default) |

## How to Use in Your Activities

### 1. In SignUp Activity
```java
private void handleSignUp(String username, String password, String email, String phone) {
    DatabaseManager dbManager = new DatabaseManager(this);
    try {
        dbManager.open();
        dbManager.insert(username, password, email, phone, "", 0, 0, 0, 0, 0);
        // Show success message
        Toast.makeText(this, "Account created successfully!", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
    } finally {
        dbManager.close();
    }
}
```

### 2. In LoginPage Activity
```java
private void handleLogin(String username, String password) {
    DatabaseManager dbManager = new DatabaseManager(this);
    try {
        dbManager.open();
        Cursor cursor = dbManager.fetch();
        
        boolean found = false;
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                String dbUsername = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_USERNAME));
                String dbPassword = cursor.getString(
                    cursor.getColumnIndexOrThrow(DatabaseHelper.COLUMN_PASSWORD));
                
                if (dbUsername.equals(username) && dbPassword.equals(password)) {
                    found = true;
                    // Login successful
                    startActivity(new Intent(this, HomePage.class));
                    break;
                }
            }
            cursor.close();
        }
        
        if (!found) {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        dbManager.close();
    }
}
```

### 3. In ProfilePage Activity
```java
private void updateProfile(long userId, String gender, int age, int height, int weight) {
    DatabaseManager dbManager = new DatabaseManager(this);
    try {
        dbManager.open();
        dbManager.update(userId, currentUsername, currentPassword, currentEmail, 
                        currentPhone, gender, age, height, weight, 0, 0);
        Toast.makeText(this, "Profile updated!", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        dbManager.close();
    }
}
```

### 4. In StatsPage Activity (Update Sleep Data)
```java
private void recordSleep(long userId, int sleepDuration, int sleepQuality) {
    DatabaseManager dbManager = new DatabaseManager(this);
    try {
        dbManager.open();
        // Fetch current user data first
        Cursor cursor = dbManager.fetch();
        // Extract other data and update with sleep info
        dbManager.update(userId, username, password, email, phone, gender, 
                        age, height, weight, sleepDuration, sleepQuality);
        Toast.makeText(this, "Sleep data recorded!", Toast.LENGTH_SHORT).show();
    } catch (Exception e) {
        e.printStackTrace();
    } finally {
        dbManager.close();
    }
}
```

## Key Points to Remember

✅ **Always remember to:**
1. Call `dbManager.open()` before any database operation
2. Call `dbManager.close()` in the finally block to release resources
3. Use try-catch-finally to handle exceptions properly
4. Close cursors after use: `cursor.close()`

❌ **Avoid:**
1. Leaving database connections open
2. Storing sensitive data like passwords in plaintext (consider hashing)
3. Not checking if data exists before operations
4. Performing database operations on the main thread for large datasets

## Data Storage Location

The SQLite database file is stored in:
```
/data/data/com.example.smartpillow/databases/smartpillow.db
```

This is the app's private directory and is NOT accessible to other apps.

## Security Notes

⚠️ **Important:** The current implementation stores passwords in plaintext. For production apps, you should:
- Hash passwords using a secure algorithm (bcrypt, scrypt)
- Use encrypted SharedPreferences for sensitive data
- Consider using Android Keystore for encryption keys

## Debugging

To view the database using Android Studio:
1. Go to Device File Explorer
2. Navigate to `/data/data/com.example.smartpillow/databases/smartpillow.db`
3. Right-click → "Save As" to export and inspect using DB Browser for SQLite
