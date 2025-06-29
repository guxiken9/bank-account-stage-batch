package com.example.bank.repository;

import com.example.bank.job.ProcessResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class ConditionEvaluationResultRepository {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public ConditionEvaluationResultRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public void insertEvaluationResults(Long calculationId, List<ProcessResult.ConditionEvaluation> evaluations,
			LocalDate calculationDate) {
		String sql = """
				INSERT INTO condition_evaluation_results (
				    calculation_id, condition_id, is_met, evaluated_value
				)
				SELECT ?, c.id, ?, ?
				FROM conditions c
				WHERE c.condition_type = ?::condition_type_enum
				AND c.valid_from <= ? AND c.valid_to >= ?
				""";

		for (ProcessResult.ConditionEvaluation evaluation : evaluations) {
			jdbcTemplate.update(sql, calculationId, evaluation.isMet(), evaluation.evaluatedValue(),
					evaluation.conditionType(), calculationDate, calculationDate);
		}
	}

}