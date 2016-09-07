This is the source code of the application shown at JavaZone 2016.

The application assumes that you have installed:

* gradle
* docker
* docker-compose
* npm
* nodejs

On top of that you should replace the conf/keycloak.json with your keyclock configuration. In order to configure keycloak I'd recomment to follow the [https://www.youtube.com/watch?v=c20igjL69Mo](video).

In order to build:

```
# first build the js client
cd web/src/main/client
npm install
./node_modules/.bin/wekpack
cd ../../..
gradle clean install

# then run
sudo docker-compose up
```

Have fun!

