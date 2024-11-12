package com.groovith.groovith.dto;

import lombok.Data;

import java.util.List;

@Data
public class MessageListResponseDto {
    private final List<MessageResponseDto> messages;
}
