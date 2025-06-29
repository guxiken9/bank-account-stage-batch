package com.example.bank.repository;

import com.example.bank.domain.RankChangeCondition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class RankChangeConditionRepository {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public RankChangeConditionRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<RankChangeCondition> findValidConditions(LocalDate calculationDate) {
		String sql = """
				SELECT c.id, c.condition_type, rc.threshold_value, rc.rank_change_levels
				FROM conditions c
				INNER JOIN rank_change_conditions rc ON c.id = rc.id
				WHERE c.condition_category = 'RANK_CHANGE'
				AND c.valid_from <= ? AND c.valid_to >= ?
				ORDER BY c.condition_type
				""";

		return jdbcTemplate.query(sql, rankChangeConditionRowMapper(), calculationDate, calculationDate);
	}

	private RowMapper<RankChangeCondition> rankChangeConditionRowMapper() {
		return (rs, rowNum) -> new RankChangeCondition(rs.getLong("id"), rs.getString("condition_type"),
				rs.getBigDecimal("threshold_value"), rs.getInt("rank_change_levels"));
	}

}