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

## What is going on

The system under test is `SqlCustomerRepository`, a repository of customers backed by a SQL database (MariaDB).
Its test, `TestMariaDBCustomerRepository`, needs a running MariaDB instance:

1. __before all tests__ (`@BeforeClass`):
    1. the Compose file is generated from the [`docker-compose.yml.template`](src/test/resources/it/unibo/spe/compose/docker-compose.yml.template) resource,
       replacing each `__NAME__` placeholder (image version, credentials, host port) with an actual value,
       and written into a _fresh temporary directory_
    2. `docker compose up --wait` starts MariaDB, and returns only once its _healthcheck_ passes,
       i.e. once MariaDB accepts connections
2. __before each test__ (`@Before`): the `customers` table is re-created, so that tests do not influence each other
3. __each test__ (`@Test`) uses the repository, hence the database, and checks the results
4. __after all tests__ (`@AfterClass`): `docker compose down --volumes` stops and removes
   the container, the network, and the volume; then the temporary directory is deleted

## Why a template, in a temporary directory?

To allow for __parallel runs__ of the test suite on the same machine (e.g. several CI jobs on the same runner),
and to prevent one run from __polluting__ another one:

- __host ports__ are global to the machine: two stacks cannot both publish MariaDB on `3306`,
  so each run picks a random free port, and injects it into the template (`__PORT__`)
- Docker Compose names containers, networks, and volumes after the __project name__,
  which defaults to the name of the directory containing the Compose file:
  a fresh temporary directory per run means a distinct project per run,
  hence distinct containers, networks, and volumes
- credentials, too, are chosen per run (e.g. a random root password)

## Troubleshooting

| Symptom                                       | Likely cause                                                             |
|-----------------------------------------------|--------------------------------------------------------------------------|
| `Cannot connect to the Docker daemon`         | Docker is not running                                                    |
| `unknown shorthand flag: 'f'` / `unknown command: docker compose` | the Compose plugin is not installed (old `docker-compose` v1 only)      |
| connection errors in `createFreshTable`       | MariaDB is not ready yet: is the `healthcheck` in the template?          |
| `port is already allocated`                   | another process grabbed the chosen port in the meanwhile: just re-run    |

Leftovers of interrupted runs can be found with `docker ps -a` and `docker volume ls`
(look for names starting with `docker-compose-testing`).

## Going further

[Testcontainers](https://testcontainers.com/) is a library implementing this very pattern,
also [for Docker Compose files](https://java.testcontainers.org/modules/docker_compose/).
