package com.groovith.groovith.dto;

import com.groovith.groovith.domain.enums.MessageType;
import lombok.Data;

@Data
public class MessageRequestDto {
    private String content;
    private MessageType type;
}
