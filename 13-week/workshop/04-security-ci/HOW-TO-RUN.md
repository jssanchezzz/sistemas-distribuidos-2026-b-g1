# How to run the negative authorization test

Story covered: **only `ADMIN` or `SELLER` may register a payment in `opti-sales-api`** (threat E of the STRIDE
table). Acceptance criterion: a token with role `OPTOMETRIST` calling `POST /api/v1/invoices/{id}/payments` is
refused with `FORBIDDEN` (403), and a `SELLER` token is accepted.

The test calls the real rule of the service, `RequestRules.requireRole(http, Roles.ADMIN, Roles.SELLER)`, the same
line that guards `InvoiceController`, compiling the real sources of `opti-sales-api` from a checkout. It needs no
database and no running service.

## Requirements

- JDK 21 or newer.
- A checkout of `opti-sales-api` (change `SRC` to your path).
- JUnit 5.11.4, Spring 6.1.11 and Jakarta Servlet 6.1.0 jars from the local Maven repository.

## Run (Git Bash on Windows)

```bash
cd 13-week/workshop/04-security-ci/sales-authz-test
M=$(cygpath -m ~/.m2/repository)
SRC=$(cygpath -m /path/to/opti-sales-api)
CP="$M/org/junit/platform/junit-platform-launcher/1.11.4/junit-platform-launcher-1.11.4.jar;$M/org/junit/platform/junit-platform-engine/1.11.4/junit-platform-engine-1.11.4.jar;$M/org/junit/platform/junit-platform-commons/1.11.4/junit-platform-commons-1.11.4.jar;$M/org/junit/jupiter/junit-jupiter-engine/5.11.4/junit-jupiter-engine-5.11.4.jar;$M/org/junit/jupiter/junit-jupiter-api/5.11.4/junit-jupiter-api-5.11.4.jar;$M/org/opentest4j/opentest4j/1.3.0/opentest4j-1.3.0.jar;$M/org/apiguardian/apiguardian-api/1.0.0/apiguardian-api-1.0.0.jar;$M/jakarta/servlet/jakarta.servlet-api/6.1.0/jakarta.servlet-api-6.1.0.jar;$M/org/springframework/spring-web/6.1.11/spring-web-6.1.11.jar;$M/org/springframework/spring-core/6.1.11/spring-core-6.1.11.jar;$M/org/springframework/spring-jcl/6.1.11/spring-jcl-6.1.11.jar;$M/com/fasterxml/jackson/core/jackson-annotations/2.21/jackson-annotations-2.21.jar"
SP="$SRC/sales-adapters/src/main/java;$SRC/sales-core/src/main/java"
OUT=$(cygpath -m /tmp/w13out); mkdir -p /tmp/w13out
javac --release 21 -cp "$CP" -sourcepath "$SP" -d "$OUT" src/test/java/co/edu/corhuila/opti/sales/authz/*.java
java -cp "$OUT;$CP" co.edu.corhuila.opti.sales.authz.RunTests
```

In the CI of `opti-sales-api` the same class runs with `mvn -B test -Dtest='*AuthzTest'` (see `../security.yml`).

## Real output on my machine

Pega aquí la salida real que te dé tu computador al correr el comando de arriba (debe decir
`2 tests successful` y `0 tests failed`).
