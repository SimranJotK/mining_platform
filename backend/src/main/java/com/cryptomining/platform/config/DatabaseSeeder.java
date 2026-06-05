package com.cryptomining.platform.config;

import com.cryptomining.platform.entity.*;
import com.cryptomining.platform.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import java.util.*;

@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DatabaseSeeder implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final UserRepository userRepository;
    private final MiningPoolRepository poolRepository;
    private final SystemConfigurationRepository configRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (roleRepository.count() > 0) {
            return;
        }
        log.info("Seeding database with default roles, permissions, pools, and users");

        Map<String, Permission> permissions = seedPermissions();
        seedRoles(permissions);
        seedPools();
        seedSystemConfigurations();
        seedUsers();
    }

    private Map<String, Permission> seedPermissions() {
        Map<String, String> defs = Map.ofEntries(
            Map.entry("VIEW_OWN_STATS", "View personal mining statistics"),
            Map.entry("CONNECT_MINING", "Connect mining accounts"),
            Map.entry("VIEW_AI_PREDICTIONS", "View AI predictions"),
            Map.entry("VIEW_ENCRYPTED_DATA", "View own encrypted data"),
            Map.entry("MANAGE_SETTINGS", "Manage own settings"),
            Map.entry("MANAGE_USERS", "Manage platform users"),
            Map.entry("SUSPEND_USERS", "Suspend user accounts"),
            Map.entry("VIEW_SYSTEM_LOGS", "Review system logs"),
            Map.entry("VIEW_PLATFORM_ANALYTICS", "View platform analytics"),
            Map.entry("MANAGE_INFRASTRUCTURE", "Full infrastructure control"),
            Map.entry("MANAGE_ADMINS", "Manage administrator accounts"),
            Map.entry("DEPLOY_SERVICES", "Deploy platform services"),
            Map.entry("CONFIGURE_APIS", "Configure API integrations"),
            Map.entry("MONITOR_PLATFORM", "Monitor platform health")
        );

        Map<String, Permission> result = new LinkedHashMap<>();
        defs.forEach((name, description) ->
            result.put(name, permissionRepository.save(Permission.builder()
                .name(name)
                .description(description)
                .build()))
        );
        return result;
    }

    private void seedRoles(Map<String, Permission> permissions) {
        createRole("ROLE_USER", "Standard user with mining monitoring access",
            List.of("VIEW_OWN_STATS", "CONNECT_MINING", "VIEW_AI_PREDICTIONS",
                "VIEW_ENCRYPTED_DATA", "MANAGE_SETTINGS"), permissions);

        createRole("ROLE_ADMIN", "Administrator with user and system management",
            List.of("VIEW_OWN_STATS", "VIEW_AI_PREDICTIONS", "MANAGE_SETTINGS",
                "MANAGE_USERS", "SUSPEND_USERS", "VIEW_SYSTEM_LOGS", "VIEW_PLATFORM_ANALYTICS"), permissions);

        createRole("ROLE_CREATOR", "Platform creator with full infrastructure control",
            List.of("VIEW_OWN_STATS", "VIEW_AI_PREDICTIONS", "MANAGE_SETTINGS",
                "MANAGE_USERS", "SUSPEND_USERS", "VIEW_SYSTEM_LOGS", "VIEW_PLATFORM_ANALYTICS",
                "MANAGE_INFRASTRUCTURE", "MANAGE_ADMINS", "DEPLOY_SERVICES",
                "CONFIGURE_APIS", "MONITOR_PLATFORM"), permissions);
    }

    private void createRole(String name, String description,
                            List<String> permissionNames, Map<String, Permission> permissions) {
        Role role = Role.builder()
            .name(name)
            .description(description)
            .permissions(new HashSet<>())
            .build();
        permissionNames.forEach(p -> role.getPermissions().add(permissions.get(p)));
        roleRepository.save(role);
    }

    private void seedPools() {
        List<MiningPool> pools = List.of(
            MiningPool.builder().name("Slush Pool")
                .apiUrl("https://slushpool.com/accounts/profile/json/btc")
                .poolType(MiningPool.PoolType.BTC).apiKeyRequired(true).build(),
            MiningPool.builder().name("F2Pool")
                .apiUrl("https://api.f2pool.com/btc")
                .poolType(MiningPool.PoolType.BTC).apiKeyRequired(true).build(),
            MiningPool.builder().name("Antpool")
                .apiUrl("https://antpool.com/api")
                .poolType(MiningPool.PoolType.BTC).apiKeyRequired(true).build(),
            MiningPool.builder().name("Ethermine")
                .apiUrl("https://api.ethermine.org")
                .poolType(MiningPool.PoolType.ETH).apiKeyRequired(false).build(),
            MiningPool.builder().name("Simulation Pool")
                .apiUrl("http://localhost:8080/api/v1/simulation/pool")
                .poolType(MiningPool.PoolType.BTC).apiKeyRequired(false).build()
        );
        poolRepository.saveAll(pools);
    }

    private void seedSystemConfigurations() {
        List<SystemConfiguration> configs = List.of(
            config("mining.simulation.enabled", "true", "Enable mining simulation mode"),
            config("ai.service.url", "http://ai-service:5000", "AI microservice URL"),
            config("security.rate_limit.requests", "100", "Rate limit requests per minute"),
            config("security.jwt.expiration", "900", "JWT expiration in seconds"),
            config("security.refresh.expiration", "604800", "Refresh token expiration in seconds")
        );
        configRepository.saveAll(configs);
    }

    private SystemConfiguration config(String key, String value, String description) {
        return SystemConfiguration.builder()
            .configKey(key)
            .configValue(value)
            .description(description)
            .build();
    }

    private void seedUsers() {
        createUser("creator@platform.local", "creator", "Creator@123",
            "Platform", "Creator", "ROLE_CREATOR");
        createUser("admin@platform.local", "admin", "Admin@123",
            "System", "Admin", "ROLE_ADMIN");
        createUser("user@platform.local", "user", "User@123",
            "Demo", "User", "ROLE_USER");
    }

    private void createUser(String email, String username, String password,
                            String firstName, String lastName, String roleName) {
        Role role = roleRepository.findByName(roleName)
            .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));
        User user = User.builder()
            .email(email)
            .username(username)
            .passwordHash(passwordEncoder.encode(password))
            .firstName(firstName)
            .lastName(lastName)
            .accountStatus(User.AccountStatus.ACTIVE)
            .roles(new HashSet<>(Set.of(role)))
            .build();
        userRepository.save(user);
    }
}
