const {
  initializeTestEnvironment,
  assertFails,
  assertSucceeds,
} = require("@firebase/rules-unit-testing");
const { test, before, after, beforeEach } = require("node:test");
const fs = require("node:fs");

let testEnv;
const PROJECT_ID = process.env.GCP_PROJECT || "demo-no-project";
const ALICE_UID = "alice_uid_123";
const BOB_UID = "bob_uid_456";

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

after(async () => {
  if (testEnv) {
    await testEnv.cleanup();
  }
});

beforeEach(async () => {
  if (testEnv) {
    await testEnv.clearFirestore();
  }
});

test("Unauthenticated user cannot list, read, or write products", async () => {
  const unauthDb = testEnv.unauthenticatedContext().firestore();
  await assertFails(unauthDb.collection("users").doc(ALICE_UID).collection("products").get());
  await assertFails(
    unauthDb.collection("users").doc(ALICE_UID).collection("products").doc("prod-1").set({
      id: "prod-1",
      name: "Teclado mecánico",
      description: "Switches táctiles",
      priceCop: 220000,
      quantity: 12,
      updatedAt: new Date(Date.now() - 1000),
      isDeleted: false,
    })
  );
});

test("Owner can create, update via set, list, and tombstone delete product", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const productRef = aliceDb.collection("users").doc(ALICE_UID).collection("products").doc("prod-uuid-1");

  await assertSucceeds(
    productRef.set({
      id: "prod-uuid-1",
      name: "Teclado mecánico",
      description: "Switches táctiles",
      priceCop: 220000,
      quantity: 12,
      updatedAt: new Date(Date.now() - 2000),
      isDeleted: false,
    })
  );

  // Sync push overwrites existing doc via .set() with same id
  await assertSucceeds(
    productRef.set({
      id: "prod-uuid-1",
      name: "Teclado mecánico RGB",
      description: "Switches táctiles",
      priceCop: 250000,
      quantity: 15,
      updatedAt: new Date(Date.now() - 1000),
      isDeleted: false,
    })
  );

  // Tombstone update
  await assertSucceeds(
    productRef.set({
      id: "prod-uuid-1",
      name: "Teclado mecánico RGB",
      description: "Switches táctiles",
      priceCop: 250000,
      quantity: 15,
      updatedAt: new Date(Date.now() - 500),
      isDeleted: true,
    })
  );

  await assertSucceeds(aliceDb.collection("users").doc(ALICE_UID).collection("products").get());
});

test("Cross-user access is denied", async () => {
  await testEnv.withSecurityRulesDisabled(async (ctx) => {
    await ctx.firestore().collection("users").doc(BOB_UID).collection("products").doc("bob-prod-1").set({
      id: "bob-prod-1",
      name: "Mouse",
      description: "Inalámbrico",
      priceCop: 79000,
      quantity: 5,
      updatedAt: new Date(Date.now() - 5000),
      isDeleted: false,
    });
  });

  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("products").get());
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("products").doc("bob-prod-1").get());
  await assertFails(
    aliceDb.collection("users").doc(BOB_UID).collection("products").doc("bob-prod-1").update({
      priceCop: 1,
      updatedAt: new Date(Date.now() - 1000),
    })
  );
  await assertFails(aliceDb.collection("users").doc(BOB_UID).collection("products").doc("bob-prod-1").delete());
});

test("Invalid payloads, shadow fields, and future timestamps are rejected", async () => {
  const aliceDb = testEnv.authenticatedContext(ALICE_UID).firestore();
  const col = aliceDb.collection("users").doc(ALICE_UID).collection("products");

  // Shadow field (e.g. pending should never be uploaded)
  await assertFails(
    col.doc("prod-2").set({
      id: "prod-2",
      name: "Monitor",
      description: "27 pulgadas",
      priceCop: 990000,
      quantity: 3,
      updatedAt: new Date(Date.now() - 1000),
      isDeleted: false,
      pending: true,
    })
  );

  // Price <= 0
  await assertFails(
    col.doc("prod-3").set({
      id: "prod-3",
      name: "Monitor",
      description: "27 pulgadas",
      priceCop: 0,
      quantity: 3,
      updatedAt: new Date(Date.now() - 1000),
      isDeleted: false,
    })
  );

  // Blank name
  await assertFails(
    col.doc("prod-4").set({
      id: "prod-4",
      name: "",
      description: "Sin nombre",
      priceCop: 10000,
      quantity: 1,
      updatedAt: new Date(Date.now() - 1000),
      isDeleted: false,
    })
  );

  // Mismatched id
  await assertFails(
    col.doc("prod-5").set({
      id: "other-id",
      name: "Monitor",
      description: "",
      priceCop: 10000,
      quantity: 1,
      updatedAt: new Date(Date.now() - 1000),
      isDeleted: false,
    })
  );

  // Future timestamp
  await assertFails(
    col.doc("prod-6").set({
      id: "prod-6",
      name: "Monitor",
      description: "",
      priceCop: 10000,
      quantity: 1,
      updatedAt: new Date(Date.now() + 60000),
      isDeleted: false,
    })
  );
});
