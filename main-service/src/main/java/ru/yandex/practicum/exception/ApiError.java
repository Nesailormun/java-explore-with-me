package ru.yandex.practicum.exception;


import java.time.LocalDateTime;

public record ApiError(String errors, String message, String reason, String status, LocalDateTime timestamp) {

}
