package team.soma.teto.health.evaluation.domain;

import team.soma.teto.health.reference.standard.domain.SchoolLevel;

public class SchoolLevelPolicy {

    public SchoolLevel resolve() {
        return SchoolLevel.HIGH;
    }
}
