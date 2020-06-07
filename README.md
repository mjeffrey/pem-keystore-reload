# Spring Boot reloadable PEM files for TLS (and optional additional listening Ports)
As part of devops I was asked to look at directly using PEM files from our internal CA (based on FreeIPA) and letsencrypt certs.
Operations would like to be able to renew the cert and have the application pick it up automatically. Currently they need to package
the certs + keys in a PKCS12 keystore and then restart the app.

One option is to use Tomcat native (not sure if it reloads the keys/certs on file change) but this then imposes a burden on developers to install the native libraries and have some nasty glue code that needs to be maintained.
Another option that turned up in searches was this: 

### PEM-KEYSTORE github project 
* https://github.com/ctron/pem-keystore

### Blog explaining how to use with Spring Boot.
https://dentrassi.de/2018/09/25/securing-a-spring-boot-application-with-pkcs-1-pem-files/

The thing I liked about this project (from Jens Reimann) was that it was relatively transparent to Spring Boot as explained in the blog post above.
It could be used with just the inclusion of the library and registration of the Security Provider at startup.
(In our deployed environments OPs override the properties like server.ssl.key-store etc.)  

## Reloadable
The second part, how to make it reloadable. Unfortunately this makes it Tomcat specific although Tomcat is the default for Spring Boot.

Other people have had similar needs to reload SSL, the nicest was the solution from "Grim".
https://serverfault.com/questions/328533/can-tomcat-reload-its-ssl-certificate-without-being-restarted

## Multiple Ports
In our application we want to have multiple ports for securing endpoints with Mutual TLS. 
There is an example of how to do that in here as well.

## Running it
Easiest is with an IDE (I use Intellij) and with the program argument:  `--server.ssl.key-store=file:certs/qwac/keystore.properties` 

Otherwise build with maven and run from the command line
```bash
mvn clean install
java -jar  ./target/pem-0.0.1-SNAPSHOT.jar --server.ssl.key-store=file:certs/qwac/keystore.properties
``` 
To test it you can copy the pem files in certs/qwac1 and certs/qwac2 onto certs/qwac 

You can check the cert deployed with:
`openssl s_client  -connect localhost:8080 </dev/null 2>/dev/null | grep 'subject='`

## Possible Issues
I haven't tested what happens when the SSL Context is reloaded while connections are being served.
It is possible there will be errors but certificate cahnges are not frequesnt and clients neeed to be tolerant of failures.
If this is an issue for your use case then it is probably better to shutdown gracefully and have the service be restarted automatically. 

I'm not very happy with the use of WatchService to check for changes since any change in the monitored directory triggers the reload and the notification may not always work. 
I will probably replace this with a simple check to read the files themselves and check the hashes to detect change.

The reload is only for Tomcat (and it may be sensitive to version changes in Spring/Tomcat). Time will tell. If it is, we can switch to shutdown/restart.

## Licencing etc.
Without much project time for this sort of stuff I did it on may own and have "Open Sourced" it with the Apache 2 licence.
Although open source I plan to incorporate it into our applications (it is not quite production ready - no tests for example).
If you want to use it, just copy the techniques and make it your own :wink:.

