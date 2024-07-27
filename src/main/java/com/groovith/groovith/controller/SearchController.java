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

@RestController
@AllArgsConstructor
@RequestMapping("/api/search")
public class SearchController {
    private final SearchService searchService;

    @GetMapping("/users")
    public ResponseEntity<SearchUsersResponseDto> searchUsers(@RequestParam String query) {
        return new ResponseEntity<>(searchService.searchUsersByName(query), HttpStatus.OK);
    }

    @GetMapping("/chatrooms")
    public ResponseEntity<SearchChatRoomsResponseDto> searchChatRooms(@RequestParam String query) {
        return new ResponseEntity<>(searchService.searchChatRoomsByName(query), HttpStatus.OK);
    }
}
