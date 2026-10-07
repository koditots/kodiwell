package com.example.kodiwellness.data.firestore

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class FirestoreHealthRepository(private val db: FirebaseFirestore) {

    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")
    }

    suspend fun getUserProfile(userId: String): FirestoreUserProfile? {
        val path = "users/$userId"
        return try {
            val doc = db.collection("users").document(userId).get().await()
            doc.toObject(FirestoreUserProfile::class.java)
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, path)
            throw e
        }
    }

    suspend fun saveUserProfile(profile: FirestoreUserProfile) {
        val uid = requireUserId()
        val path = "users/$uid"
        try {
            val payload = hashMapOf<String, Any>(
                "userId" to uid,
                "displayName" to profile.displayName,
                "email" to profile.email,
                "insuranceProvider" to profile.insuranceProvider,
                "policyNumber" to profile.policyNumber,
                "groupNumber" to profile.groupNumber,
                "insuranceContact" to profile.insuranceContact,
                "subscriberName" to profile.subscriberName,
                "planType" to profile.planType,
                "updatedAt" to FieldValue.serverTimestamp()
            )
            db.collection("users").document(uid).set(payload).await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, path)
            throw e
        }
    }

    fun observeSymptoms(): Flow<List<FirestoreSymptomLog>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/symptoms"
        emitAll(
            db.collection("users").document(uid).collection("symptoms")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FirestoreSymptomLog::class.java) }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addSymptom(symptom: FirestoreSymptomLog): String {
        val uid = requireUserId()
        val symptomsRef = db.collection("users").document(uid).collection("symptoms")
        val docRef = if (symptom.id.isNotEmpty()) symptomsRef.document(symptom.id) else symptomsRef.document()
        val path = docRef.path
        try {
            val payload = hashMapOf<String, Any>(
                "id" to docRef.id,
                "userId" to uid,
                "symptom" to symptom.symptom,
                "severity" to symptom.severity,
                "notes" to symptom.notes,
                "triggers" to symptom.triggers,
                "loggedAt" to symptom.loggedAt,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            return docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    fun observeMedications(): Flow<List<FirestoreMedication>> = flow {
        val uid = requireUserId()
        val path = "users/$uid/medications"
        emitAll(
            db.collection("users").document(uid).collection("medications")
                .snapshots()
                .map { snapshot -> snapshot.toObjects(FirestoreMedication::class.java) }
                .catch { error ->
                    if (error is Exception) {
                        handleFirestoreError(error, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun addMedication(medication: FirestoreMedication): String {
        val uid = requireUserId()
        val medRef = db.collection("users").document(uid).collection("medications")
        val docRef = if (medication.id.isNotEmpty()) medRef.document(medication.id) else medRef.document()
        val path = docRef.path
        try {
            val payload = hashMapOf<String, Any>(
                "id" to docRef.id,
                "userId" to uid,
                "name" to medication.name,
                "dosage" to medication.dosage,
                "frequency" to medication.frequency,
                "timesPerDay" to medication.timesPerDay,
                "instructions" to medication.instructions,
                "active" to medication.active,
                "createdAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            return docRef.id
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, path)
            throw e
        }
    }

    suspend fun deleteMedication(medicationId: String) {
        val uid = requireUserId()
        val path = "users/$uid/medications/$medicationId"
        try {
            db.collection("users").document(uid).collection("medications").document(medicationId).delete().await()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, path)
            throw e
        }
    }
}
