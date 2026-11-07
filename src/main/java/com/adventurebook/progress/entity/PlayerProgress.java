package com.adventurebook.progress.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PlayerProgress {

    public static final int STARTING_HEALTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long playerId;

    private Long bookId;

    private int currentSectionId;

    private int health;

    @Enumerated(EnumType.STRING)
    private ProgressStatus status;

    private Instant updatedAt;

    public PlayerProgress(Long playerId, Long bookId, int beginSectionId) {
        this.playerId = playerId;
        this.bookId = bookId;
        restart(beginSectionId);
    }

    public void restart(int beginSectionId) {
        this.currentSectionId = beginSectionId;
        this.health = STARTING_HEALTH;
        this.status = ProgressStatus.IN_PROGRESS;
    }

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = Instant.now();
    }
}
