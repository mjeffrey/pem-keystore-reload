package be.sysa.pem.reload;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.core.env.Environment;

@Slf4j
public class ReloadableServletFactory extends TomcatServletWebServerFactory {

    public static ReloadableServletFactory create(Environment environment) {
        setMonitoredPemFileLocations(environment);
        ReloadableServletFactory factory = new ReloadableServletFactory();
        factory.setProtocol(ReloadProtocol.class.getName());
        return factory;
    }

    @Override
    protected void customizeConnector(Connector connector) {
        super.customizeConnector(connector);
    }

    @SneakyThrows
    private static void setMonitoredPemFileLocations(Environment environment) {
        String keyStoreLocation = environment.getProperty("server.ssl.key-store");
        KeyStoreMonitoredPaths.addPaths(keyStoreLocation);
    }

}
