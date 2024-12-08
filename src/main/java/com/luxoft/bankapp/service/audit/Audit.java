package com.luxoft.bankapp.service.audit;

public interface Audit {
    void auditDeposit(long accountId, double amount);

    void auditWithdraw(long accountId, double amount, WithdrawState state);

    void auditBalance(long accountId);
}
