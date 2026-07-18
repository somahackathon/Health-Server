package team.soma.teto.health.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.MariaDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import team.soma.teto.health.analysis.job.domain.AiAnalysisJob;
import team.soma.teto.health.analysis.job.domain.AnalysisType;
import team.soma.teto.health.analysis.job.repository.AiAnalysisJobRepository;

@SpringBootTest
@ActiveProfiles("test")
@Testcontainers(disabledWithoutDocker = true)
class MariaDbMigrationTest {

    @Container
    private static final MariaDBContainer<?> mariaDb = new MariaDBContainer<>("mariadb:11.4")
            .withDatabaseName("health")
            .withUsername("test")
            .withPassword("test");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private AiAnalysisJobRepository aiAnalysisJobRepository;

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mariaDb::getJdbcUrl);
        registry.add("spring.datasource.username", mariaDb::getUsername);
        registry.add("spring.datasource.password", mariaDb::getPassword);
        registry.add("spring.datasource.driver-class-name", mariaDb::getDriverClassName);
        registry.add("app.ai.mode", () -> "mock");
    }

    @Test
    void emptyMariaDbRunsFlywayAndValidatesSeedData() {
        Integer componentCount = jdbcTemplate.queryForObject("select count(*) from fitness_component", Integer.class);
        Integer testItemCount = jdbcTemplate.queryForObject("select count(*) from fitness_test_item", Integer.class);
        Integer activeVersionCount = jdbcTemplate.queryForObject("select count(*) from paps_standard_version where active = true", Integer.class);
        Integer standardCount = jdbcTemplate.queryForObject("select count(*) from paps_standard", Integer.class);

        assertThat(componentCount).isEqualTo(5);
        assertThat(testItemCount).isEqualTo(12);
        assertThat(activeVersionCount).isEqualTo(1);
        assertThat(standardCount).isZero();
    }

    @Test
    void aiJobLongPayloadCanBeStoredAndRead() {
        String longPayload = "{\"text\":\"" + "a".repeat(70_000) + "\"}";
        AiAnalysisJob job = AiAnalysisJob.create("hash", AnalysisType.FITNESS, longPayload, Instant.now().plus(Duration.ofHours(1)));

        AiAnalysisJob saved = aiAnalysisJobRepository.saveAndFlush(job);

        assertThat(aiAnalysisJobRepository.findById(saved.getId()).orElseThrow().getRequestPayload()).isEqualTo(longPayload);
    }
}
