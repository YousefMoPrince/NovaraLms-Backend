package com.amarjo.novaralms.payment.Repo;

import com.amarjo.novaralms.payment.Model.MainServerDB;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface MainServerRepo extends JpaRepository<MainServerDB, UUID> {
    Optional<MainServerDB> findFirstBy();
}
