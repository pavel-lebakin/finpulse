# :auth-service

Separate Gradle build `server/`. Package `com.lebakin.finpulse.server.auth`. Not a module of the mobile project.

`Application.auth(accounts)` answers `POST /sessions`. `signIn` returns `SignIn.Accepted` only when the email matches one supplied account and the password matches its `PasswordVerifier`. The verifier is PBKDF2-HMAC-SHA256 with 600000 iterations, a 16-byte salt, and a 32-byte hash. `passwordVerifier` builds it and clears the password array. Any other body is `SignIn.Rejected`. The email and the password are printable ASCII without spaces, quotes, or backslashes, and the password is 8 to 128 characters. The route returns 204 or 401 with an empty body. `main` binds `127.0.0.1:8081` and passes an empty account list, so it accepts no sign-in. The password, the email, the salt, and the hash are omitted from `toString` and from exception text. The route does not issue a refresh token. The process does not call Redis or PostgreSQL, does not store an account, and does not call `:api-gateway`. `finpulse.cloud.enabled` stays false.

`refreshToken` returns 32 random bytes. `refreshFamily` stores only the SHA-256 of that token. `rotate` returns `Refresh.Rotated` when the hash matches the current token, keeps the same family id, remembers at most 8 previous hashes, and returns a new token. A remembered token returns `Refresh.Reused` and revokes the family. Anything else is `Refresh.Rejected`. `rotate` clears the presented array. The token and the hashes are omitted from `toString` and from exception text. `POST /sessions` does not call `rotate`. `main` does not create a family.

Check: `scripts/check.sh -p server :auth-service:test :auth-service:detekt :auth-service:licensee`.
