## Registration No- 23BPS1185 23BPS1146

# urbanflo-sumo-server

![CI Status](https://img.shields.io/github/actions/workflow/status/igloo-4002/urbanflo-sumo-server/ci.yml?style=flat-square)
![Docker Build Status](https://img.shields.io/github/actions/workflow/status/igloo-4002/urbanflo-sumo-server/docker.yml?style=flat-square&label=docker)
![Licence badge](https://img.shields.io/github/license/igloo-4002/urbanflo-sumo-server?style=flat-square)

Interacts between [SUMO](https://www.eclipse.org/sumo/) and urbanflo app.

## Development

### Dependencies

- [JDK 17](https://adoptium.net/temurin/releases/)
- [Kotlin](https://kotlinlang.org/docs/getting-started.html)
- [Gradle](https://gradle.org/install/) (optional - wrapper included in repository)
- [SUMO](https://sumo.dlr.de/docs/Downloads.php) with `libtracijni` library

### Building

1. Clone the repository
2. Run `./gradlew build` to build

### Docker development

```shell
docker compose up --force-recreate --build
```

## Deploy

### Docker

```shell
docker compose up -d --force-recreate
```

Note: ARM64/M1 users will need to use `docker compose up -d --force-recreate --build` for now, as the pre-built image is
only for x86-64. This is because the ARM version of SUMO package does not include `libtracijni`, requiring a build from
scratch, and the CI job keeps timing out for ARM.

See [this issue](https://github.com/eclipse/sumo/issues/13702) to learn more

### Manual

```shell
java -jar build/libs/urbanflo-sumo-server-0.0.1-SNAPSHOT.jar
```

## Configuration

### TraCI configuration

You'll need `libtracijni` to be in the Java library path, or you'll get `UnsatisfiedLinkError` when starting the server. You can do this by either adding `$LIBTRACI_LIB` to `PATH` or passing `-Djava.library.path=$LIBTRACI_LIB` to the `java` command, where `$LIBTRACI_LIB` is the location of  `libtracijni` binary, such as `$SUMO_HOME/bin` if you used the official installation method on Linux. Consult the [SUMO docs](https://sumo.dlr.de/docs/Libtraci.html#java) for more information.

### Spring profiles

There are 2 configuration profiles, the default one and `prod`. The only difference is `prod` launches the server at port 80 while the default one uses port 8080.

You can select `prod` profile by passing `-Dspring.profiles.active=prod` to `java` command.

### Environment variables

- `urbanflo_FRONTEND_URL`: sets the frontend URL for CORS endpoint. Defaults to `http://localhost:5173` and is ignored when `urbanflo_ALLOW_ALL_CORS_ORIGINS` is `true`
- `urbanflo_ALLOW_ALL_CORS_ORIGINS`: if set to `true`, the server will accept all CORS origins. Defaults to `false`.

## Troubleshooting

### Build failed because of `UnsatisfiedLinkError`

See the [TraCI configuration](#traci-configuration) section on how to add `libtracijni` to Java library path.

Alternatively you can skip testing by passing `-x test` to gradle.

### Every request from frontend results in a CORS error

You need to configure CORS for your setup. See [environment variables](#environment-variables) for more information.

## Architecture 

See [ARCHITECTURE.md](ARCHITECTURE.md).

## Licence

Licensed under [MIT License](LICENSE.txt).
