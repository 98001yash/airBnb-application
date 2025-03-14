package com.company.projects.airBnbApp.strategy;

import com.company.projects.airBnbApp.entity.Inventory;

import java.math.BigDecimal;

public interface PricingStrategy {
    BigDecimal calculatePrice(Inventory inventory);
}

