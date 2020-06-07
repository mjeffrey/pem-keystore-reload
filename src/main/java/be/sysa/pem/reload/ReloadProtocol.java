package be.sysa.pem.reload;

import lombok.Cleanup;
import lombok.SneakyThrows;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.http11.Http11NioProtocol;
import org.apache.tomcat.util.net.AbstractJsseEndpoint;

import java.nio.file.*;

@Slf4j
// Needs to be public
public class ReloadProtocol extends Http11NioProtocol {

    private static RefreshSslConfigThread refresher;

    // Needs to be public
    public ReloadProtocol() {
        super();
        createSingletonDaemonThread();
    }

    @Override
    public void setKeyPass(String keyPass) {
        // a non-null value is needed.
        super.setKeyPass(keyPass == null ? "" : keyPass);
    }

    @Synchronized
    private void createSingletonDaemonThread() {
        if (refresher != null) {
            return;
        }
        refresher = new RefreshSslConfigThread(this.getEndpoint());
        refresher.setDaemon(true);
        refresher.setName("Certificate Reload Thread");
        refresher.start();
    }


    private static class RefreshSslConfigThread extends Thread {
        private AbstractJsseEndpoint<?, ?> abstractJsseEndpoint = null;

        public RefreshSslConfigThread(AbstractJsseEndpoint<?, ?> abstractJsseEndpoint) {
            this.abstractJsseEndpoint = abstractJsseEndpoint;
        }

        @SneakyThrows
        public void run() {
            @Cleanup WatchService watchService = FileSystems.getDefault().newWatchService();
            for (Path path : KeyStoreMonitoredPaths.getPaths()) {
                log.info("'{}' monitoring '{}' for changes", getName(), path);
                path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY);
            }

            while (true) {
                final WatchKey wk = watchService.take();
                for (WatchEvent<?> event : wk.pollEvents()) {
                    final Path changed = (Path) event.context();
                    log.info("File has changed {} ", changed);
                    abstractJsseEndpoint.reloadSslHostConfigs();
                }
                wk.reset();
            }
        }
    }
}