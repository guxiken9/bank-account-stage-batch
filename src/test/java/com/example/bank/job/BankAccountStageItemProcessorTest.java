package com.example.bank.job;

import com.example.bank.service.StageEvaluationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BankAccountStageItemProcessorTest {

	@Mock
	private StageEvaluationService stageEvaluationService;

	private BankAccountStageItemProcessor processor;

	@BeforeEach
	void setUp() {
		processor = new BankAccountStageItemProcessor(stageEvaluationService);
	}

	@Test
	void testProcess() throws Exception {
		// Given
		BankAccountStageInput input = new BankAccountStageInput("CUS001", "NONE", LocalDate.of(2025, 4, 30),
				new BigDecimal("2500000"), BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO);

		ProcessResult expectedResult = new ProcessResult("CUS001", "NONE", "NONE", LocalDate.of(2025, 4, 30),
				LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31), new BigDecimal("2500000"), BigDecimal.ZERO,
				BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
				Collections.emptyList(), false);

		when(stageEvaluationService.evaluateStage(input)).thenReturn(expectedResult);

		// When
		ProcessResult result = processor.process(input);

		// Then
		assertThat(result).isEqualTo(expectedResult);
		verify(stageEvaluationService).evaluateStage(input);
	}

}