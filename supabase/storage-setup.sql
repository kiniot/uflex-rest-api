-- =============================================================================
-- uFlex · Supabase Storage setup for the `media` bounded context
-- =============================================================================
-- Run this ONCE per Supabase project, in: Supabase Dashboard > SQL Editor.
-- It is idempotent: it can be re-run safely.
--
-- What it does:
--   1. Creates a PRIVATE bucket `uflex-media` with size/MIME limits.
--   2. Leaves storage.objects locked down (default-deny) — see the RLS note.
--   3. Creates the `media_assets` table required by the backend in PROD
--      (where Hibernate runs with ddl-auto: validate and will NOT create it).
--
-- Architecture: the Spring Boot backend uses the service_role key to mint
-- short-lived SIGNED upload/download URLs. Clients (web/mobile) upload the bytes
-- DIRECTLY to Supabase using those URLs. Neither the service_role key nor any
-- Supabase key is ever shipped to clients.
-- =============================================================================


-- -----------------------------------------------------------------------------
-- 1. Storage bucket (private)
-- -----------------------------------------------------------------------------
-- file_size_limit is in BYTES. 524288000 = 500 MB (matches SUPABASE_MAX_VIDEO_BYTES).
insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values (
    'uflex-media',
    'uflex-media',
    false,                       -- private: objects are only reachable via signed URLs
    524288000,                   -- 500 MB hard cap at the storage layer
    array[
        'image/jpeg',
        'image/png',
        'image/webp',
        'image/heic',
        'image/heif',
        'video/mp4',
        'video/quicktime',
        'video/webm'
    ]
)
on conflict (id) do update
    set public            = excluded.public,
        file_size_limit   = excluded.file_size_limit,
        allowed_mime_types = excluded.allowed_mime_types;


-- -----------------------------------------------------------------------------
-- 2. Row Level Security note
-- -----------------------------------------------------------------------------
-- `storage.objects` has RLS ENABLED by default in Supabase. With NO policies for
-- the `anon` / `authenticated` roles, all direct client access is DENIED — which
-- is exactly what we want, because:
--   * the backend authenticates with the service_role key, which BYPASSES RLS;
--   * signed upload/download URLs carry their own token and also BYPASS RLS.
--
-- => Do NOT add permissive policies. The default-deny posture is correct for the
--    signed-URL flow and keeps clinical media private.
--
-- (Only if you later migrate clients to Supabase Auth and want direct uploads
--  would you add per-user policies here. That is intentionally out of scope.)


-- -----------------------------------------------------------------------------
-- 3. Application table: media_assets
-- -----------------------------------------------------------------------------
-- In dev (ddl-auto: update) Hibernate creates this automatically. In prod
-- (ddl-auto: validate) it must already exist, so we create it here. Column names
-- and types match the JPA mapping of MediaAsset (snake_case, pluralized table).
create table if not exists public.media_assets (
    id                   uuid          not null,
    owner_type           varchar(40)   not null,
    owner_id             uuid,
    media_type           varchar(10)   not null,
    status               varchar(10)   not null,
    bucket               varchar(100)  not null,
    object_path          varchar(512)  not null,
    content_type         varchar(150)  not null,
    original_file_name   varchar(255),
    size_bytes           bigint,
    clinic_id            uuid          not null,
    uploaded_by_user_id  uuid          not null,
    created_at           timestamp(6)  not null,
    updated_at           timestamp(6)  not null,
    constraint pk_media_assets primary key (id)
);

-- Speeds up "list media by owner within a clinic" (GET /api/v1/media?ownerType=&ownerId=).
create index if not exists ix_media_assets_owner
    on public.media_assets (owner_type, owner_id, clinic_id, status);

create index if not exists ix_media_assets_clinic
    on public.media_assets (clinic_id);
