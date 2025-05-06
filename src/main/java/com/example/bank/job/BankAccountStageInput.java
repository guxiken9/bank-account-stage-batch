package com.example.bank.job;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Record representing the input data for the Bank Account Stage monthly batch process.
 */
public record BankAccountStageInput(String customerId, String currentStageCode, LocalDate monthEndDate,
		BigDecimal totalBalance, BigDecimal foreignCurrencyBalance, BigDecimal investmentTrustBalance,
		BigDecimal monthlyForeignCurrencyPurchase, BigDecimal monthlyInvestmentTrustPurchase,
		BigDecimal housingLoanBalance, BigDecimal monthlyFxTradingVolume) {
}