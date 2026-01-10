package com.mentalhealth.backend.repository;

import com.mentalhealth.backend.model.ForumPost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForumPostRepository extends JpaRepository<ForumPost, Long> {


    List<ForumPost> findAllByOrderByCreatedAtDesc();

    // Find all posts ordered by likes (most liked first)
    List<ForumPost> findAllByOrderByLikesDesc();

    // Search posts by title or content
    @Query("SELECT p FROM ForumPost p WHERE " +
           "LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(p.content) LIKE LOWER(CONCAT('%', :query, '%'))")
    List<ForumPost> searchPosts(@Param("query") String query);

    // Find posts by user
    List<ForumPost> findByAuthorIdOrderByCreatedAtDesc(Long userId);
}

