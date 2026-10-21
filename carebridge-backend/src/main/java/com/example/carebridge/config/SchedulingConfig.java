package com.example.carebridge.config;

import com.example.carebridge.scheduling.*;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

@Configuration
public class SchedulingConfig {
 private static final Logger log = LoggerFactory.getLogger(SchedulingConfig.class);
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
     log.warn("AI scheduling failed; using rule-based recommendations", ex);
     return rules.recommend(appointment, slots, providers, reason);
    }
  };
 }
}
