package com.amarjo.novaralms.payment.Repo;

import com.amarjo.novaralms.payment.Model.MainUserDB;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MainUserRepo extends JpaRepository<MainUserDB, UUID> {
    Optional<MainUserDB> findFirstBy();
}
