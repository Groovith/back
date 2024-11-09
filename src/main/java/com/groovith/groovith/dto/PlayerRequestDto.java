package com.groovith.groovith.dto;

import com.groovith.groovith.domain.enums.PlayerActionRequestType;
import lombok.Data;

@Data
public class PlayerRequestDto {
    private String videoId;
    private Integer index;
    private Boolean repeat;
    private Long position;
    private PlayerActionRequestType action;
}
