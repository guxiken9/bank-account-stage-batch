package com.example.bank.service;

import com.example.bank.domain.RankChangeCondition;
import com.example.bank.domain.Stage;
import com.example.bank.domain.StageCondition;
import com.example.bank.job.BankAccountStageInput;
import com.example.bank.job.ProcessResult;
import com.example.bank.repository.RankChangeConditionRepository;
import com.example.bank.repository.StageConditionRepository;
import com.example.bank.repository.StageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StageEvaluationServiceTest {

	@Mock
	private StageConditionRepository stageConditionRepository;

	@Mock
	private RankChangeConditionRepository rankChangeConditionRepository;

	@Mock
	private StageRepository stageRepository;

	private StageEvaluationService service;

	@BeforeEach
	void setUp() {
		service = new StageEvaluationService(stageConditionRepository, rankChangeConditionRepository, stageRepository);

		// Setup default mocks
		when(stageRepository.findValidStages(any())).thenReturn(List.of(new Stage("NONE", "ステージなし", 0),
				new Stage("SILVER", "シルバー", 100), new Stage("GOLD", "ゴールド", 200), new Stage("PLATINUM", "プラチナ", 300)));

		when(stageConditionRepository.findValidConditions(any())).thenReturn(List.of(
				new StageCondition(1L, "TOTAL_BALANCE", "SILVER", new BigDecimal("3000000"), null),
				new StageCondition(2L, "MONTHLY_FOREIGN_CURRENCY_PURCHASE", "SILVER", new BigDecimal("30000"), null),
				new StageCondition(3L, "MONTHLY_INVESTMENT_TRUST_PURCHASE", "SILVER", new BigDecimal("30000"), null),
				new StageCondition(4L, "COMBINED_BALANCE_GOLD", "GOLD", new BigDecimal("5000000"),
						new BigDecimal("10000000")),
				new StageCondition(5L, "COMBINED_BALANCE_PLATINUM", "PLATINUM", new BigDecimal("10000000"), null)));

		when(rankChangeConditionRepository.findValidConditions(any()))
			.thenReturn(List.of(new RankChangeCondition(6L, "HOUSING_LOAN", new BigDecimal("1"), 1),
					new RankChangeCondition(7L, "FX_TRADING", new BigDecimal("1000"), 1)));
	}

	@Test
	void testBasicStageNone() {
		// 基本条件を満たさない顧客（CUS001）
		BankAccountStageInput input = new BankAccountStageInput("CUS001", "NONE", LocalDate.of(2025, 4, 30),
				new BigDecimal("2500000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO);

		ProcessResult result = service.evaluateStage(input);

		assertThat(result.finalStageCode()).isEqualTo("NONE");
		assertThat(result.hasStageTransition()).isFalse();
	}

	@Test
	void testSilverByTotalBalance() {
		// 総残高でシルバーステージ獲得（CUS002）
		BankAccountStageInput input = new BankAccountStageInput("CUS002", "NONE", LocalDate.of(2025, 4, 30),
				new BigDecimal("3500000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO);

		ProcessResult result = service.evaluateStage(input);

		assertThat(result.finalStageCode()).isEqualTo("SILVER");
		assertThat(result.hasStageTransition()).isTrue();
	}

	@Test
	void testGoldByCombinedBalance() {
		// 外貨と投資信託の合計額でゴールドステージ獲得（CUS003）
		BankAccountStageInput input = new BankAccountStageInput("CUS003", "SILVER", LocalDate.of(2025, 4, 30),
				new BigDecimal("8800000"), new BigDecimal("3000000"), new BigDecimal("3000000"), BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

		ProcessResult result = service.evaluateStage(input);

		assertThat(result.finalStageCode()).isEqualTo("GOLD");
		assertThat(result.hasStageTransition()).isTrue();
	}

	@Test
	void testPlatinumByCombinedBalance() {
		// シルバーからプラチナへ投資残高増加でアップグレード（CUS008）
		BankAccountStageInput input = new BankAccountStageInput("CUS008", "SILVER", LocalDate.of(2025, 4, 30),
				new BigDecimal("13000000"), new BigDecimal("6000000"), new BigDecimal("6000000"), BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

		ProcessResult result = service.evaluateStage(input);

		assertThat(result.finalStageCode()).isEqualTo("PLATINUM");
		assertThat(result.hasStageTransition()).isTrue();
	}

	@Test
	void testSilverByForeignCurrencyPurchase() {
		// 外貨預金の積立購入でシルバーステージ獲得（CUS006）
		BankAccountStageInput input = new BankAccountStageInput("CUS006", "NONE", LocalDate.of(2025, 4, 30),
				new BigDecimal("1000000"), BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("40000"), BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO);

		ProcessResult result = service.evaluateStage(input);

		assertThat(result.finalStageCode()).isEqualTo("SILVER");
		assertThat(result.hasStageTransition()).isTrue();
	}

	@Test
	void testSilverByInvestmentPurchase() {
		// 投資信託の積立購入でシルバーステージ獲得（CUS007）
		BankAccountStageInput input = new BankAccountStageInput("CUS007", "NONE", LocalDate.of(2025, 4, 30),
				new BigDecimal("1000000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, new BigDecimal("40000"),
				BigDecimal.ZERO, BigDecimal.ZERO);

		ProcessResult result = service.evaluateStage(input);

		assertThat(result.finalStageCode()).isEqualTo("SILVER");
		assertThat(result.hasStageTransition()).isTrue();
	}

	@Test
	void testRankUpByHousingLoan() {
		// 住宅ローンによるランクアップ（CUS009）
		BankAccountStageInput input = new BankAccountStageInput("CUS009", "NONE", LocalDate.of(2025, 4, 30),
				new BigDecimal("1000000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				new BigDecimal("10000000"), BigDecimal.ZERO);

		ProcessResult result = service.evaluateStage(input);

		assertThat(result.finalStageCode()).isEqualTo("SILVER");
		assertThat(result.hasStageTransition()).isTrue();
	}

	@Test
	void testRankUpByFxTrading() {
		// FX取引によるランクアップ（CUS010）
		BankAccountStageInput input = new BankAccountStageInput("CUS010", "NONE", LocalDate.of(2025, 4, 30),
				new BigDecimal("1000000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				BigDecimal.ZERO, new BigDecimal("1200"));

		ProcessResult result = service.evaluateStage(input);

		assertThat(result.finalStageCode()).isEqualTo("SILVER");
		assertThat(result.hasStageTransition()).isTrue();
	}

	@Test
	void testDoubleRankUp() {
		// 複数条件によるダブルランクアップ（CUS011）
		BankAccountStageInput input = new BankAccountStageInput("CUS011", "NONE", LocalDate.of(2025, 4, 30),
				new BigDecimal("13000000"), new BigDecimal("6000000"), new BigDecimal("6000000"), BigDecimal.ZERO,
				BigDecimal.ZERO, new BigDecimal("10000000"), new BigDecimal("1200"));

		ProcessResult result = service.evaluateStage(input);

		assertThat(result.finalStageCode()).isEqualTo("PLATINUM");
		assertThat(result.hasStageTransition()).isTrue();
	}

	@Test
	void testStageDowngrade() {
		// 投資残高減少でゴールドからシルバーへダウングレード（CUS004）
		BankAccountStageInput input = new BankAccountStageInput("CUS004", "GOLD", LocalDate.of(2025, 4, 30),
				new BigDecimal("9000000"), new BigDecimal("1000000"), new BigDecimal("1000000"), BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);

		ProcessResult result = service.evaluateStage(input);

		assertThat(result.finalStageCode()).isEqualTo("SILVER");
		assertThat(result.hasStageTransition()).isTrue();
	}

}