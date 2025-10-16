package tech.cusbo.msteams.demo.inboundevent.handler;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tech.cusbo.msteams.demo.inboundevent.handler.change.ChangeEventHandler;
import tech.cusbo.msteams.demo.inboundevent.handler.lifecycle.LifeCycleEventsHandler;

@Configuration
@RequiredArgsConstructor
public class HandlersMapInitializer {

  @Bean
  @Qualifier("lifecycleEventHandlers")
  public Map<String, LifeCycleEventsHandler> lifecycleEventHandlers(
      List<LifeCycleEventsHandler> handlerList
  ) {
    return handlerList.stream().collect(
            Collectors.toMap(LifeCycleEventsHandler::getEventType, Function.identity())
        );
  }

  @Bean
  @Qualifier("changeEventHandlerMap")
  public Map<String, ChangeEventHandler> changeEventHandlers(
      List<ChangeEventHandler> handlerList
  ) {
    return handlerList.stream().collect(
            Collectors.toMap(ChangeEventHandler::getEventType, Function.identity())
        );
  }
}
