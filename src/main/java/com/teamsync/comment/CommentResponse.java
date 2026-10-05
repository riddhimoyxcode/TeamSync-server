package com.teamsync.comment;

import java.time.Instant;

public record CommentResponse(String id, String taskId, String authorId, String authorName,
                              String text, Instant createdAt) {

    public CommentResponse(Comment c, String authorName) {
        this(c.getId(), c.getTaskId(), c.getAuthorId(), authorName, c.getText(), c.getCreatedAt());
    }
}