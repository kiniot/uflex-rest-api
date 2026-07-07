# Media (Supabase Storage) — backend

Bounded context `com.kiniot.uflex.api.media`. Implementa el flujo de **URL firmada**
para subir imágenes/videos a un bucket **privado** de Supabase Storage.

> Guía completa (arquitectura, web y móvil): `uflex-project-report/docs/media-storage-implementation.md`.

## Puesta en marcha

1. Ejecuta `supabase/storage-setup.sql` en **Supabase → SQL Editor** (crea el bucket
   `uflex-media` y la tabla `media_assets`).
2. Configura en `.env` (y en Railway para prod):

   ```dotenv
   SUPABASE_URL=https://<ref>.supabase.co
   SUPABASE_SERVICE_ROLE_KEY=<service_role secret>   # SOLO backend
   SUPABASE_STORAGE_BUCKET=uflex-media
   ```

3. Arranca y revisa los endpoints en `/scalar` (tag **Media**).

## Endpoints (`/api/v1/media`)

| Método | Path | Descripción |
|---|---|---|
| `POST` | `/uploads` | Crea asset `PENDING` + URL firmada de subida |
| `POST` | `/uploads/{id}/confirm` | Marca `UPLOADED` |
| `GET`  | `/{id}` | Asset + URL firmada de descarga |
| `GET`  | `/?ownerType=&ownerId=` | Lista assets `UPLOADED` de un dueño |
| `DELETE` | `/{id}` | Borra en Storage + BD |

`ownerType`: `PHYSIOTHERAPIST_RECORD` · `PATIENT_EVIDENCE` · `EXERCISE_VIDEO` · `PROFILE_PHOTO` · `GENERIC`.

## Estructura

```
media/
├── domain/            # MediaAsset (aggregate), value objects, commands, queries, events, services (puertos)
├── application/       # command/query services, ACL ExternalIamService
├── infrastructure/    # SupabaseStorageService (RestClient), properties, JPA repository
└── interfaces/rest/   # MediaController, resources, assemblers
```

## Notas

- La clave `service_role` se usa solo aquí para firmar URLs; los clientes nunca la reciben.
- Límites y MIME permitidos en `supabase.storage.*` (ver `application.yaml`).
- En prod (`ddl-auto: validate`) la tabla debe existir → por eso el SQL la crea.
- Integración actual:
  - `POST/PUT /api/v1/physiotherapists` usa `photoAssetId`
  - `POST/PUT /api/v1/exercises` usa `videoAssetId`
  - Las responses devuelven `assetId` + `downloadUrl` firmada
