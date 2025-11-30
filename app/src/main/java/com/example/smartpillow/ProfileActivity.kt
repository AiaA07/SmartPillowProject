package com.example.smartpillow

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ProfileActivity : AppCompatActivity() {

    private lateinit var nameEditText: EditText
    private lateinit var bedtimeEditText: EditText
    private lateinit var wakeTimeEditText: EditText
    private lateinit var sleepGoalEditText: EditText
    private lateinit var notifSwitch: Switch
    private lateinit var saveButton: Button
    private lateinit var logoutButton: Button

    private lateinit var usernameText: TextView
    private lateinit var emailText: TextView

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // connect views
        nameEditText = findViewById(R.id.editDisplayName)
        bedtimeEditText = findViewById(R.id.editBedtime)
        wakeTimeEditText = findViewById(R.id.editWakeTime)
        sleepGoalEditText = findViewById(R.id.editSleepGoal)
        notifSwitch = findViewById(R.id.switchNotifications)
        saveButton = findViewById(R.id.buttonSaveProfile)
        logoutButton = findViewById(R.id.buttonLogout)

        usernameText = findViewById(R.id.textAccountUsername)
        emailText = findViewById(R.id.textAccountEmail)

        val prefs = getSharedPreferences("user_profile", MODE_PRIVATE)

        // 1) Show account info from FirebaseAuth
        val user = auth.currentUser
        if (user != null) {
            val email = user.email ?: "Unknown email"
            emailText.text = email

            // default username from email or uid
            val fromEmail = if (email.contains("@")) email.substringBefore("@") else user.uid
            usernameText.text = fromEmail
        } else {
            emailText.text = "Not logged in"
            usernameText.text = "—"
        }

        // 2) Load from local profile
        nameEditText.setText(prefs.getString("displayName", ""))
        bedtimeEditText.setText(prefs.getString("bedtime", "23:00"))
        wakeTimeEditText.setText(prefs.getString("wakeTime", "07:00"))
        sleepGoalEditText.setText(prefs.getInt("sleepGoalHours", 8).toString())
        notifSwitch.isChecked = prefs.getBoolean("notifEnabled", false)

        // 3) Load extra info from Firestore (override if present)
        user?.let { u ->
            firestore.collection("users")
                .document(u.uid)
                .get()
                .addOnSuccessListener { doc ->
                    if (doc != null && doc.exists()) {
                        doc.getString("displayName")?.let { nameEditText.setText(it) }
                        doc.getString("bedtime")?.let { bedtimeEditText.setText(it) }
                        doc.getString("wakeTime")?.let { wakeTimeEditText.setText(it) }

                        // Sleep goal stored as "sleepGoal" (string or number)
                        if (doc.contains("sleepGoal")) {
                            val goalAny = doc.get("sleepGoal")
                            val goalInt = when (goalAny) {
                                is Number -> goalAny.toInt()
                                is String -> goalAny.toIntOrNull()
                                else -> null
                            } ?: 8
                            sleepGoalEditText.setText(goalInt.toString())
                        }

                        doc.getBoolean("notifEnabled")?.let {
                            notifSwitch.isChecked = it
                        }

                        // username from Firestore if available
                        doc.getString("username")?.let {
                            usernameText.text = it
                        }
                    }
                }
        }

        // 4) Save button: local + Firestore
        saveButton.setOnClickListener {
            val displayName = nameEditText.text.toString().trim()
            val bedtime = bedtimeEditText.text.toString().trim()
            val wakeTime = wakeTimeEditText.text.toString().trim()
            val sleepGoal = sleepGoalEditText.text.toString().toIntOrNull() ?: 8
            val notifEnabled = notifSwitch.isChecked

            // Save locally
            with(prefs.edit()) {
                putString("displayName", displayName)
                putString("bedtime", bedtime)
                putString("wakeTime", wakeTime)
                putInt("sleepGoalHours", sleepGoal)
                putBoolean("notifEnabled", notifEnabled)
                apply()
            }

            // Save to Firestore
            val currentUser = auth.currentUser
            if (currentUser != null) {
                val data = hashMapOf(
                    "displayName" to displayName,
                    "bedtime" to bedtime,
                    "wakeTime" to wakeTime,
                    "sleepGoal" to sleepGoal,   // matches HomePage
                    "notifEnabled" to notifEnabled
                )

                firestore.collection("users")
                    .document(currentUser.uid)
                    .set(data, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(this, "Profile saved!", Toast.LENGTH_SHORT).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(this, "Error saving: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Saved locally (not logged in)", Toast.LENGTH_SHORT).show()
                finish()
            }
        }

        // 5) Logout button
        logoutButton.setOnClickListener {
            auth.signOut()
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, LoginPage::class.java))
            finishAffinity()
        }
    }
}
