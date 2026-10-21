package com.carebridge.config;

import com.carebridge.scheduling.SchedulingEngine;
import com.carebridge.scheduling.SchedulingRecommendation;
import com.carebridge.scheduling.SchedulingStrategy;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SchedulingConfig {
    @Bean
    @ConditionalOnProperty(name = "scheduling.strategy", havingValue = "rule-based", matchIfMissing = true)
    SchedulingEngine ruleBasedEngine(@Qualifier("ruleBasedSchedulingStrategy") SchedulingStrategy strategy) {
        return strategy::recommend;
    }

    @Bean
    @ConditionalOnProperty(name = "scheduling.strategy", havingValue = "ai")
    SchedulingEngine aiEngine(@Qualifier("aiSchedulingStrategy") SchedulingStrategy ai,
                              @Qualifier("ruleBasedSchedulingStrategy") SchedulingStrategy rules) {
        return (appointment, slots, providers, reason) -> {
            try {
                List<SchedulingRecommendation> results = ai.recommend(appointment, slots, providers, reason);
                if (results == null || results.isEmpty()) return rules.recommend(appointment, slots, providers, reason);
                return results;
            } catch (Exception ex) {
                return rules.recommend(appointment, slots, providers, reason);
            }
        };
    }
}
