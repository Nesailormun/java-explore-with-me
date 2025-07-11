package ru.yandex.practicum.user.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.user.dto.NewUserRequest;
import ru.yandex.practicum.user.dto.UserDto;
import ru.yandex.practicum.user.service.UserService;

import java.util.List;

@RestController("/admin/users")
@Slf4j
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public List<UserDto> getUsers(@RequestParam(required = false) List<Long> ids,
                                  @RequestParam(defaultValue = "0") int from,
                                  @RequestParam(defaultValue = "10") int size) {
        log.info("GET /admin/users with params: ids={}; from={}; size={}", ids, from, size );
        return userService.getUsers(ids, from, size);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto registerUser(@RequestBody @Valid NewUserRequest userRequest) {
        log.info("POST /admin/users: {}", userRequest);
        return userService.registerUser(userRequest);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long userId) {
        log.info("DELETE /admin/users/{}", userId);
        userService.deleteUser(userId);
    }

}
