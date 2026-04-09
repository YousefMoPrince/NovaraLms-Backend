package com.amarjo.novaralms.auth.repository;

import com.amarjo.novaralms.auth.model.users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface userRepo extends JpaRepository<users, Integer> {
boolean existsByEmail(String email);
Optional <users> findByEmail(String email);
Optional<users> findById(Long strudentId);
}
