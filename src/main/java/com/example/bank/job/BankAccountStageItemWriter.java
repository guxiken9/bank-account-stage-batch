package com.example.bank.job;

import com.example.bank.repository.ConditionEvaluationResultRepository;
import com.example.bank.repository.CustomerStageCalculationRepository;
import com.example.bank.repository.StageTransitionRepository;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@StepScope
public class BankAccountStageItemWriter implements ItemWriter<ProcessResult> {

	private final CustomerStageCalculationRepository calculationRepository;

	private final ConditionEvaluationResultRepository evaluationRepository;

	private final StageTransitionRepository transitionRepository;

	@Autowired
	public BankAccountStageItemWriter(CustomerStageCalculationRepository calculationRepository,
			ConditionEvaluationResultRepository evaluationRepository, StageTransitionRepository transitionRepository) {
		this.calculationRepository = calculationRepository;
		this.evaluationRepository = evaluationRepository;
		this.transitionRepository = transitionRepository;
	}

	@Override
	@Transactional
	public void write(Chunk<? extends ProcessResult> chunk) throws Exception {
		for (ProcessResult result : chunk) {
			// 1. 計算結果を保存
			Long calculationId = calculationRepository.insertCalculation(result);

			// 2. 条件評価結果を保存
			evaluationRepository.insertEvaluationResults(calculationId, result.conditionEvaluations(),
					result.calculationDate());

			// 3. ステージ遷移があれば記録
			if (result.hasStageTransition()) {
				transitionRepository.insertTransition(calculationId, result);
			}
		}
	}

}
