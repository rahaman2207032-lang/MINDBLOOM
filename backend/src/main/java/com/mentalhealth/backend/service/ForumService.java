package com.mentalhealth.backend.service;

import com.mentalhealth.backend.dto.*;
import com.mentalhealth.backend.model.*;
import com.mentalhealth.backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ForumService {

    @Autowired
    private ForumPostRepository forumPostRepository;

    @Autowired
    private ForumCommentRepository forumCommentRepository;

    @Autowired
    private ForumPostLikeRepository forumPostLikeRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Get all posts with sorting and user context
     */
    public List<ForumPostResponse> getPosts(String sortType, Long currentUserId) {
        List<ForumPost> posts;

        switch (sortType.toLowerCase()) {
            case "most_liked":
                posts = forumPostRepository.findAllByOrderByLikesDesc();
                break;
            case "most_commented":
                // Get all posts and sort by comment count
                posts = forumPostRepository.findAllByOrderByCreatedAtDesc();
                posts = posts.stream()
                    .sorted((p1, p2) -> Long.compare(
                        forumCommentRepository.countByPostId(p2.getId()),
                        forumCommentRepository.countByPostId(p1.getId())
                    ))
                    .collect(Collectors.toList());
                break;
            case "latest":
            default:
                posts = forumPostRepository.findAllByOrderByCreatedAtDesc();
                break;
        }

        // Convert to response DTOs with like status and comment count
        return posts.stream()
            .map(post -> convertToResponse(post, currentUserId))
            .collect(Collectors.toList());
    }

    /**
     * Get all forum posts (sorted by latest)
     */
    public List<ForumPostResponse> getAllPosts(Long currentUserId) {
        List<ForumPost> posts = forumPostRepository.findAllByOrderByCreatedAtDesc();
        return posts.stream()
                .map(post -> convertToResponse(post, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * Get posts sorted by popularity (likes)
     */
    public List<ForumPostResponse> getPopularPosts(Long currentUserId) {
        List<ForumPost> posts = forumPostRepository.findAllByOrderByLikesDesc();
        return posts.stream()
                .map(post -> convertToResponse(post, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * Search posts by keyword
     */
    public List<ForumPostResponse> searchPosts(String query, Long currentUserId) {
        List<ForumPost> posts = forumPostRepository.searchPosts(query);
        return posts.stream()
                .map(post -> convertToResponse(post, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * Get a single post by ID
     */
    public ForumPostResponse getPostById(Long postId, Long currentUserId) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));
        return convertToResponse(post, currentUserId);
    }

    /**
     * Get posts created by a specific user
     */
    public List<ForumPostResponse> getUserPosts(Long userId, Long currentUserId) {
        List<ForumPost> posts = forumPostRepository.findByAuthorIdOrderByCreatedAtDesc(userId);
        return posts.stream()
                .map(post -> convertToResponse(post, currentUserId))
                .collect(Collectors.toList());
    }

    /**
     * Create a new forum post
     */
    @Transactional
    public ForumPostResponse createPost(Long userId, ForumPostRequest request) {
        try {
            System.out.println("=== SERVICE CREATE POST ===");
            System.out.println("📝 [ForumService] Creating post for user ID: " + userId);
            System.out.println("   Looking for user in users table...");

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        System.err.println("❌ [ForumService] USER NOT FOUND with ID: " + userId);
                        return new RuntimeException("User not found with id: " + userId);
                    });

            System.out.println("✅ [ForumService] Found user: " + user.getUsername() + " (ID: " + userId + ")");

            ForumPost post = new ForumPost(
                    request.getTitle(),
                    request.getContent(),
                    userId,
                    user.getUsername(),
                    request.isAnonymous()
            );

            System.out.println("💾 [ForumService] Saving post to database...");
            ForumPost savedPost = forumPostRepository.save(post);
            System.out.println("✅ [ForumService] Post saved successfully with ID: " + savedPost.getId());
            System.out.println("===========================");

            return convertToResponse(savedPost, userId);
        } catch (Exception e) {
            System.err.println("❌ [ForumService] Error creating post: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Failed to create post: " + e.getMessage(), e);
        }
    }

    /**
     * Update a forum post
     */
    @Transactional
    public ForumPostResponse updatePost(Long postId, Long userId, ForumPostRequest request) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Check if user is the author
        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("You can only edit your own posts");
        }

        post.setTitle(request.getTitle());
        post.setContent(request.getContent());
        post.setAnonymous(request.isAnonymous());

        ForumPost updatedPost = forumPostRepository.save(post);
        return convertToResponse(updatedPost, userId);
    }

    /**
     * Delete a forum post
     */
    @Transactional
    public void deletePost(Long postId, Long userId) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        // Check if user is the author
        if (!post.getAuthorId().equals(userId)) {
            throw new RuntimeException("You can only delete your own posts");
        }

        forumPostRepository.delete(post);
    }

    /**
     * Check if a post is liked by a user
     */
    public boolean isPostLikedByUser(Long postId, Long userId) {
        return forumPostLikeRepository.existsByPostIdAndUserId(postId, userId);
    }

    /**
     * Like a post
     */
    @Transactional
    public void likePost(Long postId, Long userId) {
        try {
            System.out.println("=== LIKE POST DEBUG ===");
            System.out.println("📝 Post ID: " + postId);
            System.out.println("👤 User ID: " + userId);

            ForumPost post = forumPostRepository.findById(postId)
                    .orElseThrow(() -> {
                        System.err.println("❌ Post not found with ID: " + postId);
                        return new RuntimeException("Post not found with id: " + postId);
                    });
            System.out.println("✅ Found post: " + post.getTitle());

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        System.err.println("❌ User not found with ID: " + userId);
                        return new RuntimeException("User not found with id: " + userId);
                    });
            System.out.println("✅ Found user: " + user.getUsername());

            // Check if already liked
            boolean alreadyLiked = forumPostLikeRepository.existsByPostIdAndUserId(postId, userId);
            System.out.println("📊 Already liked: " + alreadyLiked);

            if (alreadyLiked) {
                System.err.println("⚠️ User already liked this post");
                throw new RuntimeException("You have already liked this post");
            }

            // Create like
            ForumPostLike like = new ForumPostLike(post, user);
            forumPostLikeRepository.save(like);
            System.out.println("✅ Like saved to database");

            // Increment likes count
            post.setLikes(post.getLikes() + 1);
            forumPostRepository.save(post);
            System.out.println("✅ Post likes count updated to: " + post.getLikes());
            System.out.println("======================");
        } catch (Exception e) {
            System.err.println("❌ ERROR in likePost: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Unlike a post
     */
    @Transactional
    public void unlikePost(Long postId, Long userId) {
        ForumPost post = forumPostRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found with id: " + postId));

        ForumPostLike like = forumPostLikeRepository.findByPostIdAndUserId(postId, userId)
                .orElseThrow(() -> new RuntimeException("You haven't liked this post"));

        forumPostLikeRepository.delete(like);

        // Decrement likes count
        post.setLikes(Math.max(0, post.getLikes() - 1));
        forumPostRepository.save(post);
    }

    /**
     * Get comments for a post
     */
    public List<ForumCommentResponse> getPostComments(Long postId) {
        List<ForumComment> comments = forumCommentRepository.findByPostIdOrderByCreatedAtAsc(postId);
        return comments.stream()
                .map(this::convertCommentToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Get comment count for a post
     */
    public long getCommentCount(Long postId) {
        long count = forumCommentRepository.countByPostId(postId);
        System.out.println("📊 [ForumService] getCommentCount for post " + postId + ": " + count);
        return count;
    }

    /**
     * Get post ID by comment ID (needed before deleting comment)
     */
    public Long getPostIdByCommentId(Long commentId) {
        ForumComment comment = forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));
        return comment.getPost().getId();
    }

    /**
     * Add a comment to a post
     */
    @Transactional
    public ForumCommentResponse addComment(Long postId, Long userId, ForumCommentRequest request) {
        try {
            System.out.println("=== ADD COMMENT DEBUG ===");
            System.out.println("📝 Post ID: " + postId);
            System.out.println("👤 User ID: " + userId);
            System.out.println("💬 Comment: " + request.getContent());
            System.out.println("🎭 Anonymous: " + request.isAnonymous());

            ForumPost post = forumPostRepository.findById(postId)
                    .orElseThrow(() -> {
                        System.err.println("❌ Post not found with ID: " + postId);
                        return new RuntimeException("Post not found with id: " + postId);
                    });
            System.out.println("✅ Found post: " + post.getTitle());

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> {
                        System.err.println("❌ User not found with ID: " + userId);
                        return new RuntimeException("User not found with id: " + userId);
                    });
            System.out.println("✅ Found user: " + user.getUsername());

            ForumComment comment = new ForumComment(
                    post,
                    user,
                    request.getContent(),
                    request.isAnonymous()
            );

            ForumComment savedComment = forumCommentRepository.save(comment);
            System.out.println("✅ Comment saved with ID: " + savedComment.getId());

            // Verify the comment count immediately after saving
            long countAfterSave = forumCommentRepository.countByPostId(postId);
            System.out.println("📊 Comment count for post " + postId + " after save: " + countAfterSave);

            System.out.println("========================");

            return convertCommentToResponse(savedComment);
        } catch (Exception e) {
            System.err.println("❌ ERROR in addComment: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }

    /**
     * Delete a comment
     */
    @Transactional
    public void deleteComment(Long commentId, Long userId) {
        ForumComment comment = forumCommentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found with id: " + commentId));

        // Check if user is the author
        if (!comment.getAuthor().getId().equals(userId)) {
            throw new RuntimeException("You can only delete your own comments");
        }

        Long postId = getPostIdByCommentId(commentId); // Get the post ID before deleting the comment

        forumCommentRepository.delete(comment);

        // Optionally, you can return the updated comment count after deletion
        long updatedCount = forumCommentRepository.countByPostId(postId);
        System.out.println("📊 Updated comment count for post " + postId + " after deletion: " + updatedCount);
    }

    /**
     * Helper: Convert ForumPost to ForumPostResponse
     */
    private ForumPostResponse convertToResponse(ForumPost post, Long currentUserId) {
        System.out.println("🔍 [DEBUG] Converting post ID: " + post.getId() + " to response");

        long commentsCountLong = forumCommentRepository.countByPostId(post.getId());
        int commentsCount = (int) commentsCountLong;

        System.out.println("   📊 Comments count from DB: " + commentsCount + " for post ID: " + post.getId());

        boolean likedByCurrentUser = currentUserId != null &&
                forumPostLikeRepository.existsByPostIdAndUserId(post.getId(), currentUserId);

        String authorName = post.isAnonymous() ? "Anonymous" : post.getAuthorName();

        ForumPostResponse response = new ForumPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getAuthorId(),
                authorName,
                post.isAnonymous(),
                post.getLikes(),
                commentsCount,
                likedByCurrentUser,
                post.getCreatedAt(),
                post.getUpdatedAt()
        );

        System.out.println("   ✅ Response created with " + commentsCount + " comments");
        return response;
    }

    /**
     * Helper: Convert ForumComment to ForumCommentResponse
     */
    private ForumCommentResponse convertCommentToResponse(ForumComment comment) {
        String authorName = comment.isAnonymous() ? "Anonymous" : comment.getAuthor().getUsername();

        return new ForumCommentResponse(
                comment.getId(),
                comment.getPost().getId(),
                comment.getAuthor().getId(),
                authorName,
                comment.getContent(),
                comment.isAnonymous(),
                comment.getCreatedAt()
        );
    }
}

