package com.teamsync.comment;

import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

public interface CommentRepository extends MongoRepository<Comment, String> {

    List<Comment> findByTaskIdOrderByCreatedAtAsc(String taskId);

    void deleteByTaskId(String taskId);
}