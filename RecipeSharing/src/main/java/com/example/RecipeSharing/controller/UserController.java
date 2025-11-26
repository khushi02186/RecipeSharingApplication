package com.example.RecipeSharing.controller;

import com.example.RecipeSharing.model.Users;
import com.example.RecipeSharing.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/profile")
    public ResponseEntity<?> getUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String userId = userDetails.getUsername(); // UserDetailsServiceImpl sets username as userId

        Optional<Users> user = userRepository.findById(userId);
        if (user.isPresent()) {
            // Avoid returning password
            Users foundUser = user.get();
            foundUser.setPassword(null);
            return ResponseEntity.ok(foundUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@org.springframework.web.bind.annotation.PathVariable String id) {
        Optional<Users> user = userRepository.findById(id);
        if (user.isPresent()) {
            Users foundUser = user.get();
            // Return only public info
            Users publicUser = new Users();
            publicUser.setId(foundUser.getId());
            publicUser.setUsername(foundUser.getUsername());
            publicUser.setFollowers(foundUser.getFollowers());
            publicUser.setFollowing(foundUser.getFollowing());
            // Don't set email or password
            return ResponseEntity.ok(publicUser);
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logoutUser() {
        // Since we are using stateless JWT, actual logout is handled on client side by
        // removing token.
        // This endpoint can be used for future token blacklisting or logging.
        return ResponseEntity.ok("Logged out successfully");
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<?> followUser(@org.springframework.web.bind.annotation.PathVariable String id) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String currentUserId = userDetails.getUsername();

            if (currentUserId.equals(id)) {
                return ResponseEntity.badRequest().body("Cannot follow yourself");
            }

            Optional<Users> currentUserOpt = userRepository.findById(currentUserId);
            Optional<Users> targetUserOpt = userRepository.findById(id);

            if (currentUserOpt.isPresent() && targetUserOpt.isPresent()) {
                Users currentUser = currentUserOpt.get();
                Users targetUser = targetUserOpt.get();

                if (currentUser.getFollowing() == null)
                    currentUser.setFollowing(new java.util.HashSet<>());
                if (targetUser.getFollowers() == null)
                    targetUser.setFollowers(new java.util.HashSet<>());

                if (currentUser.getFollowing().contains(id)) {
                    // Unfollow
                    currentUser.getFollowing().remove(id);
                    targetUser.getFollowers().remove(currentUserId);
                } else {
                    // Follow
                    currentUser.getFollowing().add(id);
                    targetUser.getFollowers().add(currentUserId);
                }

                userRepository.save(currentUser);
                userRepository.save(targetUser);

                return ResponseEntity.ok("Follow status updated");
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
