package ru.yandex.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.comments.dto.*;
import ru.yandex.practicum.comments.mapper.CommentMapper;
import ru.yandex.practicum.comments.entity.Comment;
import ru.yandex.practicum.comments.entity.CommentStatus;
import ru.yandex.practicum.comments.repository.CommentRepository;
import ru.yandex.practicum.event.model.Event;
import ru.yandex.practicum.event.repository.EventRepository;
import ru.yandex.practicum.exception.*;
import ru.yandex.practicum.user.model.User;
import ru.yandex.practicum.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private static final int EDIT_WINDOW_HOURS = 1;

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;
    private final CommentMapper commentMapper;

    @Override
    @Transactional
    public CommentDto createComment(Long userId, Long eventId, NewCommentDto dto) {
        log.info("Creating new comment by user={} for event={}", userId, eventId);

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id=" + eventId + " not found"));

        if (event.getState() != Event.EventState.PUBLISHED) {
            log.error("Event with id={} is not published", eventId);
            throw new ConflictException("Cannot comment on unpublished event");
        }

        Comment comment = commentMapper.toEntity(dto, event, author);
        Comment savedComment = commentRepository.save(comment);
        log.info("Created comment with id={}", savedComment.getId());

        return commentMapper.toDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateComment(Long userId, Long commentId, UpdateCommentDto dto) {
        log.info("Updating comment id={} by user={}", commentId, userId);

        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " by user=" + userId
                        + " not found"));

        validateCommentEditable(comment);

        commentMapper.updateFromDto(dto, comment);
        Comment updatedComment = commentRepository.save(comment);
        log.info("Updated comment id={}", commentId);

        return commentMapper.toDto(updatedComment);
    }

    @Override
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        log.info("Deleting comment id={} by user={}", commentId, userId);

        if (!commentRepository.existsByIdAndAuthorId(commentId, userId)) {
            throw new NotFoundException("Comment with id=" + commentId + " not found");
        }

        commentRepository.deleteById(commentId);
        log.info("Deleted comment id={}", commentId);
    }

    @Override
    public List<CommentDto> getEventComments(Long eventId) {
        log.info("Getting comments for event id={}", eventId);

        List<Comment> comments = commentRepository.findAllByEventIdAndStatus(
                eventId, CommentStatus.PUBLISHED);

        return comments.stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getUserComments(Long userId) {
        log.info("Getting comments by user id={}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id=" + userId + " not found"));

        return commentRepository.findAllByAuthorId(userId).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto moderateComment(Long commentId, CommentStatusUpdateDto dto) {
        log.info("Moderating comment id={} with status={}", commentId, dto.getStatus());

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id=" + commentId + " not found"));

        try {
            CommentStatus newStatus = CommentStatus.valueOf(dto.getStatus());

            if (newStatus == CommentStatus.PUBLISHED) {
                comment.setPublishedOn(LocalDateTime.now());
            }

            comment.setStatus(newStatus);
            Comment updatedComment = commentRepository.save(comment);
            log.info("Comment id={} moderated to status={}", commentId, newStatus);

            return commentMapper.toDto(updatedComment);
        } catch (IllegalArgumentException e) {
            log.error("Invalid comment status: {}", dto.getStatus());
            throw new BadRequestException("Invalid comment status: " + dto.getStatus());
        }
    }

    @Override
    public List<CommentDto> getCommentsForModeration() {
        log.info("Getting comments for moderation");

        return commentRepository.findAllByStatus(CommentStatus.PENDING_MODERATION).stream()
                .map(commentMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteCommentByAdmin(Long commentId) {
        log.info("Admin deleting comment id={}", commentId);

        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment with id=" + commentId + " not found");
        }

        commentRepository.deleteById(commentId);
        log.info("Admin deleted comment id={}", commentId);
    }

    private void validateCommentEditable(Comment comment) {
        if (comment.getStatus() != CommentStatus.PENDING_MODERATION) {
            log.error("Comment id={} cannot be edited because it's not pending moderation", comment.getId());
            throw new ConflictException("Only pending moderation comments can be edited");
        }

        LocalDateTime now = LocalDateTime.now();
        if (comment.getCreatedOn().plusHours(EDIT_WINDOW_HOURS).isBefore(now)) {
            log.error("Comment id={} cannot be edited after 1 hour window", comment.getId());
            throw new ConflictException("Comment can only be edited within 1 hour of creation");
        }
    }

}
