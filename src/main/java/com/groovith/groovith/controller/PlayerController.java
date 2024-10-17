package com.groovith.groovith.controller;

import com.groovith.groovith.dto.PlayerRequestDto;
import com.groovith.groovith.dto.PlayerDetailsDto;
import com.groovith.groovith.dto.VideoDto;
import com.groovith.groovith.dto.VideoDto;
import com.groovith.groovith.security.CustomUserDetails;
import com.groovith.groovith.service.PlayerService;
import com.groovith.groovith.service.YoutubeService;
import com.groovith.groovith.service.YoutubeService;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Objects;

@RestController
@RequestMapping("/api/chatrooms/{chatRoomId}/player")
@AllArgsConstructor
public class PlayerController {
    private final PlayerService playerService;
    private final YoutubeService youtubeService;

    @PatchMapping("/join")
    public ResponseEntity<PlayerDetailsDto> joinPlayer(@PathVariable Long chatRoomId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(playerService.joinPlayer(chatRoomId, userDetails.getUserId()));
    }

    @PatchMapping("/leave")
    public ResponseEntity<Void> leavePlayer(@PathVariable Long chatRoomId, @AuthenticationPrincipal CustomUserDetails userDetails) {
        playerService.leavePlayer(chatRoomId, userDetails.getUserId());

        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<PlayerDetailsDto> getPlayerDetails(@PathVariable Long chatRoomId) {
        return ResponseEntity.ok(playerService.getPlayerDetails(chatRoomId));
    }

    @MessageMapping({"/api/chatrooms/{chatRoomId}/player/listen-together"})
    public void sendPlayerMessage(
            @Payload PlayerRequestDto playerRequestDto,
            @DestinationVariable Long chatRoomId,
            SimpMessageHeaderAccessor headerAccessor) throws IOException {
        Long userId = (Long) Objects.requireNonNull(headerAccessor.getSessionAttributes()).get("userId");
        if (userId == null) {
            throw new RuntimeException("User ID is missing in the session.");
        }
        VideoDto videoDto = youtubeService.getVideo(playerRequestDto.getVideoId());
        playerService.handleMessage(chatRoomId, playerRequestDto, userId, videoDto);
    }
}
