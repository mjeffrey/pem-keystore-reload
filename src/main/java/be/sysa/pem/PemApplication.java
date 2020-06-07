package be.sysa.pem;

import de.dentrassi.crypto.pem.PemKeyStoreProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.security.Security;

@EnableAsync
@SpringBootApplication
public class PemApplication {
    public static void main(String[] args) {
        Security.addProvider(new PemKeyStoreProvider());
        SpringApplication.run(PemApplication.class, args);
    }
}
