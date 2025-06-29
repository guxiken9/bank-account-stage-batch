package com.example.bank.repository;

import com.example.bank.domain.Stage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class StageRepository {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public StageRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public List<Stage> findValidStages(LocalDate calculationDate) {
		String sql = """
				SELECT stage_code, stage_name, stage_order
				FROM stages
				WHERE valid_from <= ? AND valid_to >= ?
				ORDER BY stage_order
				""";

		return jdbcTemplate.query(sql, stageRowMapper(), calculationDate, calculationDate);
	}

	private RowMapper<Stage> stageRowMapper() {
		return (rs, rowNum) -> new Stage(rs.getString("stage_code"), rs.getString("stage_name"),
				rs.getInt("stage_order"));
	}

}