package com.twitter.userservice.repository;

import com.twitter.userservice.model.Follow;
import com.twitter.userservice.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FollowRepository extends JpaRepository<Follow, Long> {
    
    List<Follow> findByFollower(User follower);
    
    List<Follow> findByFollowing(User following);
    
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);
    
    void deleteByFollowerAndFollowing(User follower, User following);
    
    long countByFollower(User follower);
    
    long countByFollowing(User following);
}
