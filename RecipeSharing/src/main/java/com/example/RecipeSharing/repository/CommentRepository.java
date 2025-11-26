package com.example.RecipeSharing.repository;

import com.example.RecipeSharing.model.Comment;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends MongoRepository<Comment, String> {
    List<Comment> findByRecipeId(String recipeId);
}
