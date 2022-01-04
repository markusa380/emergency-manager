# Emergency Manager

![Scala CI](https://github.com/markusa380/emergency-manager/workflows/Scala%20CI/badge.svg?branch=master)

## Build

### Frontend

The frontend is a ScalaJS project. It requires `npm` (LTS!) and `yarn` to build.

To prepare the web assets, run

```
sbt pack
```

The resulting files can be found under `./frontend/target/scala-2.13/assets/`

### Backend

The backend can be packed into a uber JAR using `sbt-assembly` by running

```
sbt backend / assembly
```

## Configuration

All necessary configuration can be found under `./backend/src/main/resources/application.conf`.

Values that need to be changed depending on the environment can be overridden
by environment variables.

### Secrets

Configure the folder where the application looks for secrets by using `APP_SECRETS_PATH` (default `~/secrets`).

It is recommended to permanently set it to a safe directory.

### Assets

The location of the web assets folder can be configured using `APP_ASSETS_PATH`.

It is preconfigured for development and should be changed when deployed to a server.

### MongoDB

Access to the MongoDB database can be configured using the following list of environment variables.

Environment Variable|Default Value|Description
---|---|---
`APP_MONGODB_HOST`||The hostname of the MongoDB server
`APP_MONGODB_PORT`|`27017`|The port of the MongoDB server
`APP_MONGODB_DB`|`em`|The MongoDB database to connect to
`APP_MONGODB_USER`|`backend`|The name of a user defined in the configured database
`APP_MONGODB_PASSWORD_FILE`|`mongodb_backend_password.txt`|The name of a text file containing the users password

Use a command like
```
docker run -d -p 27017:27017 -v  -v ~/mongodb:/data/db -e MONGO_INITDB_ROOT_USERNAME=backend -e MONGO_INITDB_ROOT_PASSWORD=pass mongo
```
to start MongoDB in a docker container.

You currently have to manually create a user like this:

```
{
  "_id": {"$oid":"61d4a5b64543ff5852ad056b"},
  "userId": "myuser",
  "passwordHash": {"$binary":"DovnUcUFlx0tTFUyCc1yaRUHafZsLa6qpwCe+uDb5d4Ul0OqzMbgH1Gse26q62EZFLT4XnMo0EJ3Jndhtyqz7w==","$type":"0"},
  "salt": {"$binary":"YXdkYXdk","$type":"0"}
}
```

## Running

The backend project utilizes `sbt-revolver`.

Start the application by using
```
sbt backend / reStart
```

## Further information

For further information, please visit the [wiki](https://github.com/markusa380/emergency-manager/wiki).
