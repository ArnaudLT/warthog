package org.arnaudlt.projectdse;

import javafx.beans.property.SimpleIntegerProperty;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.*;

@Slf4j
@Component
public class PoolService {


    private final ThreadPoolExecutor executor;

    private final SimpleIntegerProperty tickTack;

    private final ScheduledExecutorService scheduler;

    @Autowired
    public PoolService() {
        this.executor = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.tickTack = new SimpleIntegerProperty(0);

        // TODO I need a better solution ?
        this.scheduler = Executors.newScheduledThreadPool(1);
        Runnable goTickTack = () -> this.tickTack.set(this.executor.getActiveCount());
        this.scheduler.scheduleAtFixedRate(goTickTack, 500, 500, TimeUnit.MILLISECONDS);
    }


    public ThreadPoolExecutor getExecutor() {
        return executor;
    }


    public boolean isActive() {

        return this.executor.getActiveCount() != 0;
    }


    public int getActiveCount() {

        return this.executor.getActiveCount();
    }


    public int getTickTack() {

        return tickTack.get();
    }


    public SimpleIntegerProperty tickTackProperty() {

        return tickTack;
    }


    public void setTickTack(int tickTack) {

        this.tickTack.set(tickTack);
    }


    public void shutdown() {

        try {
            log.info("Closing the executor service");
            this.scheduler.shutdownNow();
            if (!this.getExecutor().awaitTermination(200, TimeUnit.MILLISECONDS)) {
                this.getExecutor().shutdownNow();
            }
        } catch (InterruptedException e) {
            this.getExecutor().shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("Bye bye :-)");
    }
}
