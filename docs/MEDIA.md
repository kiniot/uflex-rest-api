# Media (Supabase Storage) - backend

Bounded context `com.kiniot.uflex.api.media`. It implements the **signed URL**
flow to upload images/videos into a **private** Supabase Storage bucket.

> Full guide (architecture, web, and mobile): `uflex-project-report/docs/media-storage-implementation.md`.

## Setup

1. Run `supabase/storage-setup.sql` in **Supabase -> SQL Editor** (it creates the
   `uflex-media` bucket and the `media_assets` table).
2. Configure these values in `.env` (and in Railway for production):

   ```dotenv
   SUPABASE_URL=https://<ref>.supabase.co
   SUPABASE_SERVICE_ROLE_KEY=<service_role secret>   # BACKEND ONLY
   SUPABASE_STORAGE_BUCKET=uflex-media
   ```

3. Start the app and inspect the endpoints in `/scalar` (tag **Media**).

## Endpoints (`/api/v1/media`)

| Method | Path | Description |
|---|---|---|
| `POST` | `/uploads` | Creates a `PENDING` asset and returns a signed upload URL |
| `POST` | `/uploads/{id}/confirm` | Marks the asset as `UPLOADED` |
| `GET`  | `/{id}` | Returns the asset with a signed download URL |
| `GET`  | `/?ownerType=&ownerId=` | Lists `UPLOADED` assets for an owner |
| `DELETE` | `/{id}` | Deletes the object from Storage and the DB |

`ownerType`: `PHYSIOTHERAPIST_RECORD` · `PATIENT_EVIDENCE` · `EXERCISE_VIDEO` · `PROFILE_PHOTO` · `GENERIC`.

## Structure

```text
media/
├── domain/            # MediaAsset (aggregate), value objects, commands, queries, events, services (ports)
├── application/       # command/query services, ACL ExternalIamService
├── infrastructure/    # SupabaseStorageService (RestClient), properties, JPA repository
└── interfaces/rest/   # MediaController, resources, assemblers
```

## Notes

- The `service_role` key is only used here to sign URLs; clients never receive it.
- Limits and allowed MIME types live under `supabase.storage.*` (see `application.yaml`).
- In production (`ddl-auto: validate`) the table must already exist, which is why the SQL creates it.
- Current integration:
  - `POST/PUT /api/v1/physiotherapists` uses `photoAssetId`
  - `POST/PUT /api/v1/exercises` uses `videoAssetId`
  - Responses return `assetId` + a signed `downloadUrl`
