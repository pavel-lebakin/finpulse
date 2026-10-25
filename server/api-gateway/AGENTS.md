# :api-gateway

Separate Gradle build `server/`. Package `com.lebakin.finpulse.server.gateway`. Not a module of the mobile project.

`Application.gateway()` answers `GET /health` with status 200 and the text `ok`. `main` binds `127.0.0.1:8080`. Any other path is 404. The process stores nothing and does not call the mobile app. `finpulse.cloud.enabled` stays false.

Check: `scripts/check.sh -p server :api-gateway:test :api-gateway:detekt :api-gateway:licensee`.
