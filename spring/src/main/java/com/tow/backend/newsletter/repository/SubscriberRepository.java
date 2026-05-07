package com.tow.backend.newsletter.repository;

import com.tow.backend.newsletter.entity.Subscriber;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubscriberRepository extends JpaRepository<Subscriber, Long> {
    Optional<Subscriber> findByEmail(String email);
    Optional<Subscriber> findByConfirmationToken(String confirmationToken);
}

