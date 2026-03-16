package com.sumit.genaiqna.ingestion;

import org.springframework.stereotype.Component;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
public class IngestionQueue {

    private final BlockingQueue<IngestionJob> queue =
            new LinkedBlockingQueue<>();

    public void submit(IngestionJob job) {
        queue.offer(job);
    }

    public IngestionJob take() throws InterruptedException {
        return queue.take();
    }
}
