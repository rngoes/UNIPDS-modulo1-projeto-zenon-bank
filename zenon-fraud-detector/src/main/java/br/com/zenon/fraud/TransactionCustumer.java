package br.com.zenon.fraud;

import java.math.BigDecimal;

public record TransactionCustumer(String name, BigDecimal oldBalance, BigDecimal newBalance) {

}
