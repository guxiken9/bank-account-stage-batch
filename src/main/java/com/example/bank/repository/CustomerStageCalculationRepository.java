package com.example.bank.repository;

import com.example.bank.job.ProcessResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.Map;

@Repository
public class CustomerStageCalculationRepository {

	private final JdbcTemplate jdbcTemplate;

	@Autowired
	public CustomerStageCalculationRepository(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	public Long insertCalculation(ProcessResult result) {
		String sql = """
				INSERT INTO customer_stage_calculations (
				    customer_id, calculation_date, valid_from, valid_to,
				    current_stage_code, final_stage_code,
				    total_balance, foreign_currency_balance, investment_trust_balance,
				    monthly_foreign_currency_purchase, monthly_investment_trust_purchase,
				    housing_loan_balance, monthly_fx_trading_volume
				) VALUES (?, ?, ?, ?, ?::stage_code_enum, ?::stage_code_enum, ?, ?, ?, ?, ?, ?, ?)
				""";

		KeyHolder keyHolder = new GeneratedKeyHolder();

		jdbcTemplate.update(connection -> {
			PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
			ps.setString(1, result.customerId());
			ps.setDate(2, java.sql.Date.valueOf(result.calculationDate()));
			ps.setDate(3, java.sql.Date.valueOf(result.validFrom()));
			ps.setDate(4, java.sql.Date.valueOf(result.validTo()));
			ps.setString(5, result.currentStageCode());
			ps.setString(6, result.finalStageCode());
			ps.setBigDecimal(7, result.totalBalance());
			ps.setBigDecimal(8, result.foreignCurrencyBalance());
			ps.setBigDecimal(9, result.investmentTrustBalance());
			ps.setBigDecimal(10, result.monthlyForeignCurrencyPurchase());
			ps.setBigDecimal(11, result.monthlyInvestmentTrustPurchase());
			ps.setBigDecimal(12, result.housingLoanBalance());
			ps.setInt(13, result.monthlyFxTradingVolume().intValue());
			return ps;
		}, keyHolder);

		Map<String, Object> keys = keyHolder.getKeys();
		return ((Number) keys.get("id")).longValue();
	}

}