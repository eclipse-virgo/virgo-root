
package org.eclipse.virgo.nano.deployer.hot;

import java.io.File;

import org.eclipse.virgo.util.io.FileSystemChecker;
import org.eclipse.virgo.util.io.FileSystemListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Task that monitors a given directory and notifies configured {@link FileSystemListener FileSystemListeners}.
 * <p/>
 * 
 * <strong>Concurrent Semantics</strong><br />
 * 
 * Threadsafe.
 * 
 */
final class WatchTask implements Runnable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final int scanIntervalMillis;

    private final FileSystemChecker checker;

    private final File watchDir;

    WatchTask(FileSystemChecker checker, File watchDir, int scanIntervalMillis) {
        this.checker = checker;
        this.watchDir = watchDir;
        this.scanIntervalMillis = scanIntervalMillis;
    }

    /**
     * Watches the configured directory for modifications.
     */
    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            try {
                Thread.sleep(this.scanIntervalMillis);
            } catch (InterruptedException e) {
                break;
            }

            try {
                this.checker.check();
            } catch (Throwable e) {
                this.logger.error("Error watching directory '{}'", e, this.watchDir.getAbsolutePath());
            }
        }
    }
}
