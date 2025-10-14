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

@Slf4j
@RestController
@RequestMapping("/api/graph/webhook")
@RequiredArgsConstructor
public class InboundEventsController {

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
    log.info("📩 Received inbound /events call. validationToken={}, body={}", validationToken, body);
    if (validationToken != null) {
      log.info("✅ Responding to subscription validation with token={}", validationToken);
      return ResponseEntity.ok()
          .contentType(MediaType.TEXT_PLAIN)
          .body(validationToken);
    }

    JsonNode event = objectMapper.readTree(body);
    log.info("⚙️ Parsed event payload: {}", event.toPrettyString());
    // TODO: process event
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
    log.info("📩 Received inbound /events call. validationToken={}, body={}", validationToken, body);
    if (validationToken != null) {
      log.info("✅ Responding to subscription validation with token={}", validationToken);
      return ResponseEntity.ok()
          .contentType(MediaType.TEXT_PLAIN)
          .body(validationToken);
    }
    JsonNode event = objectMapper.readTree(body);
    log.info("⚙️ Parsed event payload: {}", event.toPrettyString());
    // TODO: process event
    return ResponseEntity.ok()
        .contentType(MediaType.TEXT_PLAIN)
        .body("ack");
  }
}

