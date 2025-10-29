package tech.cusbo.msteams.demo.inboundevent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.graph.models.ChangeNotification;
import com.microsoft.graph.models.ChangeNotificationCollection;
import com.microsoft.graph.models.LifecycleEventType;
import com.microsoft.kiota.serialization.KiotaJsonSerialization;
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
      @RequestBody(required = false) String jsonPayload,
      @RequestParam(required = false, name = "validationToken") String validationToken
  ) {
    log.info(
        "Received change notification with:  \n validationToken={}, \n body={}",
        validationToken,
        jsonPayload
    );
    if (validationToken != null) {
      log.info("Responding to subscription validation with token={}", validationToken);
      return ResponseEntity.ok()
          .contentType(MediaType.TEXT_PLAIN)
          .body(validationToken);
    }

    ChangeNotificationCollection notifications = KiotaJsonSerialization.deserialize(
        jsonPayload,
        ChangeNotificationCollection::createFromDiscriminatorValue
    );
    log.info("Parsed event payload: {}", objectMapper.writeValueAsString(notifications));

    for (ChangeNotification event : notifications.getValue()) {
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
      @RequestBody(required = false) String jsonPayload,
      @RequestParam(required = false, name = "validationToken") String validationToken
  ) {
    log.info(
        "Received lifecycle notification with:  \n validationToken={}, \n body={}",
        validationToken,
        jsonPayload
    );
    if (validationToken != null) {
      log.info("VALIDATION REQUEST, token={}", validationToken);
      return ResponseEntity.ok()
          .contentType(MediaType.TEXT_PLAIN)
          .body(validationToken);
    }

    ChangeNotificationCollection notifications = KiotaJsonSerialization.deserialize(
        jsonPayload,
        ChangeNotificationCollection::createFromDiscriminatorValue
    );
    log.info("Parsed event payload: {}", objectMapper.writeValueAsString(notifications));
    for (ChangeNotification event : notifications.getValue()) {
      LifecycleEventType lifecycleEvent = event.getLifecycleEvent();
      // For auth challenge we can't use async in service,
      // hence it has to be returned right away
      if ("authenticationChallenge".equalsIgnoreCase(lifecycleEvent.getValue())) {
        String challenge = (String) event.getResourceData().getAdditionalData().get("challenge");
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

