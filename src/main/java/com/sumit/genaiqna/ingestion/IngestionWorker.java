package com.sumit.genaiqna.ingestion;

import com.sumit.genaiqna.service.DocumentIngestionService;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public class IngestionWorker {

    private static final Logger log =
            LoggerFactory.getLogger(IngestionWorker.class);

    private final IngestionQueue queue;
    private final DocumentIngestionService ingestionService;

    private final ExecutorService executor =
            Executors.newFixedThreadPool(2);

    public IngestionWorker(
            IngestionQueue queue,
            DocumentIngestionService ingestionService
    ) {
        this.queue = queue;
        this.ingestionService = ingestionService;
    }

    @PostConstruct
    public void start() {
        log.info("Starting ingestion worker threads");

        executor.submit(() -> {
            while (true) {
                try {
                    IngestionJob job = queue.take();
                    log.info("Processing ingestion job for doc={}",
                            job.documentId());

                    ingestionService.process(job);

                    log.info("Completed ingestion for doc={}",
                            job.documentId());

                } catch (Exception e) {
                    log.error("Ingestion failed", e);
                }
            }
        });
    }
}
