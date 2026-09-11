package com.marco.rentflow.infrastructure.config;

import org.springframework.aop.Advisor;
import org.springframework.aop.aspectj.AspectJExpressionPointcut;
import org.springframework.aop.support.DefaultPointcutAdvisor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.interceptor.MatchAlwaysTransactionAttributeSource;
import org.springframework.transaction.interceptor.RollbackRuleAttribute;
import org.springframework.transaction.interceptor.RuleBasedTransactionAttribute;
import org.springframework.transaction.interceptor.TransactionInterceptor;

import java.util.Collections;

@Configuration
public class UseCaseTransactionConfig {

    @Bean
    public TransactionInterceptor useCaseTransactionInterceptor(PlatformTransactionManager transactionManager) {
        // establezco las reglas de la transacción
        RuleBasedTransactionAttribute transactionAttribute = new RuleBasedTransactionAttribute();
        transactionAttribute.setRollbackRules(
                Collections.singletonList(new RollbackRuleAttribute(Exception.class))
        );

        MatchAlwaysTransactionAttributeSource attributeSource = new MatchAlwaysTransactionAttributeSource();
        attributeSource.setTransactionAttribute(transactionAttribute);

        return new TransactionInterceptor(transactionManager, attributeSource);
    }

    @Bean
    public Advisor useCaseTransactionAdvisor(TransactionInterceptor useCaseTransactionInterceptor) {
        AspectJExpressionPointcut pointcut = new AspectJExpressionPointcut();
        // Intercepto all el paquete de casos de uso sin tocar el core
        pointcut.setExpression("execution(public * com.marco.rentflow.core.application.usecase..*.*(..))");
        return new DefaultPointcutAdvisor(pointcut, useCaseTransactionInterceptor);
    }

}

/**
 * (11-09-26, 12:00 hr)
 * TransactionInterceptor es el "interceptor" que hace el trabajo
 * Spring inyecta PlatformTransactionManager automáticamente
 * (es el que sabe hablar con PostgreSQL para hacer BEGIN, COMMIT, ROLLBACK)
 *
 */
