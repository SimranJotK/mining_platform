package com.cryptomining.platform.scheduler;

import com.cryptomining.platform.entity.User;
import com.cryptomining.platform.repository.UserRepository;
import com.cryptomining.platform.service.MiningService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MiningDataScheduler {

    private final MiningService miningService;
    private final UserRepository userRepository;

    @Scheduled(fixedRate = 60000)
    public void collectMiningData() {
        log.debug("Running mining data collection scheduler");
        List<User> users = userRepository.findAll();
        for (User user : users) {
            try {
                miningService.simulateMiningData(user.getId());
            } catch (Exception e) {
                log.warn("Failed to collect data for user {}: {}", user.getId(), e.getMessage());
            }
        }
    }
}
