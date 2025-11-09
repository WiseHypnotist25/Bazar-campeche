/**
 * Script para importar datos de ejemplo a Firestore
 *
 * PREREQUISITOS:
 * 1. Instalar Firebase CLI: npm install -g firebase-tools
 * 2. Iniciar sesión: firebase login
 * 3. Inicializar proyecto: firebase init firestore (si no lo has hecho)
 * 4. Instalar dependencias: npm install firebase-admin
 *
 * USO:
 * node import-data.js
 */

const admin = require('firebase-admin');
const fs = require('fs');
const path = require('path');

// Inicializar Firebase Admin
// Asegúrate de tener el archivo de credenciales de tu proyecto
// Descarga desde: Firebase Console > Project Settings > Service Accounts
const serviceAccount = require('./serviceAccountKey.json');

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount)
});

const db = admin.firestore();

// Función para importar tiendas
async function importStores() {
  console.log('📦 Importando tiendas...');

  const storesData = JSON.parse(
    fs.readFileSync(path.join(__dirname, 'stores.json'), 'utf8')
  );

  const batch = db.batch();
  let count = 0;

  for (const store of storesData.stores) {
    const storeRef = db.collection('stores').doc(store.id);
    batch.set(storeRef, store);
    count++;
  }

  await batch.commit();
  console.log(`✅ ${count} tiendas importadas exitosamente`);
}

// Función para importar productos
async function importProducts() {
  console.log('📦 Importando productos...');

  const productsData = JSON.parse(
    fs.readFileSync(path.join(__dirname, 'products.json'), 'utf8')
  );

  const batch = db.batch();
  let count = 0;

  for (const product of productsData.products) {
    const productRef = db.collection('products').doc(product.id);
    batch.set(productRef, product);
    count++;
  }

  await batch.commit();
  console.log(`✅ ${count} productos importados exitosamente`);
}

// Función principal
async function main() {
  try {
    console.log('🚀 Iniciando importación de datos...\n');

    await importStores();
    await importProducts();

    console.log('\n🎉 ¡Importación completada exitosamente!');
    console.log('\nRecuerda:');
    console.log('- Reemplazar REPLACE_WITH_YOUR_USER_ID en stores.json con tu userId real');
    console.log('- Las URLs de imágenes son placeholders, puedes reemplazarlas con URLs reales');

    process.exit(0);
  } catch (error) {
    console.error('❌ Error durante la importación:', error);
    process.exit(1);
  }
}

main();
