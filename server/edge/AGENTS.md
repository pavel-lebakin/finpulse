# :edge

Separate Gradle build `server/`. Package `com.lebakin.finpulse.server.edge`. A library, not a process, and not a module of the mobile project.

`openEdge(tunnel, shield)` returns `Edge` only when DNS is proxied, the origin is `127.0.0.1:8080`, TLS is `full-strict`, and HSTS, WAF, and bot protection are on. The rate limit is 100 requests in 60 seconds. `cacheControl(PublicFile)` is `public, max-age=86400`. `cacheControl(Receipt)` is `private, no-store`. The tunnel token and the Origin CA are omitted from `toString` and from exception text. `Edge` does not keep those secrets. The module does not start cloudflared, does not call Cloudflare, does not store a receipt, and does not call `:api-gateway`. `finpulse.cloud.enabled` stays false.

Check: `scripts/check.sh -p server :edge:test :edge:detekt :edge:licensee`.
