package com.groovith.groovith.dto;

import com.groovith.groovith.domain.MessageType;
import lombok.Data;

@Data
public class MessageRequestDto {
    private String content;
    private MessageType type;
}
