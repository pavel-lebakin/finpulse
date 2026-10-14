# :core:network

Package `com.lebakin.finpulse.core.network`. Depends on `:core:common` and the Ktor client. No feature dependency.

Public types: `HttpsClient`, `HttpReply`, `openHttpsClient`.

`HttpsClient.post` accepts an HTTPS URL. A 2xx reply returns `HttpReply`. Any other address throws `IllegalArgumentException` before the call. A transport failure throws `NoConnectionException`. Any other status throws `ServerStatusException` and does not include the response body. `CancellationException` is rethrown. Redirects are off, so a 3xx does not continue onto cleartext. The client does not pin a certificate and does not log the body or headers. Android and JVM use OkHttp. iOS uses the Darwin engine. OkHttp pulls `org.slf4j:slf4j-api:2.0.19`, a reviewed Licensee exception because its MIT URL is not mapped. The app manifest forbids cleartext in every build.

Check: `scripts/check.sh :core:network:jvmTest :core:network:compileKotlinIosSimulatorArm64 :core:network:detekt :core:network:licensee`.
