package com.luxoft.bankapp;

import com.luxoft.bankapp.exceptions.NotEnoughFundsException;
import com.luxoft.bankapp.model.Account;
import com.luxoft.bankapp.model.CheckingAccount;
import com.luxoft.bankapp.model.Client;
import com.luxoft.bankapp.service.Banking;
import com.luxoft.bankapp.service.audit.AuditService;
import com.luxoft.bankapp.service.operations.BankingOperationsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import static org.junit.jupiter.api.Assertions.assertThrows;


@SpringBootTest(classes = BankApplication.class)
public class BankApplicationTests4 {
    @Autowired
    private Banking banking;

    @Autowired
    private BankingOperationsService bankingOperationsService;

    @Autowired
    private AuditService auditService;

    private Client client;

    @BeforeEach
    public void init() {
        client = banking.getClient("Jonny Bravo");
        client.setDefaultActiveAccountIfNotSet();
        client.getActiveAccount().setId(999);
    }

    @Test
    public void depositToClient1() {
        double amount = 100;

        int countOfEvents = auditService.getEvents().size();

        bankingOperationsService.deposit(client, amount);

        int newCountOfEvents = auditService.getEvents().size();

        assertEquals(countOfEvents + 1, newCountOfEvents);
        assertEquals("DepositEvent",
                auditService.getEvents().get(newCountOfEvents - 1).getClass().getSimpleName());
    }

    @Test
    public void depositToClient2() {
        Account account = client.getActiveAccount();
        double amount = 100;

        int countOfEvents = auditService.getEvents().size();

        bankingOperationsService.deposit(account, amount);

        int newCountOfEvents = auditService.getEvents().size();

        assertEquals(countOfEvents + 1, newCountOfEvents);
        assertEquals("DepositEvent",
                auditService.getEvents().get(newCountOfEvents - 1).getClass().getSimpleName());
    }

    @Test
    public void getClientBalance() {
        int countOfEvents = auditService.getEvents().size();

        bankingOperationsService.getBalance(client);

        int newCountOfEvents = auditService.getEvents().size();

        assertEquals(countOfEvents + 1, newCountOfEvents);
        assertEquals("BalanceEvent",
                auditService.getEvents().get(newCountOfEvents - 1).getClass().getSimpleName());
    }

    @Test
    public void withdrawFromClient1() {
        double amount = 100;

        int countOfEvents = auditService.getEvents().size();

        bankingOperationsService.withdraw(client, amount);

        int newCountOfEvents = auditService.getEvents().size();

        assertEquals(countOfEvents + 2, newCountOfEvents);
        assertEquals("WithdrawEvent",
                auditService.getEvents().get(newCountOfEvents - 2).getClass().getSimpleName());
        assertEquals("WithdrawEvent",
                auditService.getEvents().get(newCountOfEvents - 1).getClass().getSimpleName());
    }

    @Test
    public void withdrawFromClient2() {
        Account account = client.getActiveAccount();
        double amount = 100;

        int countOfEvents = auditService.getEvents().size();

        bankingOperationsService.withdraw(account, amount);

        int newCountOfEvents = auditService.getEvents().size();

        assertEquals(countOfEvents + 2, newCountOfEvents);
        assertEquals("WithdrawEvent",
                auditService.getEvents().get(newCountOfEvents - 2).getClass().getSimpleName());
        assertEquals("WithdrawEvent",
                auditService.getEvents().get(newCountOfEvents - 1).getClass().getSimpleName());
    }

    @Test
    public void withdrawFromClient3() {
        Account account = client.getActiveAccount();
        double balance = account.getBalance();
        double overdraft = 0;

        int countOfEvents = auditService.getEvents().size();

        if (account instanceof CheckingAccount) {
            overdraft = ((CheckingAccount) account).getOverdraft();
        }

        double amount = balance + overdraft + 1000;

        assertThrows(NotEnoughFundsException.class,
                () -> bankingOperationsService.withdraw(account, amount));

        int newCountOfEvents = auditService.getEvents().size();

        assertEquals(countOfEvents + 2, newCountOfEvents);
        assertEquals("WithdrawEvent",
                auditService.getEvents().get(newCountOfEvents - 2).getClass().getSimpleName());
        assertEquals("WithdrawEvent",
                auditService.getEvents().get(newCountOfEvents - 1).getClass().getSimpleName());
    }

}
