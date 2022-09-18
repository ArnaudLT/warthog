package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.Service;
import javafx.concurrent.WorkerStateEvent;
import org.arnaudlt.warthog.model.util.PoolService;

public abstract class AbstractMonitoredService<T> extends Service<T> {


    private final PoolService poolService;


    protected AbstractMonitoredService(PoolService poolService) {
        this.poolService = poolService;
    }


    @Override
    public void start() {

        setExecutor(this.poolService.getExecutor());
        this.poolService.registerService(this);
        this.addEventHandler(WorkerStateEvent.WORKER_STATE_SUCCEEDED, event -> this.poolService.deregisterService(this));
        this.addEventHandler(WorkerStateEvent.WORKER_STATE_FAILED, event -> this.poolService.deregisterService(this));
        this.addEventHandler(WorkerStateEvent.WORKER_STATE_CANCELLED, event -> this.poolService.deregisterService(this));

        super.start();
    }

}
