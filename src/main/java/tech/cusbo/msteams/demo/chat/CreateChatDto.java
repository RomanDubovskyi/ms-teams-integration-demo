package tech.cusbo.msteams.demo.chat;

import com.microsoft.graph.models.ChatType;

public record CreateChatDto(
    String topic,
    ChatType type,
    String[] memberIds
) {

}
