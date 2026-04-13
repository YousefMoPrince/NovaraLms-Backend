package com.amarjo.novaralms.auth.repository;

import com.amarjo.novaralms.auth.model.users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface userRepo extends JpaRepository<users, Long> {

Optional <users> findByEmail(String email);

}
