package com.cryptomining.platform.config;

import com.cryptomining.platform.entity.*;
import com.cryptomining.platform.repository.*;
import com.cryptomining.platform.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final MiningWorkerRepository workerRepository;
    private final MiningPoolRepository poolRepository;
    private final NotificationService notificationService;

    @Override
    public void run(String... args) {
        userRepository.findByEmail("user@platform.local").ifPresent(user -> {
            if (workerRepository.findByUserId(user.getId()).isEmpty()) {
                log.info("Initializing demo mining workers for user");
                MiningPool pool = poolRepository.findAll().stream()
                    .filter(p -> p.getName().equals("Simulation Pool"))
                    .findFirst().orElse(poolRepository.findAll().get(0));

                for (int i = 1; i <= 3; i++) {
                    workerRepository.save(MiningWorker.builder()
                        .user(user)
                        .pool(pool)
                        .workerName("Rig-0" + i)
                        .workerId("SIM-WRK-00" + i)
                        .deviceType(i == 3 ? MiningWorker.DeviceType.GPU : MiningWorker.DeviceType.ASIC)
                        .status(MiningWorker.WorkerStatus.OFFLINE)
                        .build());
                }

                notificationService.createNotification(user.getId(),
                    "Welcome to Crypto Mining Platform",
                    "Your account has been set up. Connect your mining workers to get started.",
                    Notification.NotificationType.INFO);
            }
        });
    }
}
