# Cloudflare

`:edge` is a library in the server build. `openEdge(tunnel, shield)` checks the edge policy and returns `Edge`. The module does not start cloudflared and does not call the Cloudflare API. `:api-gateway` does not depend on it. The mobile app does not link it. `finpulse.cloud.enabled` stays false.

## DNS and the tunnel

The hostname is a DNS name, not an IP address. `proxied` must be true, so the name sits behind Cloudflare.

The only accepted origin is `127.0.0.1:8080`, the address `:api-gateway` binds. Any other host, including `0.0.0.0`, or any other port is rejected. The policy has no public listen address.

The tunnel token is a caller-supplied secret. It is omitted from `toString` and from exception text. `Edge` does not keep it.

## TLS

The only accepted mode is `full-strict`. HSTS must be on. The Origin CA material is a caller-supplied secret. It is omitted from `toString` and from exception text. `Edge` does not keep it. A newline is allowed so a PEM can be passed. A NUL byte is rejected.

The mobile client does not pin the Cloudflare leaf certificate. Cloudflare rotates that certificate. The app already allows HTTPS only.

## WAF, rate limit, and bots

WAF must be on. Bot protection must be on. The rate limit must be 100 requests in 60 seconds. A different count is rejected. This limit is the edge policy. `:cache` still does not store a rate-limit counter.

## Cache

`cacheControl(PublicFile)` returns `public, max-age=86400`. Public static files may be cached at the edge for one day.

`cacheControl(Receipt)` returns `private, no-store`. That is the same directive `:storage` writes on a private object. `:edge` does not read the object and does not store a receipt.

## What this library does not do

It does not open a tunnel, create a DNS record, or upload a receipt. It does not change `finpulse.cloud.enabled`.
