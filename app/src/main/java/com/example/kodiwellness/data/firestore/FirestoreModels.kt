package com.example.kodiwellness.data.firestore

import com.google.firebase.Timestamp

data class FirestoreUserProfile(
    val userId: String = "",
    val displayName: String = "",
    val email: String = "",
    val insuranceProvider: String = "",
    val policyNumber: String = "",
    val groupNumber: String = "",
    val insuranceContact: String = "",
    val subscriberName: String = "",
    val planType: String = "",
    val updatedAt: Timestamp? = null
)

data class FirestoreSymptomLog(
    val id: String = "",
    val userId: String = "",
    val symptom: String = "",
    val severity: Double = 1.0,
    val notes: String = "",
    val triggers: String = "",
    val loggedAt: String = "",
    val createdAt: Timestamp? = null
)

data class FirestoreMedication(
    val id: String = "",
    val userId: String = "",
    val name: String = "",
    val dosage: String = "",
    val frequency: String = "",
    val timesPerDay: Double = 1.0,
    val instructions: String = "",
    val active: Boolean = true,
    val createdAt: Timestamp? = null
)
