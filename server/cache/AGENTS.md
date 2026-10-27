# :cache

Separate Gradle build `server/`. Package `com.lebakin.finpulse.server.cache`. A library, not a process, and not a module of the mobile project.

`openRedis(endpoint)` opens one Redis connection. `Redis.set` writes one value. `Redis.get` reads it, and a missing key returns null. The password is omitted from `toString` and from exception text. The module does not start Redis, does not store a refresh token, does not apply a rate limit, and does not call `:api-gateway`. `finpulse.cloud.enabled` stays false. No Redis client library is on the classpath.

The test starts `redis-server` on `127.0.0.1`. `FINPULSE_REDIS` selects that executable when it is not on `PATH`.

Check: `scripts/check.sh -p server :cache:test :cache:detekt :cache:licensee`.
