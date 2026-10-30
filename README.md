# Docker Compose for testing

Example (and exercise) for the [Software Process Engineering](https://unibo-spe.github.io/) course
at the University of Bologna: testing code that relies on a database,
by starting the database with [Docker Compose](https://docs.docker.com/compose/) right before the tests,
and stopping it right after them.

> Slides: [Containerization, "Docker Compose for testing"](https://unibo-spe.github.io/09-containerization/#/compose-testing-walkthrough)

## Branches

| Branch     | Content                                                                 |
|------------|-------------------------------------------------------------------------|
| `exercise` | the exercise: the test class is full of `TODO()`s, to be completed      |
| `master`   | the solution                                                            |

## Requirements

- JDK 17 or later
- Docker, with the Compose plugin (`docker compose version` should work), and the Docker daemon running

## How to run

```bash
./gradlew test
```

The test output includes the logs of `docker compose` and of the MariaDB container.

## The exercise

The system under test is `SqlCustomerRepository`, a repository of customers backed by a SQL database (MariaDB).
Its test, [`TestMariaDBCustomerRepository`](src/test/kotlin/it/unibo/spe/compose/TestMariaDBCustomerRepository.kt),
needs a running MariaDB instance, to be started via Docker Compose __before all tests__,
and stopped __after all tests__.

Your job is to complete the `companion object` of `TestMariaDBCustomerRepository`, following the `TODO`s therein:

1. `PORT`: find a free port on the host
2. `createComposeFile`: generate the actual Compose file from the
   [`docker-compose.yml.template`](src/test/resources/it/unibo/spe/compose/docker-compose.yml.template) resource,
   into a fresh temporary directory
3. `executeDockerCompose`: call `docker compose` from Kotlin, via `ProcessBuilder`
4. `setUpMariaDb` / `tearDownMariaDb`: start and stop the stack, with the right `docker compose` options
5. run the tests (`./gradlew test`): do they pass _reliably_? If not, fix the template

Nothing outside the test class and the template needs to be changed.

### Questions to answer along the way

- Why is the Compose file generated from a _template_, instead of being a static file?
- Why is it written into a fresh temporary _directory_, at each run?
  (Hint: how does Docker Compose name the containers, networks, and volumes it creates?)
- Why should the host port not be hard-coded?
  (Hint: what if two runs of the test suite happen in parallel, on the same machine?)
- What does `docker compose up --wait` actually wait for?
- What is left on your machine after the tests, if `docker compose down` is called _without_ `--volumes`?

## Troubleshooting

| Symptom                                       | Likely cause                                                             |
|-----------------------------------------------|--------------------------------------------------------------------------|
| `NotImplementedError`                         | some `TODO()` is still there                                             |
| `Cannot connect to the Docker daemon`         | Docker is not running                                                    |
| `unknown shorthand flag: 'f'` / `unknown command: docker compose` | the Compose plugin is not installed (old `docker-compose` v1 only)      |
| connection errors in `createFreshTable`       | MariaDB is not ready yet: see step 5                                     |
| `port is already allocated`                   | another process grabbed the chosen port in the meanwhile: just re-run    |

Leftovers of interrupted runs can be found with `docker ps -a` and `docker volume ls`
(look for names starting with `docker-compose-testing`).

## Going further

[Testcontainers](https://testcontainers.com/) is a library implementing this very pattern,
also [for Docker Compose files](https://java.testcontainers.org/modules/docker_compose/).
