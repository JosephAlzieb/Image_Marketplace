package com.marketplace.repository;

import com.marketplace.model.entity.Image;
import com.marketplace.model.entity.ImageReaction;
import com.marketplace.model.entity.User;
import com.marketplace.model.enums.ReactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ImageReactionRepository extends JpaRepository<ImageReaction, UUID> {
    Optional<ImageReaction> findByUserAndImage(User user, Image image);

    long countByImageAndReactionType(Image image, ReactionType reactionType);
}
