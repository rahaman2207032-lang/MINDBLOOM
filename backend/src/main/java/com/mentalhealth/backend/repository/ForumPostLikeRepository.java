package com.mentalhealth.backend.repository;

import com.mentalhealth.backend.model.ForumPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForumPostLikeRepository extends JpaRepository<ForumPostLike, Long> {

    // Check if user already liked a post
    boolean existsByPostIdAndUserId(Long postId, Long userId);

    // Find like by post and user
    Optional<ForumPostLike> findByPostIdAndUserId(Long postId, Long userId);

    // Count likes for a post
    long countByPostId(Long postId);
}

