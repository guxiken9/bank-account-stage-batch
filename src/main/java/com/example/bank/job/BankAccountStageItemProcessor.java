package com.example.bank.job;

import com.example.bank.service.StageEvaluationService;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@StepScope
public class BankAccountStageItemProcessor implements ItemProcessor<BankAccountStageInput, ProcessResult> {

	private final StageEvaluationService stageEvaluationService;

	@Autowired
	public BankAccountStageItemProcessor(StageEvaluationService stageEvaluationService) {
		this.stageEvaluationService = stageEvaluationService;
	}

	@Override
	public ProcessResult process(BankAccountStageInput input) throws Exception {
		return stageEvaluationService.evaluateStage(input);
	}

}
