package com.kamui.luna.modules.finance.repository;

import com.kamui.luna.modules.finance.entity.FinancialTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FinancialTransactionRepository extends JpaRepository<FinancialTransaction, Long> {
    List<FinancialTransaction> findAllByOrderByCreatedAtDesc();
}
