package tech.cusbo.msteams.demo.inboundevent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tech.cusbo.msteams.demo.inboundevent.handler.change.ChangeEventStrategyService;
import tech.cusbo.msteams.demo.inboundevent.handler.lifecycle.LifeCycleEventStrategyService;

@Slf4j
@RestController
@RequestMapping("/api/graph/webhook")
@RequiredArgsConstructor
public class InboundEventsController {

  private final LifeCycleEventStrategyService lifeCycleEventStrategyService;
  private final ChangeEventStrategyService changeEventStrategyService;
  private final ObjectMapper objectMapper;

  @PostMapping(
      value = "/events",
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE
  )
  @SneakyThrows
  public ResponseEntity<String> processInboundEvent(
      @RequestBody(required = false) String body,
      @RequestParam(required = false, name = "validationToken") String validationToken
  ) {
    log.info("Received inbound /events call. validationToken={}, body={}", validationToken, body);
    if (validationToken != null) {
      log.info("Responding to subscription validation with token={}", validationToken);
      return ResponseEntity.ok()
          .contentType(MediaType.TEXT_PLAIN)
          .body(validationToken);
    }

    JsonNode events = objectMapper.readTree(body);
    log.info("Parsed event payload: {}", events.toPrettyString());
    for (JsonNode event : events.path("value")) {
        changeEventStrategyService.pickHandlerAndProcessAsync(event);
    }
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_PLAIN)
        .body("ack");
  }

  @PostMapping(
      value = "/lifecycle",
      consumes = MediaType.ALL_VALUE,
      produces = MediaType.TEXT_PLAIN_VALUE
  )
  @SneakyThrows
  public ResponseEntity<String> processLifecycleEvent(
      @RequestBody(required = false) String body,
      @RequestParam(required = false, name = "validationToken") String validationToken
  ) {
    log.info("Received inbound events call. validationToken={}, body={}", validationToken, body);
    if (validationToken != null) {
      log.info("VALIDATION REQUEST, token={}", validationToken);
      return ResponseEntity.ok()
          .contentType(MediaType.TEXT_PLAIN)
          .body(validationToken);
    }
    JsonNode events = objectMapper.readTree(body);
    log.info("LIFECYCLE EVENT, BODY: {}", events.toPrettyString());
    for (JsonNode event : events.path("value")) {
      String lifecycleEvent = event.path("lifecycleEvent").asText();
      // For auth challenge we can't use async in service,
      // hence it hat to be returned right away
      if ("authenticationChallenge".equalsIgnoreCase(lifecycleEvent)) {
        String challenge = event.path("resourceData").path("challenge").asText();
        log.info("AUTH CHALLENGE, responding with {}", challenge);
        return ResponseEntity.ok()
            .contentType(MediaType.TEXT_PLAIN)
            .body(challenge);
      }
      lifeCycleEventStrategyService.handleLifeCycleEventAsync(event);
    }

    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_PLAIN)
        .body("ack");
  }
}

