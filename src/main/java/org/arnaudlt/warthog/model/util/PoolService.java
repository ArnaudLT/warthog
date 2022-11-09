package org.arnaudlt.warthog.model.util;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class PoolService {


    private final ThreadPoolExecutor executor;

    private final ObservableList<Service<?>> services;


    @Autowired
    public PoolService() {
        this.executor = new ThreadPoolExecutor(4, 4, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
        this.services = FXCollections.synchronizedObservableList(FXCollections.observableList(new ArrayList<>()));
    }


    public void registerService(Service<?> service) {

        this.services.add(service);
    }


    public void deregisterService(Service<?> service) {

        this.services.remove(service);
    }


    public ObservableList<Service<?>> getServices() {

        return services;
    }


    public ThreadPoolExecutor getExecutor() {

        return executor;
    }


    public boolean isActive() {

        return this.executor.getActiveCount() != 0;
    }


    public void shutdown() {

        try {
            log.info("Closing the executor service");
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
