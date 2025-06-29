package com.example.bank.repository;

import com.example.bank.domain.StageCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StageConditionRepository {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public StageConditionRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<StageCondition> findValidConditions(LocalDate calculationDate) {
		String sql = """
				SELECT c.id, c.condition_type, sc.stage_code, sc.min_value, sc.max_value
				FROM conditions c
				INNER JOIN stage_conditions sc ON c.id = sc.id
				WHERE c.condition_category = 'STAGE'
				AND c.valid_from <= ? AND c.valid_to >= ?
				ORDER BY c.condition_type, sc.stage_code
				""";

		return jdbcTemplate.query(sql, stageConditionRowMapper(), calculationDate, calculationDate);
	}

	private RowMapper<StageCondition> stageConditionRowMapper() {
		return (rs, rowNum) -> new StageCondition(rs.getLong("id"), rs.getString("condition_type"),
				rs.getString("stage_code"), rs.getBigDecimal("min_value"), rs.getBigDecimal("max_value"));
	}

}