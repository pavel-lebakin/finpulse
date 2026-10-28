# :queue

Separate Gradle build `server/`. Package `com.lebakin.finpulse.server.queue`. A library, not a process, and not a module of the mobile project.

`openKafka(endpoint)` opens one Kafka client. `Kafka.publish` writes one record. `Kafka.take` reads the latest record, and an empty topic returns null. The password is omitted from `toString` and from exception text. The module does not start Kafka, does not write an outbox, a dead-letter record, or an idempotency key, and does not call `:api-gateway`. `finpulse.cloud.enabled` stays false. The client is `org.apache.kafka:kafka-clients` 4.4.0 (Apache-2.0).

The test starts one KRaft broker on `127.0.0.1`. `FINPULSE_KAFKA` selects `kafka-server-start` when it is not on `PATH`.

Check: `scripts/check.sh -p server :queue:test :queue:detekt :queue:licensee`.
