package com.mentalhealth.backend.repository;

import com.mentalhealth.backend.model.ForumPostLike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ForumPostLikeRepository extends JpaRepository<ForumPostLike, Long> {


    boolean existsByPostIdAndUserId(Long postId, Long userId);


    Optional<ForumPostLike> findByPostIdAndUserId(Long postId, Long userId);


    long countByPostId(Long postId);
}

