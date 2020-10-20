package org.arnaudlt.warthog.model.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Component
public class UniqueIdGenerator {


    private final AtomicInteger atomicInteger;


    @Autowired
    private UniqueIdGenerator() {

        this.atomicInteger = new AtomicInteger(0);
    }


    public int getUniqueId() {

        return this.atomicInteger.getAndIncrement();
    }

}
