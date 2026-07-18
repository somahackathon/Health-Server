package team.soma.teto.health.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class PapsOfficialStandardDataIntegrityTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void officialStandardVersionIsSingleActiveVersion() {
        Integer activeVersionCount = jdbcTemplate.queryForObject("select count(*) from paps_standard_version where active = true", Integer.class);
        String activeVersionCode = jdbcTemplate.queryForObject("select code from paps_standard_version where active = true", String.class);
        Boolean official = jdbcTemplate.queryForObject("select official from paps_standard_version where code = 'PAPS_OFFICIAL_2025_V1'", Boolean.class);

        assertThat(activeVersionCount).isEqualTo(1);
        assertThat(activeVersionCode).isEqualTo("PAPS_OFFICIAL_2025_V1");
        assertThat(official).isTrue();
    }

    @Test
    void officialGeneralStandardsHaveFiveGradesPerSupportedCombination() {
        Integer supportedCombinationCount = jdbcTemplate.queryForObject("""
                select count(*)
                from (
                    select test_item_id, school_level, school_grade, gender
                    from paps_standard standard
                    join paps_standard_version version on version.id = standard.version_id
                    where version.code = 'PAPS_OFFICIAL_2025_V1'
                    group by test_item_id, school_level, school_grade, gender
                    having count(*) = 5 and count(distinct grade) = 5
                ) combinations
                """, Integer.class);
        Integer rowCount = jdbcTemplate.queryForObject("""
                select count(*)
                from paps_standard standard
                join paps_standard_version version on version.id = standard.version_id
                where version.code = 'PAPS_OFFICIAL_2025_V1'
                """, Integer.class);

        assertThat(supportedCombinationCount).isEqualTo(170);
        assertThat(rowCount).isEqualTo(850);
    }

    @Test
    void unsupportedBodyFatPercentageIsNotExposedAsActiveItem() {
        Boolean active = jdbcTemplate.queryForObject("select active from fitness_test_item where code = 'BODY_FAT_PERCENTAGE'", Boolean.class);
        Integer standardCount = jdbcTemplate.queryForObject("""
                select count(*)
                from paps_standard standard
                join fitness_test_item item on item.id = standard.test_item_id
                where item.code = 'BODY_FAT_PERCENTAGE'
                """, Integer.class);

        assertThat(active).isFalse();
        assertThat(standardCount).isZero();
    }

    @Test
    void elementaryPushUpIsNotSeededBecauseOfficialTableDoesNotConductIt() {
        Integer elementaryPushUpCount = jdbcTemplate.queryForObject("""
                select count(*)
                from paps_standard standard
                join paps_standard_version version on version.id = standard.version_id
                join fitness_test_item item on item.id = standard.test_item_id
                where version.code = 'PAPS_OFFICIAL_2025_V1'
                  and item.code = 'PUSH_UP'
                  and standard.school_level = 'ELEMENTARY'
                """, Integer.class);

        assertThat(elementaryPushUpCount).isZero();
    }

    @Test
    void bmiUsesOfficialCategoriesAndMaleHighGradeTwoAndThreeDoNotHaveOverweight() {
        Integer bmiRowCount = jdbcTemplate.queryForObject("""
                select count(*)
                from paps_bmi_standard standard
                join paps_standard_version version on version.id = standard.version_id
                where version.code = 'PAPS_OFFICIAL_2025_V1'
                """, Integer.class);
        List<String> maleHighGradeTwoCategories = jdbcTemplate.queryForList("""
                select category
                from paps_bmi_standard standard
                join paps_standard_version version on version.id = standard.version_id
                where version.code = 'PAPS_OFFICIAL_2025_V1'
                  and standard.school_level = 'HIGH'
                  and standard.school_grade = 2
                  and standard.gender = 'MALE'
                order by category
                """, String.class);
        List<String> maleHighGradeThreeCategories = jdbcTemplate.queryForList("""
                select category
                from paps_bmi_standard standard
                join paps_standard_version version on version.id = standard.version_id
                where version.code = 'PAPS_OFFICIAL_2025_V1'
                  and standard.school_level = 'HIGH'
                  and standard.school_grade = 3
                  and standard.gender = 'MALE'
                order by category
                """, String.class);

        assertThat(bmiRowCount).isEqualTo(88);
        assertThat(maleHighGradeTwoCategories).doesNotContain("OVERWEIGHT");
        assertThat(maleHighGradeThreeCategories).doesNotContain("OVERWEIGHT");
    }
}
