package com.sumit.genaiqna.ingestion;

import java.util.UUID;

public record IngestionJob(
        UUID documentId,
        String content
) {
}