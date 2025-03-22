package com.twitter.userservice.service;

import com.twitter.userservice.exception.UserNotFoundException;
import com.twitter.userservice.model.Follow;
import com.twitter.userservice.model.Role;
import com.twitter.userservice.model.User;
import com.twitter.userservice.repository.FollowRepository;
import com.twitter.userservice.repository.RoleRepository;
import com.twitter.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final FollowRepository followRepository;
    
    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
    
    public User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id: " + id));
    }
    
    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("User not found with username: " + username));
    }
    
    @Transactional
    public User createUser(User user) {
        // Check if username already exists
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("Username is already taken");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Email is already in use");
        }
        
        // Set default role
        Set<Role> roles = new HashSet<>();
        Role userRole = roleRepository.findByName(Role.ERole.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Default role not found"));
        roles.add(userRole);
        user.setRoles(roles);
        
        return userRepository.save(user);
    }
    
    @Transactional
    public User updateUser(Long id, User updatedUser) {
        User user = getUserById(id);
        
        if (updatedUser.getUsername() != null && !updatedUser.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(updatedUser.getUsername())) {
                throw new RuntimeException("Username is already taken");
            }
            user.setUsername(updatedUser.getUsername());
        }
        
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(updatedUser.getEmail())) {
                throw new RuntimeException("Email is already in use");
            }
            user.setEmail(updatedUser.getEmail());
        }
        
        if (updatedUser.getBio() != null) {
            user.setBio(updatedUser.getBio());
        }
        
        if (updatedUser.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(updatedUser.getProfilePictureUrl());
        }
        
        return userRepository.save(user);
    }
    
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }
    
    @Transactional
    public void followUser(Long followerId, Long followingId) {
        if (followerId.equals(followingId)) {
            throw new RuntimeException("Users cannot follow themselves");
        }
        
        User follower = getUserById(followerId);
        User following = getUserById(followingId);
        
        if (followRepository.findByFollowerAndFollowing(follower, following).isPresent()) {
            throw new RuntimeException("User already follows this account");
        }
        
        Follow follow = new Follow();
        follow.setFollower(follower);
        follow.setFollowing(following);
        
        followRepository.save(follow);
    }
    
    @Transactional
    public void unfollowUser(Long followerId, Long followingId) {
        User follower = getUserById(followerId);
        User following = getUserById(followingId);
        
        Follow follow = followRepository.findByFollowerAndFollowing(follower, following)
                .orElseThrow(() -> new RuntimeException("Follow relationship does not exist"));
        
        followRepository.delete(follow);
    }
    
    public List<User> getFollowers(Long userId) {
        User user = getUserById(userId);
        List<Follow> followers = followRepository.findByFollowing(user);
        return followers.stream().map(Follow::getFollower).toList();
    }
    
    public List<User> getFollowing(Long userId) {
        User user = getUserById(userId);
        List<Follow> following = followRepository.findByFollower(user);
        return following.stream().map(Follow::getFollowing).toList();
    }
    
    public long getFollowersCount(Long userId) {
        User user = getUserById(userId);
        return followRepository.countByFollowing(user);
    }
    
    public long getFollowingCount(Long userId) {
        User user = getUserById(userId);
        return followRepository.countByFollower(user);
    }
}
