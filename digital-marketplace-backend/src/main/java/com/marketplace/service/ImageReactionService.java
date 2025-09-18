package com.marketplace.service;

import com.marketplace.model.entity.Image;
import com.marketplace.model.entity.ImageReaction;
import com.marketplace.model.entity.User;
import com.marketplace.model.enums.ReactionType;
import com.marketplace.repository.ImageReactionRepository;
import com.marketplace.repository.ImageRepository;
import com.marketplace.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ImageReactionService {

    private final ImageReactionRepository reactionRepo;
    private final UserRepository userRepo;
    private final ImageRepository imageRepo;

    public ImageReactionService(ImageReactionRepository reactionRepo, UserRepository userRepo, ImageRepository imageRepo) {
        this.reactionRepo = reactionRepo;
        this.userRepo = userRepo;
        this.imageRepo = imageRepo;
    }

    @Transactional
    public void reactToImage(UUID userId, UUID imageId, ReactionType reactionType) {
        User user = userRepo.findById(userId).orElseThrow();
        Image image = imageRepo.findById(imageId).orElseThrow();

        Optional<ImageReaction> existing = reactionRepo.findByUserAndImage(user, image);

        if (existing.isPresent()) {
            ImageReaction reaction = existing.get();
            // Wenn gleiche Reaktion → entfernen (toggle)
            if (reaction.getReactionType() == reactionType) {
                reactionRepo.delete(reaction);
            } else {
                // Sonst ReactionType ändern
                reaction.setReactionType(reactionType);
                reactionRepo.save(reaction);
            }
        } else {
            // Neue Reaktion anlegen
            ImageReaction reaction = new ImageReaction();
            reaction.setUser(user);
            reaction.setImage(image);
            reaction.setReactionType(reactionType);
            reactionRepo.save(reaction);
        }
    }

    public long countLikes(UUID imageId) {
        Image image = imageRepo.findById(imageId).orElseThrow();
        return reactionRepo.countByImageAndReactionType(image, ReactionType.LIKE);
    }

    public long countDislikes(UUID imageId) {
        Image image = imageRepo.findById(imageId).orElseThrow();
        return reactionRepo.countByImageAndReactionType(image, ReactionType.DISLIKE);
    }
}
