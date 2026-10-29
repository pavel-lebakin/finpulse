# :storage

Separate Gradle build `server/`. Package `com.lebakin.finpulse.server.storage`. A library, not a process, and not a module of the mobile project.

`openR2(endpoint)` opens one S3 client. `R2.put` writes one object with `Cache-Control: private, no-store`. `R2.sign` returns a GET URL that expires in 60 seconds. The library does not return object bytes. The password is omitted from `toString` and from exception text. The module does not start MinIO, does not store a receipt, and does not call `:api-gateway`. `finpulse.cloud.enabled` stays false. No S3 client library is on the classpath.

The test starts MinIO on `127.0.0.1`. `FINPULSE_MINIO` selects the `minio` executable when it is not on `PATH`. The binary is the community server. An AIStor build that denies S3 without a license does not satisfy the test. Community release `RELEASE.2025-09-07T16-13-09Z` does.

Check: `scripts/check.sh -p server :storage:test :storage:detekt :storage:licensee`.
