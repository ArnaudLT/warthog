package org.arnaudlt.warthog.ui.service;

import javafx.concurrent.ScheduledService;
import javafx.concurrent.Task;
import org.arnaudlt.warthog.model.util.PoolService;

public class ActiveThreadsCountService extends ScheduledService<Integer> {


    private final PoolService poolService;


    public ActiveThreadsCountService(PoolService poolService) {
        this.poolService = poolService;
    }


    @Override
    protected Task<Integer> createTask() {

        return new Task<>() {

            @Override
            protected Integer call() {

                return poolService.getExecutor().getActiveCount();
            }
        };
    }

}
