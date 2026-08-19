package com.marco.rentflow.core.domain.common;

import com.marco.rentflow.core.domain.common.exceptions.CurrencyMismatchException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Money Value Object Tests")
class MoneyTest {

    @Test
    @DisplayName("Should create Money correctly")
    void shouldCreateMoney() {
        Money money = new Money(new BigDecimal("1000"), Currency.CLP);
        assertEquals(new BigDecimal("1000"), money.getAmount());
        assertEquals(Currency.CLP, money.getCurrency());
    }

    @Test
    @DisplayName("Should throw exception on negative amount")
    void shouldThrowExceptionOnNegativeAmount() {
        assertThrows(IllegalArgumentException.class, () ->
                new Money(new BigDecimal("-1"), Currency.CLP)
        );
    }

    @Test
    @DisplayName("Should throw CurrencyMismatchException when operating different currencies")
    void shouldThrowCurrencyMismatchException() {
        Money clp = new Money(new BigDecimal("1000"), Currency.CLP);
        Money usd = new Money(new BigDecimal("100"), Currency.USD);

        CurrencyMismatchException exception = assertThrows(CurrencyMismatchException.class, () ->
                clp.add(usd)
        );

        assertNotNull(exception.getMessage());
    }
}