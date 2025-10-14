package tech.cusbo.msteams.demo.inboundevent.subscription;

import java.util.List;

public record GraphSubscriptionResourceDto(
    String resource,
    List<String> changeTypes
) {

}
