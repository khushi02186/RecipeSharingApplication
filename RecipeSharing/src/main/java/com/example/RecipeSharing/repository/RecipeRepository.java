package com.example.RecipeSharing.repository;

import com.example.RecipeSharing.model.Recipe;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RecipeRepository extends MongoRepository<Recipe, String> {
    Recipe findRecipeById(String id);

}
