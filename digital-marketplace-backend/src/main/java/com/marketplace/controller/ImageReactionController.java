package com.marketplace.controller;

import com.marketplace.annotation.CurrentUser;
import com.marketplace.model.enums.ReactionType;
import com.marketplace.security.UserPrincipal;
import com.marketplace.service.ImageReactionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/images/reactions")
public class ImageReactionController {

    private final ImageReactionService reactionService;

    public ImageReactionController(ImageReactionService reactionService) {
        this.reactionService = reactionService;
    }

    @PostMapping("/{imageId}/like")
    public ResponseEntity<Void> like(@PathVariable UUID imageId, @CurrentUser UserPrincipal currentUser) {
        reactionService.reactToImage(currentUser.getId(), imageId, ReactionType.LIKE);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{imageId}/dislike")
    public ResponseEntity<Void> dislike(@PathVariable UUID imageId, @CurrentUser UserPrincipal currentUser) {
        reactionService.reactToImage(currentUser.getId(), imageId, ReactionType.DISLIKE);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/{imageId}/likes")
    public long countLikes(@PathVariable UUID imageId) {
        return reactionService.countLikes(imageId);
    }

    @GetMapping("/{imageId}/dislikes")
    public long countDislikes(@PathVariable UUID imageId) {
        return reactionService.countDislikes(imageId);
    }
}
