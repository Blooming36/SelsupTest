package org.example;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

public class CrptApi {

    private final Semaphore requestSemaphore;


    public CrptApi(TimeUnit timeUnit, int requestLimit) {
        this.requestSemaphore = new Semaphore(requestLimit);
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(this::releasePermit, 0, timeUnit.toSeconds(1), TimeUnit.SECONDS);
    }

    private void releasePermit() {
        try {
            requestSemaphore.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void createDocument(Document document, String signature) throws  IOException {
        CloseableHttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost httpPost = new HttpPost("apiUrl");
        String requestBody = "{\"document\": " + document + ", \"signature\": \"" + signature + "\"}";
        StringEntity entity = new StringEntity(requestBody, ContentType.APPLICATION_JSON);
        httpPost.setEntity(entity);
        httpClient.execute(httpPost);
    }


    @Getter
    @Setter
    @AllArgsConstructor
    public static class Description {
        @JsonProperty("participantInn")
        private String participantInn;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Document {
        private Description description;
        private String doc_id;
        private String doc_status;
        private String doc_type;
        private boolean importRequest;
        private String owner_inn;
        private String participant_inn;
        private String producer_inn;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate production_date;
        private String production_type;
        private List<Product> products;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate reg_date;
        private String reg_number;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Product {
        private String certificate_document;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate certificate_document_date;
        private String certificate_document_number;
        private String owner_inn;
        private String producer_inn;
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate production_date;
        private String tnved_code;
        private String uit_code;
        private String uitu_code;
    }

    public static void main(String[] args) throws IOException {
        CrptApi crptApi = new CrptApi(TimeUnit.SECONDS, 5);
        Document document = new Document();
        String signature = "sampleSignature";
        crptApi.createDocument(document, signature);
    }
}

