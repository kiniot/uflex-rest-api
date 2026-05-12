# Problema y Solución: Asignación de TenantId

## El Problema Original

### Error
```
java.lang.IllegalStateException: Tenant ID is required but not present
```

### Causa Raíz

Al crear un `ClinicAdmin` via `POST /api/v1/clinic-admins`, el código en `ClinicAdminCommandServiceImpl.handle()` intentaba obtener el `clinicId` del usuario autenticado:

```java
var clinicId = externalIamService.fetchCurrentClinicId()
        .orElseThrow(() -> new ClinicNotFoundException("Current clinic not found"));
```

Este `fetchCurrentClinicId()` obtiene el `tenantId` del token JWT del usuario actual. Pero el usuario recién registrado **NO tiene `tenantId`** todavía.

### Flujo Fallido
1. Usuario se registra (SignUp) → sin `tenantId`
2. Usuario inicia sesión (SignIn) → token SIN `tenantId`
3. Usuario llama a `POST /api/v1/clinic-admins`
4. **ERROR**: el código intenta obtener `tenantId` del usuario, pero es `null`

---

## Solución Intermedia (Workaround)

Se agregó `clinicId` **explícito** en el request de `ClinicAdmin`:

```json
{
  "clinicId": "uuid-de-la-clinica",
  "firstName": "Daniel",
  "lastName": "Crispin",
  ...
}
```

### Archivos Modificados
- `RegisterClinicAdminResource` - agregó campo `clinicId`
- `RegisterClinicAdminCommand` - agregó campo `clinicId`
- `RegisterClinicAdminCommandFromResourceAssembler` - mapea `clinicId`
- `ClinicAdminCommandServiceImpl` - usa `command.clinicId()` en lugar de `fetchCurrentClinicId()`

### Flujo con Workaround
1. SignUp → usuario sin `tenantId`
2. SignIn → token SIN `tenantId`
3. Crear Clínica → solo se guarda la clínica
4. Crear ClinicAdmin (con `clinicId` explícito) → se guardan datos personales
5. **Re-autenticar** → sign-in otra vez → token CON `tenantId`

**Problema del workaround:** Requiere que el usuario se re-autentique después de crear el ClinicAdmin para obtener un token con `tenantId`.

---

## Solución Definitiva: ClinicCreatedEventHandler en IAM

### Concepto

En lugar de asignar el `tenantId` al crear el `ClinicAdmin`, se asigna al crear la clínica. Así el usuario ya tiene `tenantId` inmediatamente después de crear la clínica.

### Flujo Definitivo
1. SignUp → usuario sin `tenantId`
2. SignIn → token SIN `tenantId`
3. Crear Clínica → `ClinicRegisteredEvent` → `ClinicCreatedEventHandler` → se asigna `tenantId` **INMEDIATAMENTE**
4. ¡Listo! El usuario ya tiene `tenantId`
5. Crear ClinicAdmin → funciona sin problemas (ya tiene `tenantId`)
6. **No necesita re-autenticar**

### Beneficios
| Aspecto | Workaround | Solución Definitiva |
|---------|------------|---------------------|
| Timing | Después de 2 pasos | Inmediato |
| Re-autenticación | Sí requerida | No requerida |
| UX | Más pasos | Más fluido |

---

## Cambios Implementados

### En IAM (Archivos Nuevos/Modificados)
1. **Nuevo `ClinicCreatedEventHandler`** - maneja `ClinicRegisteredEvent` y asigna `tenantId`
2. **Eliminado `ClinicAdminRegisteredEventHandler`** - ya no es necesario

### En Organization (Archivos Eliminados)
1. **Eliminado `ClinicAdminRegisteredEvent`** - ya no se usa

### En Organization (Archivos Modificados)
1. **`ClinicAdminCommandServiceImpl`** - removida publicación de evento
2. **`RegisterClinicAdminResource`** - mantiene `clinicId` explícito (necesario para asociar admin a clínica)

---

## Flujo Completo Actual

```
1. SignUp → usuario sin tenantId
2. SignIn → token SIN tenantId
3. Crear Clínica → ClinicRegisteredEvent → ClinicCreatedEventHandler → asigna tenantId
4. Crear ClinicAdmin (con clinicId explícito) → solo guarda datos personales del admin
5. ¡Listo! Usuario tiene tenantId sin necesidad de re-autenticar
```

### Nota sobre ClinicAdmin
- `ClinicAdmin` sigue siendo necesario para almacenar **datos personales del admin** (nombre, DNI, teléfono, etc.)
- El `tenantId` se asigna vía `ClinicRegisteredEvent`, no vía `ClinicAdminRegisteredEvent`

---

## Resumen de Archivos

| Contexto | Archivo | Cambio |
|----------|---------|--------|
| IAM | `ClinicCreatedEventHandler` (nuevo) | Handler que escucha `ClinicRegisteredEvent` y asigna `tenantId` |
| IAM | `ClinicAdminRegisteredEventHandler` | ELIMINADO |
| Organization | `ClinicAdminRegisteredEvent` | ELIMINADO |
| Organization | `ClinicAdminCommandServiceImpl` | Removida publicación de evento |
| Organization | `ClinicAdmin` | Se mantiene (solo datos personales) |
