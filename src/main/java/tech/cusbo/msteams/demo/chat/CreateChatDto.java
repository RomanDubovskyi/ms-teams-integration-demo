package tech.cusbo.msteams.demo.chat;


public record CreateChatDto(
    String topic,
    String type,
    String[] memberIds
) {

}
