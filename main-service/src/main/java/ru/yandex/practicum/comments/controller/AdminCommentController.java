package ru.yandex.practicum.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.comments.dto.CommentDto;
import ru.yandex.practicum.comments.dto.CommentStatusUpdateDto;
import ru.yandex.practicum.comments.service.CommentService;

import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
public class AdminCommentController {

    private final CommentService commentService;

    @GetMapping
    public List<CommentDto> getCommentsForModeration() {
        log.info("GET /admin/comments");
        return commentService.getCommentsForModeration();
    }

    @PatchMapping("/{commentId}")
    public CommentDto moderateComment(
            @PathVariable Long commentId,
            @RequestBody CommentStatusUpdateDto dto) {

        log.info("PATCH /admin/comments/{}", commentId);
        return commentService.moderateComment(commentId, dto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@PathVariable Long commentId) {
        log.info("DELETE /admin/comments/{}", commentId);
        commentService.deleteCommentByAdmin(commentId);
    }
}