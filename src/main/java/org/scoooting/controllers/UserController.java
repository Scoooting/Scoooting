package org.scoooting.controllers;

import lombok.RequiredArgsConstructor;
import org.scoooting.dto.UserDto;
import org.scoooting.services.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/app/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/{page}")
    public ResponseEntity<List<UserDto>> getUsers(@PathVariable int page) {
        List<UserDto> userDtos = userService.getPagingUsers(page);
        return ResponseEntity.ok()
                .header("X-Total-Count", String.valueOf(userDtos.size()))
                .body(userDtos);
    }
}
