
# economic-crime-levy-enrolment-frontend

This is the frontend microservice that enables customers to claim their Economic Crime Levy enrolment.

## Running the service

> `sbt run`

The service runs on port `14007` by default.

## Running dependencies

Using [sm2](https://github.com/hmrc/sm2)
with the service manager profile `ECONOMIC_CRIME_LEVY_ALL` will start
all of the Economic Crime Levy microservices as well as the services
that they depend on.

> `sm2 --start ECONOMIC_CRIME_LEVY_ALL`

## Running tests

### Unit tests

> `sbt test`

### Integration tests

> `sbt it:test`

### All tests

This is a sbt command alias specific to this project. It will run a scala format
check, run a scala style check, run unit tests, run integration tests and produce a coverage report.
> `sbt runAllChecks`

## Scalafmt and Scalastyle

To check if all the scala files in the project are formatted correctly:
> `sbt scalafmtCheckAll`

To format all the scala files in the project correctly:
> `sbt scalafmtAll`

To check if there are any scalastyle errors, warnings or infos:
> `sbt scalastyle`

## Feature flags

- eclAccountEnabled: Controls showing a link to account dashboard on confirmation of claim enrolment.
- enrolmentStoreProxyStubEnabled: When enabled we use the stub for the Enrolment Store Proxy service.
- enrolmentStoreProxyStubReturnsEclReference: When enabled we stub the response from the Enrolment Store Proxy service for getting enrolments for a group.
- welsh-translation: Controls whether the link to view a page in Welsh is displayed on a page.

### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").