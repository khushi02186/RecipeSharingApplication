package com.example.RecipeSharing.controller;

import java.util.List;
import java.util.Optional;

import com.example.RecipeSharing.model.Comment;
import com.example.RecipeSharing.model.Users;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.example.RecipeSharing.model.Recipe;
import com.example.RecipeSharing.repository.RecipeRepository;

@RestController
@RequestMapping("/api/recipe")
public class RecipeController {

    private static final Logger logger = LoggerFactory.getLogger(RecipeController.class);

    @Autowired
    RecipeRepository recipeRepository;

    @Autowired
    private com.example.RecipeSharing.repository.CommentRepository commentRepository;

    @Autowired
    private com.example.RecipeSharing.repository.UserRepository userRepository;

    @PostMapping("/add")
    private ResponseEntity<Recipe> addRecipe(@RequestBody Recipe recipe) {
        recipe.setId(null);
        Recipe saveRecipe = recipeRepository.save(recipe);
        return ResponseEntity.ok(saveRecipe);
    }

    @GetMapping("getAllRecipe")
    private List<Recipe> getAllRecipe() {
        logger.info("=== GET ALL RECIPES REQUEST ===");

        try {
            List<Recipe> recipes = recipeRepository.findAll();
            logger.info("Retrieved {} recipes from database", recipes.size());
            return recipes;
        } catch (Exception e) {
            logger.error("Error retrieving all recipes: {}", e.getMessage(), e);
            throw e;
        }
    }

    @GetMapping("/{id}")
    private ResponseEntity<Recipe> getRecipeById(@PathVariable String id) {
        logger.info("=== GET RECIPE BY ID REQUEST ===");
        logger.info("Recipe ID: {}", id);

        try {
            Optional<Recipe> recipe = recipeRepository.findById(id);
            if (recipe.isPresent()) {
                logger.info("Recipe found: {}", recipe.get().getTitle());
                return ResponseEntity.ok(recipe.get());
            } else {
                logger.warn("Recipe not found with ID: {}", id);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            logger.error("Error retrieving recipe by ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PutMapping("/update/{id}")
    private ResponseEntity<Recipe> updateRecipe(@PathVariable String id, @RequestBody Recipe updatedRecipe) {
        logger.info("=== UPDATE RECIPE REQUEST ===");
        logger.info("Recipe ID: {}", id);
        logger.info("Updated recipe title: {}", updatedRecipe.getTitle());

        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            org.springframework.security.core.userdetails.UserDetails userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication
                    .getPrincipal();
            String userId = userDetails.getUsername(); // UserDetailsServiceImpl sets username as userId

            if (updatedRecipe.getUserId().equalsIgnoreCase(userId)) {
                return recipeRepository.findById(id)
                        .map(existing -> {
                            logger.info("Updating existing recipe: {}", existing.getTitle());
                            updatedRecipe.setId(id);
                            Recipe saveRecipe = recipeRepository.save(updatedRecipe);
                            logger.info("Recipe updated successfully");
                            return ResponseEntity.ok(saveRecipe);
                        }).orElseGet(() -> {
                            logger.warn("Recipe not found for update with ID: {}", id);
                            return ResponseEntity.notFound().build();
                        });
            } else {
                logger.warn("You are not the correct user to update this recipe");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            logger.error("Error updating recipe with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @DeleteMapping("/delete/{id}")
    private ResponseEntity<?> deleteRecipe(@PathVariable String id) {
        logger.info("=== DELETE RECIPE REQUEST ===");
        logger.info("Recipe ID: {}", id);

        try {
            org.springframework.security.core.Authentication authentication = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            org.springframework.security.core.userdetails.UserDetails userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication
                    .getPrincipal();
            String userId = userDetails.getUsername();

            Recipe recipe = recipeRepository.findRecipeById(id);
            if (recipe == null) {
                logger.warn("Recipe not found for deletion with ID: {}", id);
                return ResponseEntity.notFound().build();
            }

            if (userId.equalsIgnoreCase(recipe.getUserId())) {
                recipeRepository.deleteById(id);
                logger.info("Recipe deleted successfully with ID: {}", id);
                return ResponseEntity.ok().build();
            } else {
                logger.info("You are not a right user to delete this recipe");
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        } catch (Exception e) {
            logger.error("Error deleting recipe with ID {}: {}", id, e.getMessage(), e);
            throw e;
        }
    }

    @PostMapping("/{id}/comment")
    public ResponseEntity<Comment> addComment(@PathVariable String id, @RequestBody Comment comment) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
           UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String userId = userDetails.getUsername();

            // Fetch username for display
            String username = userRepository.findById(userId).map(Users::getUsername).orElse("Unknown");

            comment.setRecipeId(id);
            comment.setUserId(userId);
            comment.setUsername(username);
            comment.setCreatedAt(System.currentTimeMillis());

            Comment savedComment = commentRepository.save(comment);
            return ResponseEntity.ok(savedComment);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/{id}/comments")
    public ResponseEntity<List<Comment>> getComments(@PathVariable String id) {
        return ResponseEntity.ok(commentRepository.findByRecipeId(id));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Recipe> toggleLike(@PathVariable String id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (org.springframework.security.core.userdetails.UserDetails) authentication.getPrincipal();
            String userId = userDetails.getUsername();

            Recipe recipe = recipeRepository.findById(id).orElseThrow(() -> new RuntimeException("Recipe not found"));

            if (recipe.getLikedUserIds() == null) {
                recipe.setLikedUserIds(new java.util.HashSet<>());
            }

            if (recipe.getLikedUserIds().contains(userId)) {
                recipe.getLikedUserIds().remove(userId);
            } else {
                recipe.getLikedUserIds().add(userId);
            }

            Recipe savedRecipe = recipeRepository.save(recipe);
            return ResponseEntity.ok(savedRecipe);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
