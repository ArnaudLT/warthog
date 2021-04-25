package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import javafx.event.EventHandler;
import org.arnaudlt.warthog.model.util.PoolService;

public abstract class AbstractMonitoredService<T> extends Service<T> {


    private final PoolService poolService;


    protected AbstractMonitoredService(PoolService poolService) {
        this.poolService = poolService;
    }


    public void whenFailed(EventHandler<WorkerStateEvent> value) {

        this.poolService.deregisterService(this);
        this.setOnFailed(value);
    }


    public void whenCancelled(EventHandler<WorkerStateEvent> value) {

        this.poolService.deregisterService(this);
        this.setOnCancelled(value);
    }


    public void whenSucceeded(EventHandler<WorkerStateEvent> value) {

        this.poolService.deregisterService(this);
        this.setOnSucceeded(value);
    }


    public void start() {

        setExecutor(this.poolService.getExecutor());
        this.poolService.registerService(this);

        if (this.getOnSucceeded() == null) {

            setOnSucceeded(state -> this.poolService.deregisterService(this));
        }

        if (this.getOnFailed() == null) {

            setOnFailed(state -> this.poolService.deregisterService(this));
        }

        if (this.getOnCancelled() == null) {

            setOnCancelled(state -> this.poolService.deregisterService(this));
        }

        super.start();
    }

}
