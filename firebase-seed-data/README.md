# Firebase Seed Data - Importación de Datos de Ejemplo

Este directorio contiene scripts y datos para importar tiendas y productos de ejemplo a Firestore.

## Contenido

- `stores.json` - 5 tiendas de ejemplo de diferentes categorías
- `products.json` - 12 productos de ejemplo distribuidos entre las tiendas
- `import-data.js` - Script de importación usando Firebase Admin SDK
- `README.md` - Este archivo con instrucciones

## Prerequisitos

### 1. Instalar Node.js
Si no tienes Node.js instalado, descárgalo desde: https://nodejs.org/

### 2. Instalar Firebase CLI
```bash
npm install -g firebase-tools
```

### 3. Iniciar sesión en Firebase
```bash
firebase login
```

### 4. Obtener credenciales del proyecto

1. Ve a [Firebase Console](https://console.firebase.google.com/)
2. Selecciona tu proyecto
3. Ve a **Project Settings** (⚙️) > **Service Accounts**
4. Haz clic en **Generate New Private Key**
5. Guarda el archivo como `serviceAccountKey.json` en este directorio (`firebase-seed-data/`)

**⚠️ IMPORTANTE**: Nunca subas `serviceAccountKey.json` a Git. Este archivo ya está en `.gitignore`.

## Instalación

Desde el directorio `firebase-seed-data/`, ejecuta:

```bash
npm install firebase-admin
```

## Configuración

### 1. Reemplazar el User ID

Antes de importar, necesitas reemplazar `REPLACE_WITH_YOUR_USER_ID` con tu User ID real de Firebase Auth.

**Para obtener tu User ID:**

1. Abre la app en un emulador/dispositivo
2. Inicia sesión con tu cuenta
3. Ve a Firebase Console > Authentication > Users
4. Copia el **User UID** de tu usuario

**Reemplazar en `stores.json`:**

Abre `stores.json` y reemplaza todas las ocurrencias de `REPLACE_WITH_YOUR_USER_ID` con tu UID real:

```json
{
  "id": "store_001",
  "ownerId": "TU_USER_ID_AQUI",
  "name": "TechStore México",
  ...
}
```

### 2. (Opcional) Actualizar URLs de imágenes

Las URLs de imágenes actuales son placeholders de imgbb. Puedes:
- Dejarlas como están (funcionarán como placeholders)
- Reemplazarlas con URLs reales de productos

## Importación

### Método 1: Usando el script Node.js (Recomendado)

Desde el directorio `firebase-seed-data/`:

```bash
node import-data.js
```

El script importará:
- 5 tiendas a la colección `stores`
- 12 productos a la colección `products`

**Salida esperada:**
```
🚀 Iniciando importación de datos...

📦 Importando tiendas...
✅ 5 tiendas importadas exitosamente
📦 Importando productos...
✅ 12 productos importados exitosamente

🎉 ¡Importación completada exitosamente!

Recuerda:
- Reemplazar REPLACE_WITH_YOUR_USER_ID en stores.json con tu userId real
- Las URLs de imágenes son placeholders, puedes reemplazarlas con URLs reales
```

### Método 2: Usando Firebase CLI directamente

También puedes usar comandos de Firebase CLI:

```bash
# Importar tiendas
firebase firestore:import stores.json --collection stores

# Importar productos
firebase firestore:import products.json --collection products
```

## Verificación

Después de importar, verifica los datos:

### Opción A: Firebase Console
1. Ve a Firebase Console > Firestore Database
2. Deberías ver las colecciones `stores` y `products` con los datos

### Opción B: En la app
1. Abre la app Bazar
2. Ve a la pestaña **Inicio** - deberías ver los productos
3. Si iniciaste sesión con el usuario que usaste como ownerId, ve a **Vender** - deberías ver tus tiendas y productos

## Datos Incluidos

### Tiendas (5)
1. **TechStore México** - Electrónica
2. **Fashion Boutique** - Ropa y Moda
3. **Supermercado del Valle** - Alimentos
4. **Librería Universal** - Libros
5. **Deportes Extremos** - Deportes

### Productos (12)
- 3 productos de TechStore (iPhone, MacBook, AirPods, Samsung)
- 2 productos de Fashion Boutique (Chamarra, Vestido)
- 2 productos de Supermercado (Leche, Pan)
- 2 productos de Librería (Libro, Set de Colores)
- 2 productos de Deportes (Balón, Bicicleta)
- 1 producto adicional de TechStore (Samsung Galaxy)

## Solución de Problemas

### Error: "Cannot find module 'firebase-admin'"
**Solución:** Ejecuta `npm install firebase-admin` en este directorio

### Error: "ENOENT: no such file or directory, open 'serviceAccountKey.json'"
**Solución:** Asegúrate de haber descargado el archivo de credenciales y lo hayas guardado como `serviceAccountKey.json` en este directorio

### Error: "User does not have permission to access"
**Solución:** Verifica que el archivo `serviceAccountKey.json` sea del proyecto correcto de Firebase

### Las tiendas aparecen pero sin productos
**Solución:** Verifica que los `storeId` en `products.json` coincidan con los `id` en `stores.json`

### No veo las tiendas en la app en la sección "Vender"
**Solución:** Asegúrate de haber reemplazado `REPLACE_WITH_YOUR_USER_ID` con tu User ID real en `stores.json` antes de importar

## Limpieza de Datos

Si necesitas eliminar los datos de prueba:

### Opción A: Desde Firebase Console
1. Ve a Firestore Database
2. Selecciona la colección que quieres limpiar
3. Elimina los documentos manualmente

### Opción B: Script de limpieza (crear si es necesario)
Puedes crear un script similar a `import-data.js` pero que elimine los documentos por ID.

## Notas Adicionales

- Los datos incluyen ratings, imágenes, stock y precios realistas
- Algunos productos tienen descuentos (`discountPrice`)
- Los timestamps usan milisegundos desde epoch (formato compatible con Firestore)
- Las categorías son variadas para probar diferentes casos de uso

## Próximos Pasos

Después de importar los datos:
1. Prueba la búsqueda de productos
2. Prueba el filtrado por categoría
3. Verifica que las imágenes se carguen correctamente
4. Prueba agregar productos al carrito
5. Verifica que la sección de vendedor muestre las tiendas correctamente
