package ru.korev.springlessons;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {
    private final Semaphore semaphore;
    private final long intervalMillis;
    private final List<Instant> requestTimestamps = new ArrayList<>();
    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.semaphore = new Semaphore(requestLimit);
        this.intervalMillis = timeUnit.toMillis(1);
    }

    public void createDocument(Document document, String signature) throws InterruptedException {
        semaphore.acquire();
        try {
            removeExpiredTimestamps();

            if (requestTimestamps.size() >= semaphore.availablePermits()) {
                long sleepTime = intervalMillis - Duration.between(requestTimestamps.get(0), Instant.now()).toMillis();
                if (sleepTime > 0) {
                    Thread.sleep(sleepTime);
                }
                removeExpiredTimestamps();
            }

            sendRequest(document, signature);
            requestTimestamps.add(Instant.now());
        } finally {
            semaphore.release();
        }
    }


    private void removeExpiredTimestamps() {
        Instant cutoff = Instant.now().minusMillis(intervalMillis);
        requestTimestamps.removeIf(instant -> instant.isBefore(cutoff));
    }

    private void sendRequest(Document document, String signature) {
        System.out.println("Simulate sending request to https://ismp.crpt.ru/api/v3/lk/documents/create");
        System.out.println("Document: " + document);
        System.out.println("Signature: " + signature);
    }

    public static class Document {
        @JsonProperty("description")
        private Description description;
        @JsonProperty("doc_id")
        private String docId;
        @JsonProperty("doc_status")
        private String docStatus;
        @JsonProperty("doc_type")
        private String docType;
        @JsonProperty("importRequest")
        private boolean importRequest;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("participant_inn")
        private String participantInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        private String productionDate;
        @JsonProperty("production_type")
        private String productionType;
        @JsonProperty("products")
        private List<Product> products;
        @JsonProperty("reg_date")
        private String regDate;
        @JsonProperty("reg_number")
        private String regNumber;
        //getters and setters
    }

    public static class Description {
        @JsonProperty("participantInn")
        private String participantInn;
        //getters and setters
    }

    public static class Product {
        @JsonProperty("certificate_document")
        private String certificateDocument;

        @JsonProperty("certificate_document_date")
        private String certificateDocumentDate;
        @JsonProperty("certificate_document_number")
        private String certificateDocumentNumber;
        @JsonProperty("owner_inn")
        private String ownerInn;
        @JsonProperty("producer_inn")
        private String producerInn;
        @JsonProperty("production_date")
        private String productionDate;
        @JsonProperty("tnved_code")
        private String tnvedCode;
        @JsonProperty("uit_code")
        private String uitCode;
        @JsonProperty("uitu_code")
        private String uituCode;
        //getters and setters
    }
}
