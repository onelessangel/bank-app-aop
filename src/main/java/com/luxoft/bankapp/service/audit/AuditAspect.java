package com.luxoft.bankapp.service.audit;

import com.luxoft.bankapp.model.Account;
import com.luxoft.bankapp.model.Client;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class AuditAspect {
    @Autowired
    private Audit audit;

    @Pointcut("execution(* com.luxoft.bankapp.service.operations.*.deposit(..))")
    public void anyDeposit() {
    }

    @Pointcut("execution(* com.luxoft.bankapp.service.operations.*.withdraw(..))")
    public void anyWithdraw() {
    }

    @Pointcut("execution(* com.luxoft.bankapp.service.operations.*.getBalance(..))")
    public void anyBalance() {
    }

    private long getAccountId(Object[] methodArgs) {
        Account account;

        if (methodArgs[0] instanceof Client) {
            account = ((Client) methodArgs[0]).getActiveAccount();
        } else {
            account = (Account) methodArgs[0];
        }

        return account.getId();
    }

    @Before("anyDeposit()")
    public void logDeposit(JoinPoint joinPoint) {
        Object[] methodArgs = joinPoint.getArgs();

        audit.auditDeposit(getAccountId(methodArgs), (double) methodArgs[1]);
    }

    @Before("anyBalance()")
    public void logBalance(JoinPoint joinPoint) {
        Object[] methodArgs = joinPoint.getArgs();

        audit.auditBalance(getAccountId(methodArgs));
    }

    @Around("anyWithdraw()")
    public Object logWithdrawal(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] methodArgs = joinPoint.getArgs();

        long accountId = getAccountId(methodArgs);

        audit.auditWithdraw(accountId, (double) methodArgs[1], WithdrawState.TRYING);

        Object result;

        try {
            result = joinPoint.proceed();

            audit.auditWithdraw(accountId, (double) methodArgs[1], WithdrawState.SUCCESSFUL);
        } catch (Exception e) {
            audit.auditWithdraw(accountId, (double) methodArgs[1], WithdrawState.FAILED);

            throw e;
        }

        return result;
    }
}
