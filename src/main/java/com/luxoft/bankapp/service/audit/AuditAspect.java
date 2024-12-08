package com.luxoft.bankapp.service.audit;

import com.luxoft.bankapp.model.Account;
import com.luxoft.bankapp.model.Client;
import com.luxoft.bankapp.service.audit.events.BalanceEvent;
import com.luxoft.bankapp.service.audit.events.DepositEvent;
import com.luxoft.bankapp.service.audit.events.WithdrawEvent;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

@Aspect
@Configuration
public class AuditAspect {
    @Autowired
    private ApplicationContext applicationContext;

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

        applicationContext.publishEvent(new DepositEvent(getAccountId(methodArgs), (double) methodArgs[1]));
    }

    @Before("anyBalance()")
    public void logBalance(JoinPoint joinPoint) {
        Object[] methodArgs = joinPoint.getArgs();

        applicationContext.publishEvent(new BalanceEvent(getAccountId(methodArgs)));
    }

    @Around("anyWithdraw()")
    public Object logWithdrawal(ProceedingJoinPoint joinPoint) throws Throwable {
        Object[] methodArgs = joinPoint.getArgs();

        long accountId = getAccountId(methodArgs);

        applicationContext.publishEvent(new WithdrawEvent(accountId, (double) methodArgs[1]));

        Object result;

        try {
            result = joinPoint.proceed();

            applicationContext.publishEvent(
                    new WithdrawEvent(accountId, (double) methodArgs[1], WithdrawEvent.State.SUCCESSFUL)
            );
        } catch (Exception e) {
            applicationContext.publishEvent(
                    new WithdrawEvent(accountId, (double) methodArgs[1], WithdrawEvent.State.FAILED)
            );

            throw e;
        }

        return result;
    }
}
