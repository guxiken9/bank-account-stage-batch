package com.example.bank;

import org.junit.jupiter.api.Test;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.jdbc.Sql;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@SpringBatchTest
@TestPropertySource(
		properties = { "spring.batch.job.name=BankAccountStage", "input.file.path=classpath:test-data.csv" })
@Sql(scripts = { "/db/migration/V001__initial_schema.sql", "/db/migration/V002__initial_data.sql" })
class BankAccountStageBatchIntegrationTest {

	@Autowired
	private JobLauncherTestUtils jobLauncherTestUtils;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void testBankAccountStageJob() throws Exception {
		// When
		JobExecution jobExecution = jobLauncherTestUtils.launchJob();

		// Then
		assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

		// 計算結果テーブルの検証
		Integer calculationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM customer_stage_calculations",
				Integer.class);
		assertThat(calculationCount).isEqualTo(11); // テストデータの顧客数

		// 条件評価結果テーブルの検証
		Integer evaluationCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM condition_evaluation_results",
				Integer.class);
		assertThat(evaluationCount).isEqualTo(77); // 11顧客 × 7条件

		// ステージ遷移テーブルの検証（遷移があった顧客のみ）
		Integer transitionCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM stage_transitions", Integer.class);
		assertThat(transitionCount).isEqualTo(10); // CUS001以外の10顧客

		// 特定顧客の結果検証
		verifyCustomerResults();
	}

	private void verifyCustomerResults() {
		// CUS001: ステージ変更なし
		String cus001Stage = jdbcTemplate.queryForObject(
				"SELECT final_stage_code FROM customer_stage_calculations WHERE customer_id = 'CUS001'", String.class);
		assertThat(cus001Stage).isEqualTo("NONE");

		// CUS002: NONE → SILVER
		String cus002Stage = jdbcTemplate.queryForObject(
				"SELECT final_stage_code FROM customer_stage_calculations WHERE customer_id = 'CUS002'", String.class);
		assertThat(cus002Stage).isEqualTo("SILVER");

		// CUS003: SILVER → GOLD
		String cus003Stage = jdbcTemplate.queryForObject(
				"SELECT final_stage_code FROM customer_stage_calculations WHERE customer_id = 'CUS003'", String.class);
		assertThat(cus003Stage).isEqualTo("GOLD");

		// CUS008: SILVER → PLATINUM
		String cus008Stage = jdbcTemplate.queryForObject(
				"SELECT final_stage_code FROM customer_stage_calculations WHERE customer_id = 'CUS008'", String.class);
		assertThat(cus008Stage).isEqualTo("PLATINUM");

		// CUS011: NONE → PLATINUM（ダブルランクアップ）
		String cus011Stage = jdbcTemplate.queryForObject(
				"SELECT final_stage_code FROM customer_stage_calculations WHERE customer_id = 'CUS011'", String.class);
		assertThat(cus011Stage).isEqualTo("PLATINUM");
	}

}