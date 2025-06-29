package com.example.bank.repository;

import com.example.bank.job.ProcessResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class StageTransitionRepository {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public StageTransitionRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void insertTransition(Long calculationId, ProcessResult result) {
		String sql = """
				INSERT INTO stage_transitions (
				    customer_id, calculation_id, previous_stage_code,
				    current_stage_code, transition_date
				) VALUES (?, ?, ?, ?, ?)
				""";

		jdbcTemplate.update(sql, result.customerId(), calculationId, result.currentStageCode(), result.finalStageCode(),
				result.validFrom());
	}

}