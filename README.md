# tfa-example

Two factor Auths (2FA) example application

## Usage

### Run the application locally

#### When local leiningen and java are installed
Copy and configure `config.edn` and `smtp.properties` from provided `stencil-*` files.
Then use these docker-compose commands to bring up and manage the dev environment;
```shell
docker-compose up -d
docker-compose exec dev-server lein migratus migrate
docker-compose exec dev-server lein migratus create new-db-migrtion-name
docker-compose exec dev-server bash
docker-compose restart dev-server
docker-compose logs -f dev-server
docker-compose stop
docker-compose down
```
Once up, these URLs should be reachable in web browser.
- http://127.0.0.1:3000/swagger for Swagger UI
- http://127.0.0.1:3000/index.html for application frontend
- http://127.0.0.1:3300 for Adminer UI (to reach mysql server running within docker env)

nREPL server on port 7000 should also be available


#### Alternate when local `lein` and `java` are installed
Adjust config.edn to match your local installations, then use these commands;
```shell
lein ring server-headless
lein migratus migrate
lein migratus create new-db-migration-name
```

### Packaging and running as standalone jar

```
lein do clean, ring uberjar
java -Dconf=config.edn -jar target/server.jar
```

### Packaging as war

`lein ring uberwar`

### Packaging as docker image
Create .prod-config.edn and .prod-smtp.properties to match with the production env. Then build the docker image using this command;
```shell
docker build -t image-title-and-version .
```

## License

Copyright ©  FIXME
