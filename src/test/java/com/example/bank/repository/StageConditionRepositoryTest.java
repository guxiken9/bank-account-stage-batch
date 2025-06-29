package com.example.bank.repository;

import com.example.bank.TestcontainersConfiguration;
import com.example.bank.domain.StageCondition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({ StageConditionRepository.class, TestcontainersConfiguration.class })
class StageConditionRepositoryTest {

	@Autowired
	private StageConditionRepository repository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void findValidConditions_shouldReturnAllValidConditions() {
		// Given
		LocalDate calculationDate = LocalDate.of(2025, 4, 30);

		// When
		List<StageCondition> conditions = repository.findValidConditions(calculationDate);

		// Then
		assertThat(conditions).hasSize(5);

		// シルバー条件の検証
		assertThat(conditions).filteredOn(condition -> "SILVER".equals(condition.stageCode()))
			.hasSize(3)
			.extracting(StageCondition::conditionType)
			.containsExactlyInAnyOrder("TOTAL_BALANCE", "MONTHLY_FOREIGN_CURRENCY_PURCHASE",
					"MONTHLY_INVESTMENT_TRUST_PURCHASE");

		// ゴールド条件の検証
		StageCondition goldCondition = conditions.stream()
			.filter(c -> "GOLD".equals(c.stageCode()))
			.findFirst()
			.orElseThrow();

		assertThat(goldCondition.conditionType()).isEqualTo("COMBINED_BALANCE_GOLD");
		assertThat(goldCondition.minValue()).isEqualByComparingTo(new BigDecimal("5000000"));
		assertThat(goldCondition.maxValue()).isEqualByComparingTo(new BigDecimal("10000000"));

		// プラチナ条件の検証
		StageCondition platinumCondition = conditions.stream()
			.filter(c -> "PLATINUM".equals(c.stageCode()))
			.findFirst()
			.orElseThrow();

		assertThat(platinumCondition.conditionType()).isEqualTo("COMBINED_BALANCE_PLATINUM");
		assertThat(platinumCondition.minValue()).isEqualByComparingTo(new BigDecimal("10000000"));
	}

}