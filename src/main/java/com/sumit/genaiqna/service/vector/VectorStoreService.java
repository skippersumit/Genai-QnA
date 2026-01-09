package com.sumit.genaiqna.service.vector;

import java.util.List;
import java.util.Map;

public interface VectorStoreService {

    void upsert(
            String id,
            float[] vector,
            Map<String, Object> payload
    );

    List<Map<String, Object>> search(
            float[] queryVector,
            int topK
    );
}
