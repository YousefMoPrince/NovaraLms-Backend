package com.amarjo.novaralms.payment.Repo;

import com.amarjo.novaralms.payment.Model.ServerTransactions;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ServerTransactionsRepo extends JpaRepository<ServerTransactions, UUID> {
}
