package com.luxoft.bankapp.service.audit;

import org.springframework.stereotype.Service;

@Service
public class AuditService implements Audit{
    @Override
    public void auditDeposit(long accountId, double amount) {
        System.out.println("ACCOUNT ID: " + accountId + " DEPOSIT: " + amount);
    }

    @Override
    public void auditWithdraw(long accountId, double amount, WithdrawState state) {
        System.out.println("ACCOUNT ID: " + accountId + " WITHDRAWAL: " + amount);
    }

    @Override
    public void auditBalance(long accountId) {
        System.out.println("ACCOUNT ID: " + accountId + " BALANCE CHECK");
    }
}
