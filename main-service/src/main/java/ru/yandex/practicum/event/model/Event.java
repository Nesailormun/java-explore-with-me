package ru.yandex.practicum.event.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.category.model.Category;
import ru.yandex.practicum.user.model.User;

import java.time.LocalDateTime;

@Entity
@Table(name = "events")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 120)
    private String title;

    @Column(length = 2000)
    private String annotation;

    @Column(length = 7000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id")
    private User initiator;

    private LocalDateTime eventDate;
    private LocalDateTime createdOn;
    private LocalDateTime publishedOn;

    @Embedded
    private Location location;

    private Boolean paid;
    private Integer participantLimit;
    private Boolean requestModeration;

    @Enumerated(EnumType.STRING)
    private EventState state;

    public enum EventState {
        PENDING, PUBLISHED, CANCELED
    }
}
