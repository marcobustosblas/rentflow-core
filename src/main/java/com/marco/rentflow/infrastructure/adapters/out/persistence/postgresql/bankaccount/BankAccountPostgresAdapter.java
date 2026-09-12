package com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.bankaccount;

import com.marco.rentflow.core.domain.bankaccount.BankAccount;
import com.marco.rentflow.core.domain.bankaccount.ports.out.BankAccountRepository;
import com.marco.rentflow.infrastructure.adapters.out.persistence.postgresql.bankaccount.mapper.BankAccountPersistenceMapper;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Component
public class BankAccountPostgresAdapter implements BankAccountRepository {

    private final BankAccountSpringDataRepository springDataRepository;

    public BankAccountPostgresAdapter(BankAccountSpringDataRepository springDataRepository) {
        this.springDataRepository = springDataRepository;
    }

    @Override
    public BankAccount save(BankAccount bankAccount) {
        BankAccountJpaEntity entity = BankAccountPersistenceMapper.toJpaEntity(bankAccount);
        BankAccountJpaEntity savedEntity = springDataRepository.save(entity);
        return BankAccountPersistenceMapper.toDomain(savedEntity);
    }

    @Override
    public Optional<BankAccount> findById(UUID id) {
        return springDataRepository.findById(id)
                .map(BankAccountPersistenceMapper::toDomain);
    }

    @Override
    public List<BankAccount> findByUserId(UUID userId) {
        return springDataRepository.findByUserId(userId)
                .stream()
                .map(BankAccountPersistenceMapper::toDomain)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        springDataRepository.deleteById(id);
    }
}