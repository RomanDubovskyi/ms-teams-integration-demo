package tech.cusbo.msteams.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.bot.schema.Activity;
import com.microsoft.bot.schema.ActivityTypes;
import com.microsoft.bot.schema.ChannelAccount;
import com.microsoft.bot.schema.ConversationAccount;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class InboundEventsController {
  private final ObjectMapper objectMapper = new ObjectMapper();
  private String bearerToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIsIng1dCI6IkhTMjNiN0RvN1RjYVUxUm9MSHdwSXEyNFZZZyIsImtpZCI6IkhTMjNiN0RvN1RjYVUxUm9MSHdwSXEyNFZZZyJ9.eyJhdWQiOiJodHRwczovL2FwaS5ib3RmcmFtZXdvcmsuY29tIiwiaXNzIjoiaHR0cHM6Ly9zdHMud2luZG93cy5uZXQvZDZkNDk0MjAtZjM5Yi00ZGY3LWExZGMtZDU5YTkzNTg3MWRiLyIsImlhdCI6MTc1OTg0MTIwNCwibmJmIjoxNzU5ODQxMjA0LCJleHAiOjE3NTk5Mjc5MDQsImFpbyI6ImsySmdZTmlZT3p2U3RQKzl2TVg1a25NUFdqbkxBQT09IiwiYXBwaWQiOiIzZThiNGRiNC0xOTE3LTRhNmUtOGYzNS02MTI3ZTllNDBjZjYiLCJhcHBpZGFjciI6IjEiLCJpZHAiOiJodHRwczovL3N0cy53aW5kb3dzLm5ldC9kNmQ0OTQyMC1mMzliLTRkZjctYTFkYy1kNTlhOTM1ODcxZGIvIiwiaWR0eXAiOiJhcHAiLCJyaCI6IjEuQVc0QUlKVFUxcHZ6OTAyaDNOV2FrMWh4MjBJekxZMHB6MWxKbFhjT0RxLTlGcnhlQVFCdUFBLiIsInRpZCI6ImQ2ZDQ5NDIwLWYzOWItNGRmNy1hMWRjLWQ1OWE5MzU4NzFkYiIsInV0aSI6Ikt0eDJzNEw1c0V1S2l2Yzc1cHdRQUEiLCJ2ZXIiOiIxLjAiLCJ4bXNfZnRkIjoibk55S0NqcklfZXN1RzZLVkdfTW92NUw4bW9YQWdpU3ViQUhGRDVZT0VvTUJkWE51YjNKMGFDMWtjMjF6IiwieG1zX2lkcmVsIjoiMiAxMyIsInhtc19yZCI6IjAuNDJMbllCSml6bHZIS0NUQ3dTb2tjRWxtdDlXaUVnUHY1Zm1Mb2g4THRhOEZpcklMQ2R6elRycVhmVERUYjhtMkFfbjloYnNTZ2FLY1FnSU1hQUFveWlFazRHU3MyNnQ1UHRKemFqa2YzX3E5WW5zQSJ9.cjr7kug9wR8Zjn7KFg4wklds4bxvM4HV20Ya7oibI7gCPaNEusPAV6q6k2VuwOQ9lcqjBYwr4msqLuwIt6jeyS8TaVFFkDeoz1doM9JotJ9pe-24YF8rZ2NxacZB2UXD8Hl-4abglbZv8SeilFbKpEBFu47TZh-7f1BlvaDksz5CS7fID57W9-ZZZxRk5m4PF8tkODkhmv_JpMi7mNpFBUZz8vZ5g3x-ABNQsjloTGOYfIeDjn7M7dfFHTRvW4_r45LZuyE1m-lkefnQxk8ygRHa8C15xA2rEC-JzsrqSydWPeqo1YZlQ8v_RFzcvsiJ96YzOExTALR2c_baQm3aZA";

  @PostMapping
  @SneakyThrows
  public ResponseEntity<Void> processEvent(@RequestBody Activity activity) {
    System.out.println("Received event " + objectMapper
        .writerWithDefaultPrettyPrinter()
        .writeValueAsString(activity));


    // --- 1️⃣ Prepare outgoing Activity ---
    Activity reply = Activity.createMessageActivity();
    reply.setType("message");
    reply.setText("✅ Automatic reply from Spring Boot Bot");
    reply.setLocale("en-US");

    // Must match the bot’s identity (recipient of inbound message)
    ChannelAccount from = new ChannelAccount();
    from.setId(activity.getRecipient().getId());
    from.setName(activity.getRecipient().getName());
    reply.setFrom(from);

    // Optional but recommended: specify the recipient (original sender)
    ChannelAccount recipient = new ChannelAccount();
    recipient.setId(activity.getFrom().getId());
    recipient.setName(activity.getFrom().getName());
    reply.setRecipient(recipient);

    // Set conversation context
    ConversationAccount conversation = new ConversationAccount();
    conversation.setId(activity.getConversation().getId());
    reply.setConversation(conversation);

    // --- 2️⃣ Build the URL ---
    String serviceUrl = activity.getServiceUrl();
    if (serviceUrl.endsWith("/")) {
      serviceUrl = serviceUrl.substring(0, serviceUrl.length() - 1);
    }
    String conversationId = activity.getConversation().getId();
    String postUrl = serviceUrl + "/v3/conversations/" + conversationId + "/activities";

    // --- 3️⃣ Prepare request ---
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.setBearerAuth(bearerToken); // 🔐 Your token provider

    HttpEntity<Activity> entity = new HttpEntity<>(reply, headers);

    // --- 4️⃣ Send with RestTemplate ---
    RestTemplate restTemplate = new RestTemplate();
    ResponseEntity<String> response = restTemplate.exchange(
        postUrl,
        HttpMethod.POST,
        entity,
        String.class
    );

    System.out.println("Reply status: " + response.getStatusCodeValue());
    System.out.println("Reply body: " + response.getBody());

    return ResponseEntity.ok().build();
  }
}

