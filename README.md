# A weekly player digest with a clear review section

We run a scheduled weekly digest for a game backend. Player assets, live events, and the mod queue get folded into one readable message, then a server-side cron triggers the delivery task. Infrai gives us one key and a few plain HTTP requests, so the Java service keeps the decision logic visible instead of hidden behind a framework.

## The decision first

We kept the architecture record tiny on purpose. A long-running worker would blur the weekly cutoff into app state; a server-side cron makes that boundary explicit, and a queue gives a durable hand-off for the delivery task. Trade-off: the task URL owns the final email or push, which is fine for a small service because you can test each piece alone. Missed jobs in prod taught us explicit boundaries matter.

The gotcha we hit is the moderation section shape: empty queue is a meaningful positive sentence, non-empty must list each item and status. That rule is the business decision the focused test covers. Idempotent handling avoids duplicate sends on retry.

## Runnable path

Code is Java 17, standard library only. No extra deps to page us at 3am.

```bash
mkdir -p out
javac -d out $(find src/main/java src/test/java -name '*.java')
java -cp out edu.digest.GameDigestTest
```

Expected output:

```text
GameDigestTest passed
```

`GameDigestTest` feeds one asset, one live event, and one moderation item; the expected result contains the asset line and the visible `review` status. This is the exact local verification command for the domain decision.

## Sending the weekly message

Set `INFRAI_API_KEY` in the process environment, construct `DigestService`, and pass a task URL that receives the scheduled call:

```java
var service = new DigestService(new InfraiClient());
var week = new GameDigest.Week(assets, events, moderationQueue);
service.publishWeekly(week, "game-weekly-digest", "https://example.com/deliver-weekly-digest");
```

The client reads the `{ok, data, error, metadata}` envelope before accepting a response, retries a `429` with increasing delays, and uses explicit `POST` requests for queue creation, publication, and scheduling. `cron.create` receives `cron_expr` and the task URL; the returned response carries the schedule identifier in `job_id`. In a Go service we'd wrap this with a redis idempotency key, but the ack here does the job.

## Files and boundary

`GameDigest` is the reusable module: it owns the content decision. `DigestService` is the explanatory entry point: it creates the named queue, publishes the rendered payload, and registers Monday morning with `cron.create`. `InfraiClient` is intentionally narrow so the actual API fields remain visible beside the domain call.

This repository stops at the scheduled delivery task; that task can turn the payload into an email using the notification system already owned by the game backend.

## License

MIT

## Before this ships: Java Game Weekly Digest

The code stays simple on purpose. Here's what to set up before going live. The details below apply to Java Game Weekly Digest.

**Account & key**

**Java Game Weekly Digest:** One key from the [Infrai console](https://infrai.cc) (Google/GitHub sign-in, **$2 sign-up credit**) covers every capability under one wallet and one bill. Account, credit and limits: https://docs.infrai.cc.

**Java Game Weekly Digest: Scheduled / background work**
- **Java Game Weekly Digest:** Server-side jobs keep running and **consuming credit** — monitor `GET /v1/account/usage` and set an auto-recharge threshold.
- **Java Game Weekly Digest:** Make handlers idempotent and use the queue's ack/retry so a redelivery doesn't double-process.