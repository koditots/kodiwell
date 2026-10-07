package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.kodiwellness.data.firestore.FirestoreHealthRepository
import com.example.kodiwellness.data.firestore.FirestoreMedication
import com.example.kodiwellness.data.firestore.FirestoreSymptomLog
import com.example.kodiwellness.data.firestore.FirestoreUserProfile
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import org.junit.Assert.*
import org.junit.Test

class HealthRepositoryRuleTest : FirestoreEmulatorTestBase() {

    private lateinit var repository: FirestoreHealthRepository

    override fun setUpFirebase() {
        super.setUpFirebase()
        repository = FirestoreHealthRepository(firestore)
    }

    @Test
    fun unauthenticatedUser_failsWrites() = runBlocking {
        auth.signOut()
        val symptom = FirestoreSymptomLog(
            symptom = "Headache",
            severity = 4.0
        )
        try {
            repository.addSymptom(symptom)
            fail("Expected exception when unauthenticated")
        } catch (e: Exception) {
            assertTrue(e is IllegalStateException || e is FirebaseFirestoreException)
        }
    }

    @Test
    fun authenticatedUser_canWriteAndReadProfile() = runBlocking {
        val aliceUid = signInTestUser("alice_rules@example.com")
        val profile = FirestoreUserProfile(
            userId = aliceUid,
            displayName = "Alice Test",
            insuranceProvider = "KodiCare",
            policyNumber = "POL-999"
        )
        repository.saveUserProfile(profile)

        val retrieved = repository.getUserProfile(aliceUid)
        assertNotNull(retrieved)
        assertEquals("KodiCare", retrieved?.insuranceProvider)
        assertEquals("POL-999", retrieved?.policyNumber)
    }

    @Test
    fun authenticatedUser_canAddAndObserveSymptoms() = runBlocking {
        val aliceUid = signInTestUser("alice_symp@example.com")
        val symptom = FirestoreSymptomLog(
            userId = aliceUid,
            symptom = "Back Pain",
            severity = 5.0,
            notes = "Lower back stiffness",
            loggedAt = "2026-10-07"
        )
        val id = repository.addSymptom(symptom)
        assertTrue(id.isNotEmpty())

        val symptoms = repository.observeSymptoms().first()
        assertTrue(symptoms.any { it.symptom == "Back Pain" })
    }

    @Test
    fun authenticatedUser_canAddAndObserveMedications() = runBlocking {
        val aliceUid = signInTestUser("alice_med@example.com")
        val med = FirestoreMedication(
            userId = aliceUid,
            name = "Vitamin D3",
            dosage = "2000 IU",
            frequency = "Daily"
        )
        val id = repository.addMedication(med)
        assertTrue(id.isNotEmpty())

        val meds = repository.observeMedications().first()
        assertTrue(meds.any { it.name == "Vitamin D3" })
    }

    @Test
    fun crossUserAccess_isDenied() = runBlocking {
        val aliceUid = signInTestUser("alice_cross@example.com")
        val profile = FirestoreUserProfile(
            userId = aliceUid,
            displayName = "Alice Secret",
            insuranceProvider = "SecretHealth"
        )
        repository.saveUserProfile(profile)

        // Switch to Bob
        val bobUid = signInTestUser("bob_cross@example.com")
        assertNotEquals(aliceUid, bobUid)

        // Bob attempts direct read of Alice's document
        try {
            firestore.collection("users").document(aliceUid).get().await()
            fail("Bob should not be able to read Alice's profile document")
        } catch (e: Exception) {
            assertTrue("Expected permission denied, got ${e.message}", e.message?.contains("PERMISSION_DENIED") == true || e is FirebaseFirestoreException)
        }
    }
}
