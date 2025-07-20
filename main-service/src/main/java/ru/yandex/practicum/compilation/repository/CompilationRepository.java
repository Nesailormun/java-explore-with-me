package ru.yandex.practicum.compilation.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.yandex.practicum.compilation.model.Compilation;

import java.util.List;
import java.util.Optional;

public interface CompilationRepository extends JpaRepository<Compilation, Long> {

    @EntityGraph(attributePaths = "events")
    List<Compilation> findAllByPinned(boolean pinned, Pageable pageable);

    @EntityGraph(attributePaths = "events")
    Optional<Compilation> findById(Long id);
}
