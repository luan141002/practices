package com.personal.practices;

import com.personal.practices.entity.Transaction;
import com.personal.practices.repository.TransactionRepository;
import com.personal.practices.service.SnowflakeIdGeneratorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class PracticesApplicationTests {

    private final SnowflakeIdGeneratorService snowflakeIdGeneratorService;

    private final TransactionRepository transactionRepository;

    @Autowired
    PracticesApplicationTests(SnowflakeIdGeneratorService snowflakeIdGeneratorService, TransactionRepository transactionRepository) {
        this.snowflakeIdGeneratorService = snowflakeIdGeneratorService;
        this.transactionRepository = transactionRepository;
    }


    @Test
    public void testSnowflakeIdGenerator() {
        Long id = snowflakeIdGeneratorService.generateId();
        System.out.println("Generated ID: " + id);
    }

    @Test
    public void testSnowflakeId() {
        Transaction transaction = new Transaction();
        transaction.setTestMachineId(5);
        Transaction savedTransaction = transactionRepository.save(transaction);
        snowflakeIdGeneratorService.setMachineId(2);
        snowflakeIdGeneratorService.resolve(savedTransaction.getId());

    }

}
