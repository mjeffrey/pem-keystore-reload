package be.sysa.pem.reload;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.boot.autoconfigure.web.servlet.ServletWebServerFactoryCustomizer;
import org.springframework.core.env.Environment;

@Slf4j
public class MultiPortReloadableServletFactory extends ReloadableServletFactory {

    public static ReloadableServletFactory createWithAdditionalPorts(Environment environment, ServletWebServerFactoryCustomizer customizer, int... ports) {
        ReloadableServletFactory factory = ReloadableServletFactory.create(environment);
        addPorts(factory, customizer, ports);
        return factory;
    }
    private static void addPorts(ReloadableServletFactory factory, ServletWebServerFactoryCustomizer customizer, int... ports) {
        customizer.customize(factory);
        Connector[] connectors = new Connector[ports.length];
        for (int i = 0; i < ports.length; i++) {
            int port = ports[i];
            Connector connector = new Connector(ReloadProtocol.class.getName());
            connector.setThrowOnFailure(true);
            factory.customizeConnector(connector);
            connector.setPort(port);
            connectors[i] = connector;
            log.info("Additional connector on port {}", port);
        }
        factory.addAdditionalTomcatConnectors(connectors);
    }

}
