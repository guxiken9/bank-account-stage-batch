package com.example.bank.repository;

import com.example.bank.TestcontainersConfiguration;
import com.example.bank.job.ProcessResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({ CustomerStageCalculationRepository.class, TestcontainersConfiguration.class })
class CustomerStageCalculationRepositoryTest {

	@Autowired
	private CustomerStageCalculationRepository repository;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void insertCalculation_shouldInsertAndReturnGeneratedId() {
		// Given
		ProcessResult result = new ProcessResult("CUS001", "NONE", "SILVER", LocalDate.of(2025, 4, 30),
				LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31), new BigDecimal("3500000"), BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				Collections.emptyList(), true);

		// When
		Long calculationId = repository.insertCalculation(result);

		// Then
		assertThat(calculationId).isNotNull().isPositive();

		// データベースに正しく保存されているか検証
		Map<String, Object> savedData = jdbcTemplate
			.queryForMap("SELECT * FROM customer_stage_calculations WHERE id = ?", calculationId);

		assertThat(savedData.get("customer_id")).isEqualTo("CUS001");
		assertThat(savedData.get("current_stage_code")).isEqualTo("NONE");
		assertThat(savedData.get("final_stage_code")).isEqualTo("SILVER");
		assertThat(savedData.get("total_balance")).isEqualTo(new BigDecimal("3500000.00"));
	}

}