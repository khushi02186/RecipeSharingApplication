package com.example.RecipeSharing.repository;

import com.example.RecipeSharing.model.Users;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface UserRepository extends MongoRepository<Users, String> {
    Optional<Users> findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByUsername(String username);

}
