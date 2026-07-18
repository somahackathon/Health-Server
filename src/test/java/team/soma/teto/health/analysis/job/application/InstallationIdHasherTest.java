package team.soma.teto.health.analysis.job.application;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class InstallationIdHasherTest {

    private final InstallationIdHasher hasher = new InstallationIdHasher();

    @Test
    void sameInstallationIdProducesSameHash() {
        String installationId = "11111111-1111-4111-8111-111111111111";

        assertThat(hasher.hash(installationId)).isEqualTo(hasher.hash(installationId));
    }

    @Test
    void differentInstallationIdsProduceDifferentHashes() {
        String first = hasher.hash("11111111-1111-4111-8111-111111111111");
        String second = hasher.hash("22222222-2222-4222-8222-222222222222");

        assertThat(first).isNotEqualTo(second);
    }

    @Test
    void hashDoesNotContainOriginalInstallationId() {
        String installationId = "11111111-1111-4111-8111-111111111111";

        String hash = hasher.hash(installationId);

        assertThat(hash).hasSize(64);
        assertThat(hash).doesNotContain(installationId);
        assertThat(hash).matches("[0-9a-f]{64}");
    }
}
