package com.marco.rentflow.core.domain.bankaccount.ports.out;

import com.marco.rentflow.core.domain.bankaccount.BankAccount;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BankAccountRepository {

    // Guarda una nueva cuenta bancaria
    BankAccount save(BankAccount bankAccount);

    // Busca una cuenta bancaria por su ID para validar que exista
    Optional<BankAccount> findById(UUID id);

    List<BankAccount> findByUserId(UUID userId);

    public void deleteById(UUID id);
}
