package cc.dodder.torrent.download.task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.Semaphore;

/***
 * Bloking executor
 *
 * @author Mr.Xu
 * @simce 2019-10-18 17:36
 **/
public class BlockingExecutor {

    private final ExecutorService exec;
    private final Semaphore semaphore;

    public BlockingExecutor(ExecutorService exec, int bound) {
        this.exec = exec;
        this.semaphore = new Semaphore(bound);
    }

    public void execute(final Runnable command)
            throws InterruptedException, RejectedExecutionException {
        semaphore.acquire();
        try {
            exec.execute(() -> {
                try {
                    command.run();
                } finally {
                    semaphore.release();
                }
            });
        } catch (RejectedExecutionException e) {
            semaphore.release();
            throw e;
        }
    }

    public void shutdownNow() {
        exec.shutdownNow();
    }
}
