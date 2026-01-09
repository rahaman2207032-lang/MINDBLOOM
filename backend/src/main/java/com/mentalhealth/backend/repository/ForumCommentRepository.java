package com.mentalhealth.backend.repository;

import com.mentalhealth.backend.model.ForumComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {

    // Find all comments for a post ordered by creation date (Native SQL)
    @Query(value = "SELECT * FROM forum_comments WHERE post_id = :postId ORDER BY created_at ASC", nativeQuery = true)
    List<ForumComment> findByPostIdOrderByCreatedAtAsc(@Param("postId") Long postId);

    // Count comments for a post (Native SQL)
    @Query(value = "SELECT COUNT(*) FROM forum_comments WHERE post_id = :postId", nativeQuery = true)
    long countByPostId(@Param("postId") Long postId);

    // Find comments by user (Native SQL)
    @Query(value = "SELECT * FROM forum_comments WHERE user_id = :userId ORDER BY created_at DESC", nativeQuery = true)
    List<ForumComment> findByAuthorIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}

