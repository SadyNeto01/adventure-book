package com.adventurebook.progress;

import com.adventurebook.progress.entity.PlayerProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlayerProgressRepository extends JpaRepository<PlayerProgress, Long> {

    Optional<PlayerProgress> findByPlayerIdAndBookId(Long playerId, Long bookId);

    List<PlayerProgress> findByPlayerIdOrderByUpdatedAtDesc(Long playerId);
}
