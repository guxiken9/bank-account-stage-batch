package com.example.bank.service;

import com.example.bank.domain.RankChangeCondition;
import com.example.bank.domain.Stage;
import com.example.bank.domain.StageCondition;
import com.example.bank.job.BankAccountStageInput;
import com.example.bank.job.ProcessResult;
import com.example.bank.repository.RankChangeConditionRepository;
import com.example.bank.repository.StageConditionRepository;
import com.example.bank.repository.StageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class StageEvaluationService {

	private final StageConditionRepository stageConditionRepository;

	private final RankChangeConditionRepository rankChangeConditionRepository;

	private final StageRepository stageRepository;

	@Autowired
	public StageEvaluationService(StageConditionRepository stageConditionRepository,
			RankChangeConditionRepository rankChangeConditionRepository, StageRepository stageRepository) {
		this.stageConditionRepository = stageConditionRepository;
		this.rankChangeConditionRepository = rankChangeConditionRepository;
		this.stageRepository = stageRepository;
	}

	public ProcessResult evaluateStage(BankAccountStageInput input) {
		LocalDate calculationDate = input.monthEndDate();

		List<ProcessResult.ConditionEvaluation> conditionEvaluations = new ArrayList<>();

		// 基本ステージ判定
		String basicStage = evaluateBasicStage(input, calculationDate, conditionEvaluations);

		// ランクアップ条件適用
		String finalStage = applyRankUpConditions(basicStage, input, calculationDate, conditionEvaluations);

		LocalDate validFrom = calculationDate.plusDays(1);
		LocalDate validTo = validFrom.plusMonths(1).minusDays(1);

		boolean hasStageTransition = !input.currentStageCode().equals(finalStage);

		return new ProcessResult(input.customerId(), input.currentStageCode(), finalStage, calculationDate, validFrom,
				validTo, input.totalBalance(), input.foreignCurrencyBalance(), input.investmentTrustBalance(),
				input.monthlyForeignCurrencyPurchase(), input.monthlyInvestmentTrustPurchase(),
				input.housingLoanBalance(), input.monthlyFxTradingVolume(), conditionEvaluations, hasStageTransition);
	}

	private String evaluateBasicStage(BankAccountStageInput input, LocalDate calculationDate,
			List<ProcessResult.ConditionEvaluation> conditionEvaluations) {

		List<StageCondition> conditions = stageConditionRepository.findValidConditions(calculationDate);
		Map<String, Integer> stageOrders = getStageOrderMap(calculationDate);

		String highestStage = "NONE";
		int highestOrder = 0;

		for (StageCondition condition : conditions) {
			BigDecimal evaluatedValue = getEvaluatedValue(condition.conditionType(), input);
			boolean isMet = isConditionMet(condition, evaluatedValue);

			conditionEvaluations
				.add(new ProcessResult.ConditionEvaluation(condition.conditionType(), isMet, evaluatedValue));

			if (isMet) {
				int stageOrder = stageOrders.getOrDefault(condition.stageCode(), 0);
				if (stageOrder > highestOrder) {
					highestOrder = stageOrder;
					highestStage = condition.stageCode();
				}
			}
		}

		return highestStage;
	}

	private String applyRankUpConditions(String basicStage, BankAccountStageInput input, LocalDate calculationDate,
			List<ProcessResult.ConditionEvaluation> conditionEvaluations) {

		List<RankChangeCondition> conditions = rankChangeConditionRepository.findValidConditions(calculationDate);
		Map<String, Integer> stageOrders = getStageOrderMap(calculationDate);

		int totalRankUp = 0;

		for (RankChangeCondition condition : conditions) {
			BigDecimal evaluatedValue = getEvaluatedValue(condition.conditionType(), input);
			boolean isMet = evaluatedValue.compareTo(condition.thresholdValue()) >= 0;

			conditionEvaluations
				.add(new ProcessResult.ConditionEvaluation(condition.conditionType(), isMet, evaluatedValue));

			if (isMet) {
				totalRankUp += condition.rankChangeLevels();
			}
		}

		return applyRankUp(basicStage, totalRankUp, stageOrders);
	}

	private BigDecimal getEvaluatedValue(String conditionType, BankAccountStageInput input) {
		return switch (conditionType) {
			case "TOTAL_BALANCE" -> input.totalBalance();
			case "MONTHLY_FOREIGN_CURRENCY_PURCHASE" -> input.monthlyForeignCurrencyPurchase();
			case "MONTHLY_INVESTMENT_TRUST_PURCHASE" -> input.monthlyInvestmentTrustPurchase();
			case "COMBINED_BALANCE_GOLD", "COMBINED_BALANCE_PLATINUM" ->
				input.foreignCurrencyBalance().add(input.investmentTrustBalance());
			case "HOUSING_LOAN" -> input.housingLoanBalance();
			case "FX_TRADING" -> input.monthlyFxTradingVolume();
			default -> BigDecimal.ZERO;
		};
	}

	private boolean isConditionMet(StageCondition condition, BigDecimal evaluatedValue) {
		boolean meetsMin = evaluatedValue.compareTo(condition.minValue()) >= 0;
		boolean meetsMax = condition.maxValue() == null || evaluatedValue.compareTo(condition.maxValue()) < 0;
		return meetsMin && meetsMax;
	}

	private String applyRankUp(String currentStage, int rankUpLevels, Map<String, Integer> stageOrders) {
		if (rankUpLevels == 0) {
			return currentStage;
		}

		int currentOrder = stageOrders.getOrDefault(currentStage, 0);
		int newOrder = currentOrder + (rankUpLevels * 100);

		return stageOrders.entrySet()
			.stream()
			.filter(entry -> entry.getValue() <= newOrder)
			.max(Map.Entry.comparingByValue())
			.map(Map.Entry::getKey)
			.orElse(currentStage);
	}

	private Map<String, Integer> getStageOrderMap(LocalDate calculationDate) {
		return stageRepository.findValidStages(calculationDate)
			.stream()
			.collect(Collectors.toMap(Stage::stageCode, Stage::stageOrder));
	}

}