package com.example.bank.job;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record ProcessResult(String customerId, String currentStageCode, String finalStageCode,
		LocalDate calculationDate, LocalDate validFrom, LocalDate validTo, BigDecimal totalBalance,
		BigDecimal foreignCurrencyBalance, BigDecimal investmentTrustBalance, BigDecimal monthlyForeignCurrencyPurchase,
		BigDecimal monthlyInvestmentTrustPurchase, BigDecimal housingLoanBalance, BigDecimal monthlyFxTradingVolume,
		List<ConditionEvaluation> conditionEvaluations, boolean hasStageTransition) {

	public record ConditionEvaluation(String conditionType, boolean isMet, BigDecimal evaluatedValue) {
	}
}
