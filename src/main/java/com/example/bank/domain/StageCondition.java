package com.example.bank.domain;

import java.math.BigDecimal;

public record StageCondition(Long id, String conditionType, String stageCode, BigDecimal minValue,
		BigDecimal maxValue) {
}