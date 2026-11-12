package com.amarjo.novaralms.auth.repository;

import com.amarjo.novaralms.auth.model.users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepo extends JpaRepository<users, Long> {

Optional <users> findByEmail(String email);
Optional <users> findByAccountNumber(Long accountNumber);
Optional <users> findByCode(String code);

}
