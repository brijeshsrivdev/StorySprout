# StorySprout local infrastructure

Docker Compose provides the local PostgreSQL metadata store and MinIO object storage used during development. Production storage should be hidden behind an application storage abstraction so S3/GCS can replace MinIO later.
