package com.example.bank.domain;

import java.math.BigDecimal;

public record RankChangeCondition(Long id, String conditionType, BigDecimal thresholdValue, int rankChangeLevels) {
}