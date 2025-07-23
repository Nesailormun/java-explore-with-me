package ru.yandex.practicum.comments.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.yandex.practicum.comments.entity.Comment;
import ru.yandex.practicum.comments.entity.CommentStatus;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findAllByEventIdAndStatus(Long eventId, CommentStatus status);

    List<Comment> findAllByAuthorId(Long authorId);

    List<Comment> findAllByStatus(CommentStatus status);

    Optional<Comment> findByIdAndAuthorId(Long commentId, Long authorId);

    @Query("SELECT c FROM Comment c " +
            "JOIN FETCH c.event " +
            "JOIN FETCH c.author " +
            "WHERE c.id = :commentId")
    Optional<Comment> findByIdWithDetails(@Param("commentId") Long commentId);

    boolean existsByIdAndAuthorId(Long commentId, Long authorId);
}