package com.sumit.genaiqna.service.prompt;

public class OutputSchema {

    public static String instruction() {
        return """
                Respond ONLY in the following JSON format:
                
                {
                  "answer": "<one sentence answer>",
                  "confidence": "LOW | MEDIUM | HIGH"
                }
                """;
    }
}
