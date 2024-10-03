package com.groovith.groovith.controller;

import com.groovith.groovith.dto.SearchChatRoomsResponseDto;
import com.groovith.groovith.dto.SearchUsersResponseDto;
import com.groovith.groovith.service.SearchService;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.data.domain.Pageable;

@RestController
@AllArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/users")
    public ResponseEntity<SearchUsersResponseDto> searchUsers(
            @RequestParam String query,
            Pageable pageable,
            @RequestParam(required = false) Long lastUserId   // 첫번째 페이지일 경우 null 값
    ) {
        return new ResponseEntity<>(searchService.searchUsers(query, pageable, lastUserId), HttpStatus.OK);
    }

    @GetMapping("/chatrooms")
    public ResponseEntity<SearchChatRoomsResponseDto> searchChatRooms(
            @RequestParam String query,
            Pageable pageable,
            @RequestParam(required = false)Long lastChatRoomId // 첫번째 페이지일 경우 null 값
    ) {
        return new ResponseEntity<>(searchService.searchChatRooms(query, pageable, lastChatRoomId), HttpStatus.OK);
    }
}
