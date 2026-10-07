const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_123";
const BOB_UID = "bob_456";

const [emulatorHost, emulatorPortStr] = (process.env.FIRESTORE_EMULATOR_HOST || "127.0.0.1:8085").split(":");
const emulatorPort = parseInt(emulatorPortStr, 10);

before(async () => {
  const rules = fs.readFileSync("./firestore.rules", "utf8");
  testEnv = await initializeTestEnvironment({
    projectId: PROJECT_ID,
    firestore: {
      rules,
      host: emulatorHost,
      port: emulatorPort,
    },
  });
});

beforeEach(async () => {
  await testEnv.clearFirestore();
});

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

test("Unauthenticated user cannot read or write profiles", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).get());
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).set({ userId: ALICE_UID, displayName: "Alice" }));
});

test("Alice can manage her own profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).set({
      userId: ALICE_UID,
      displayName: "Alice",
      insuranceProvider: "BlueCross",
      policyNumber: "POL-123456",
      groupNumber: "GRP-7890",
      insuranceContact: "1-800-555-0199"
    })
  );
  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).get());
});

test("Bob cannot access Alice profile", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await aliceDb.collection("users").doc(ALICE_UID).set({
    userId: ALICE_UID,
    displayName: "Alice"
  });

  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();
  await assertFails(bobDb.collection("users").doc(ALICE_UID).get());
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).set({
      userId: BOB_UID,
      displayName: "Bob"
    })
  );
});

test("Alice can log symptoms and Bob cannot access them", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const bobDb = testEnv.authenticatedContext(BOB_UID).firestore();

  // Valid symptom log
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("symptoms").doc("symp_1").set({
      userId: ALICE_UID,
      symptom: "Headache",
      severity: 6,
      notes: "Mild pain behind eyes",
      triggers: "Screen time",
      loggedAt: "2026-10-07"
    })
  );

  // Bob fails to read Alice's symptom
  await assertFails(
    bobDb.collection("users").doc(ALICE_UID).collection("symptoms").doc("symp_1").get()
  );

  // Invalid symptom: severity > 10 rejected
  await assertFails(
    aliceDb.collection("users").doc(ALICE_UID).collection("symptoms").doc("symp_bad").set({
      userId: ALICE_UID,
      symptom: "Fever",
      severity: 15
    })
  );
});

test("Alice can manage medications", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertSucceeds(
    aliceDb.collection("users").doc(ALICE_UID).collection("medications").doc("med_1").set({
      userId: ALICE_UID,
      name: "Amoxicillin",
      dosage: "500mg",
      frequency: "Twice daily",
      timesPerDay: 2,
      active: true
    })
  );
});
