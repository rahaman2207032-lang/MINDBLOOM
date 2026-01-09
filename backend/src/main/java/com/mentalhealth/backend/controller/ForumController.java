package com.mentalhealth.backend.controller;

import com.mentalhealth.backend.dto.*;
import com.mentalhealth.backend.service.ForumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/forum")
public class ForumController {

    @Autowired
    private ForumService forumService;

    /**
     * Get all posts with optional sorting
     * GET /api/forum/posts?sort=latest&userId=1
     */
    @GetMapping("/posts")
    public ResponseEntity<List<ForumPostResponse>> getPosts(
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(required = false) Long userId) {
        try {
            System.out.println("📥 [ForumController] Fetching posts. Sort: " + sort + ", UserId: " + userId);
            List<ForumPostResponse> posts = forumService.getPosts(sort, userId);
            System.out.println("✅ [ForumController] Returning " + posts.size() + " posts");
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            System.err.println("❌ [ForumController] Error fetching posts: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get popular posts (sorted by likes)
     * GET /api/forum/posts/popular?userId=1
     */
    @GetMapping("/posts/popular")
    public ResponseEntity<List<ForumPostResponse>> getPopularPosts(
            @RequestParam(required = false) Long userId) {
        try {
            List<ForumPostResponse> posts = forumService.getPopularPosts(userId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            System.err.println("❌ Error fetching popular posts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Search posts by keyword
     * GET /api/forum/posts/search?query=anxiety&userId=1
     */
    @GetMapping("/posts/search")
    public ResponseEntity<List<ForumPostResponse>> searchPosts(
            @RequestParam String query,
            @RequestParam(required = false) Long userId) {
        try {
            List<ForumPostResponse> posts = forumService.searchPosts(query, userId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            System.err.println("❌ Error searching posts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get a single post by ID
     * GET /api/forum/posts/1?userId=1
     */
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ForumPostResponse> getPostById(
            @PathVariable Long postId,
            @RequestParam(required = false) Long userId) {
        try {
            ForumPostResponse post = forumService.getPostById(postId, userId);
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            System.err.println("❌ Error fetching post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            System.err.println("❌ Error fetching post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Get posts by a specific user
     * GET /api/forum/users/1/posts?currentUserId=1
     */
    @GetMapping("/users/{userId}/posts")
    public ResponseEntity<List<ForumPostResponse>> getUserPosts(
            @PathVariable Long userId,
            @RequestParam(required = false) Long currentUserId) {
        try {
            List<ForumPostResponse> posts = forumService.getUserPosts(userId, currentUserId);
            return ResponseEntity.ok(posts);
        } catch (Exception e) {
            System.err.println("❌ Error fetching user posts: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Create a new forum post
     * POST /api/forum/posts
     * Body: { "title": "...", "content": "...", "userId": 1, "anonymous": false }
     */
    @PostMapping("/posts")
    public ResponseEntity<ForumPostResponse> createPost(@RequestBody ForumPostRequest request) {
        try {
            System.out.println("=== CREATE POST DEBUG ===");
            System.out.println("📥 [ForumController] Received create post request");
            System.out.println("   Title: " + request.getTitle());
            System.out.println("   Content length: " + (request.getContent() != null ? request.getContent().length() : 0));
            System.out.println("   UserId: " + request.getUserId());
            System.out.println("   Anonymous: " + request.isAnonymous());
            System.out.println("========================");

            // Validate request
            if (request.getTitle() == null || request.getTitle().trim().isEmpty()) {
                System.err.println("❌ [ForumController] Title is empty");
                return ResponseEntity.badRequest().build();
            }

            if (request.getContent() == null || request.getContent().trim().isEmpty()) {
                System.err.println("❌ [ForumController] Content is empty");
                return ResponseEntity.badRequest().build();
            }

            if (request.getUserId() == null) {
                System.err.println("❌ [ForumController] UserId is null");
                return ResponseEntity.badRequest().build();
            }

            ForumPostResponse post = forumService.createPost(request.getUserId(), request);
            System.out.println("✅ [ForumController] Forum post created successfully with ID: " + post.getId());
            return ResponseEntity.status(HttpStatus.CREATED).body(post);
        } catch (RuntimeException e) {
            System.err.println("❌ [ForumController] Runtime error creating post: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        } catch (Exception e) {
            System.err.println("❌ [ForumController] Error creating post: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Update a forum post
     * PUT /api/forum/posts/1?userId=1
     * Body: { "title": "...", "content": "...", "anonymous": false }
     */
    @PutMapping("/posts/{postId}")
    public ResponseEntity<ForumPostResponse> updatePost(
            @PathVariable Long postId,
            @RequestParam Long userId,
            @RequestBody ForumPostRequest request) {
        try {
            System.out.println("✏️ Updating post " + postId + " by user: " + userId);
            ForumPostResponse post = forumService.updatePost(postId, userId, request);
            System.out.println("✅ Post updated successfully");
            return ResponseEntity.ok(post);
        } catch (RuntimeException e) {
            System.err.println("❌ Error updating post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.err.println("❌ Error updating post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Delete a forum post
     * DELETE /api/forum/posts/1?userId=1
     */
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long postId,
            @RequestParam Long userId) {
        try {
            System.out.println("🗑️ Deleting post " + postId + " by user: " + userId);
            forumService.deletePost(postId, userId);
            System.out.println("✅ Post deleted successfully");
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            System.err.println("❌ Error deleting post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        } catch (Exception e) {
            System.err.println("❌ Error deleting post: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Like/Unlike toggle for a post (toggle behavior)
     * POST /api/forum/posts/{postId}/like?userId=1 or with header X-User-Id
     */
    @PostMapping("/posts/{postId}/like")
    public ResponseEntity<?> likePost(
            @PathVariable Long postId,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        try {
            Long effectiveUserId = (userId != null) ? userId : headerUserId;
            if (effectiveUserId == null) {
                System.err.println("❌ [ForumController] Missing userId for likePost");
                Map<String, String> error = new HashMap<>();
                error.put("error", "userId is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            System.out.println("👍 User " + effectiveUserId + " toggling like on post " + postId);

            // Check if already liked - if so, unlike; if not, like (TOGGLE behavior)
            boolean alreadyLiked = forumService.isPostLikedByUser(postId, effectiveUserId);

            Map<String, Object> response = new HashMap<>();
            if (alreadyLiked) {
                // Unlike the post
                System.out.println("👎 Post already liked, unliking...");
                forumService.unlikePost(postId, effectiveUserId);
                response.put("success", true);
                response.put("liked", false);
                response.put("message", "Post unliked successfully");
            } else {
                // Like the post
                System.out.println("❤️ Post not liked yet, liking...");
                forumService.likePost(postId, effectiveUserId);
                response.put("success", true);
                response.put("liked", true);
                response.put("message", "Post liked successfully");
            }

            System.out.println("✅ Like toggle successful");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("❌ Error toggling like: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.err.println("❌ Error toggling like: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Unlike a post
     * DELETE /api/forum/posts/{postId}/like?userId=1 or with header X-User-Id
     */
    @DeleteMapping("/posts/{postId}/like")
    public ResponseEntity<?> unlikePost(
            @PathVariable Long postId,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        try {
            Long effectiveUserId = (userId != null) ? userId : headerUserId;
            if (effectiveUserId == null) {
                System.err.println("❌ [ForumController] Missing userId for unlikePost");
                Map<String, String> error = new HashMap<>();
                error.put("error", "userId is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            System.out.println("👎 User " + effectiveUserId + " unliking post " + postId);
            forumService.unlikePost(postId, effectiveUserId);
            System.out.println("✅ Post unliked successfully");

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Post unliked successfully");
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("❌ Error unliking post: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.err.println("❌ Error unliking post: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Get comments for a post
     * GET /api/forum/posts/1/comments
     */
    @GetMapping("/posts/{postId}/comments")
    public ResponseEntity<List<ForumCommentResponse>> getPostComments(@PathVariable Long postId) {
        try {
            List<ForumCommentResponse> comments = forumService.getPostComments(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            System.err.println("❌ Error fetching comments: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Add a comment to a post
     * POST /api/forum/posts/{postId}/comments?userId=1 or with header X-User-Id
     * Body: { "content": "...", "anonymous": false }
     */
    @PostMapping("/posts/{postId}/comments")
    public ResponseEntity<?> addComment(
            @PathVariable Long postId,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId,
            @RequestBody ForumCommentRequest request) {
        try {
            Long effectiveUserId = (userId != null) ? userId : headerUserId;
            if (effectiveUserId == null) {
                System.err.println("❌ [ForumController] Missing userId for addComment");
                Map<String, String> error = new HashMap<>();
                error.put("error", "userId is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }
            System.out.println("💬 User " + effectiveUserId + " commenting on post " + postId);
            ForumCommentResponse comment = forumService.addComment(postId, effectiveUserId, request);

            // Get updated comment count for the post
            long updatedCount = forumService.getCommentCount(postId);

            System.out.println("✅ Comment added successfully. New count: " + updatedCount);

            // Return both the comment and updated count
            Map<String, Object> response = new HashMap<>();
            response.put("comment", comment);
            response.put("commentCount", updatedCount);
            response.put("success", true);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            System.err.println("❌ Error adding comment: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        } catch (Exception e) {
            System.err.println("❌ Error adding comment: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }

    /**
     * Delete a comment
     * DELETE /api/forum/comments/{commentId}?userId=1 or with header X-User-Id
     */
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable Long commentId,
            @RequestParam(required = false) Long userId,
            @RequestHeader(value = "X-User-Id", required = false) Long headerUserId) {
        try {
            Long effectiveUserId = (userId != null) ? userId : headerUserId;
            if (effectiveUserId == null) {
                System.err.println("❌ [ForumController] Missing userId for deleteComment");
                Map<String, String> error = new HashMap<>();
                error.put("error", "userId is required");
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
            }

            System.out.println("🗑️ User " + effectiveUserId + " deleting comment " + commentId);

            // Get postId before deleting the comment
            Long postId = forumService.getPostIdByCommentId(commentId);

            // Delete the comment
            forumService.deleteComment(commentId, effectiveUserId);

            // Get updated comment count
            long updatedCount = forumService.getCommentCount(postId);

            System.out.println("✅ Comment deleted successfully. New count: " + updatedCount);

            // Return success with updated count
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Comment deleted successfully");
            response.put("commentCount", updatedCount);
            response.put("postId", postId);

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            System.err.println("❌ Error deleting comment: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
        } catch (Exception e) {
            System.err.println("❌ Error deleting comment: " + e.getMessage());
            Map<String, String> error = new HashMap<>();
            error.put("error", "Internal server error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
}

