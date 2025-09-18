package com.marketplace.model.entity;

import com.marketplace.model.enums.ReactionType;
import jakarta.persistence.*;

@Entity
@Table(name = "image_reactions",
        uniqueConstraints = {@UniqueConstraint(columnNames = {"user_id", "image_id"})})
public class ImageReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "image_id", nullable = false)
    private Image image;

    @Enumerated(EnumType.STRING)
    private ReactionType reactionType; // LIKE oder DISLIKE

    public ReactionType getReactionType() {
        return reactionType;
    }

    public void setReactionType(ReactionType reactionType) {
        this.reactionType = reactionType;
    }

    public void setUser(User user) {
        this.user = user;
    }
    public void setImage(Image image) {
        this.image = image;
    }
    public Long getId() {
        return id;
    }
    public User getUser() {
        return user;
    }
    public Image getImage() {
        return image;
    }
}

