# PAPS Standard Data

## Status

Official PAPS standard rows are not seeded yet.

The server can now represent official grade-level PAPS criteria without converting grade into age. Evaluation requests carry both `schoolLevel` and `schoolGrade`, and `paps_standard` stores both values.

Do not insert official PAPS ranges by mapping school grade to ages such as high school grade 1 equals age 15. Use the exact school level and school grade from the official table.

## Supported Classification Axes

- `schoolLevel`: `ELEMENTARY`, `MIDDLE`, `HIGH`
- `schoolGrade`: 1 to 6 for elementary school, 1 to 3 for middle or high school
- `gender`: `MALE`, `FEMALE`
- `testItem`: `fitness_test_item.code`
- `grade`: 1 to 5

`minimumAge` and `maximumAge` remain in the table for compatibility, but official criteria lookup uses `schoolLevel` and `schoolGrade`.

## Required Source Check

Before inserting official rows, verify the source document directly:

- issuing institution
- document title
- applicable year
- school level
- school grade
- gender
- test item
- unit
- grade 1 to 5 criteria
- inclusive or exclusive boundary wording

Allowed source types:

- Ministry of Education
- Student Health Information Center
- official Office of Education PAPS manuals
- National Law Information Center
- official public data from the Ministry of Education or Offices of Education

Do not use blogs, cafes, knowledge sites, private spreadsheets, or search result snippets.

## Official Source Candidates

These sources identify official or public-agency PAPS materials, but their attached tables still need direct numeric verification before seeding DB rows:

- Ministry of Education, `학생건강체력평가제(PAPS)측정 매뉴얼(2009 수정본)`, https://www.moe.go.kr/boardCnts/viewRenew.do?boardID=316&boardSeq=14508&lev=0&m=0302&opType=N&page=1&s=moe&searchType=null&statusYN=C
- Student Health Information Center, `학생건강체력평가(PAPS) 운영 매뉴얼`, source `제주특별자치도교육청`, https://www.schoolhealth.kr/web/search/selectTotalSearchList.do?bbsId=&bbsTyCode=&kwdLogYn=N&lstnum1=3744&pageIndex=1&pageUnit=10&searchWrd=%ED%95%99%EC%83%9D%EA%B1%B4%EA%B0%95%EC%B2%B4%EB%A0%A5%ED%8F%89%EA%B0%80%2F&sortOrder=
- Gyeonggi Provincial Office of Education, `학생건강체력평가(PAPS) 매뉴얼 및 단위학교 학생 체력증진 기본 계획`, https://www.goe.go.kr/goe/na/ntt/selectNttInfo.do?mi=10100&nttSn=106640

## Seed SQL Template

Use a new Flyway migration after verifying the official numbers. Do not edit existing migrations.

```sql
INSERT INTO paps_standard_version (
    code,
    name,
    source_type,
    source_name,
    source_url,
    official,
    active,
    created_at,
    updated_at
) VALUES (
    'PAPS_OFFICIAL_YYYY_V1',
    'PAPS official criteria YYYY',
    'OFFICIAL',
    '<official document title>',
    '<official source url>',
    TRUE,
    FALSE,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
);

INSERT INTO paps_standard (
    version_id,
    test_item_id,
    school_level,
    school_grade,
    gender,
    minimum_age,
    maximum_age,
    grade,
    minimum_value,
    maximum_value,
    minimum_inclusive,
    maximum_inclusive,
    created_at,
    updated_at
) VALUES
(
    (SELECT id FROM paps_standard_version WHERE code = 'PAPS_OFFICIAL_YYYY_V1'),
    (SELECT id FROM fitness_test_item WHERE code = '<TEST_ITEM_CODE>'),
    '<ELEMENTARY|MIDDLE|HIGH>',
    <SCHOOL_GRADE>,
    '<MALE|FEMALE>',
    0,
    99,
    <GRADE_1_TO_5>,
    <MINIMUM_VALUE_OR_NULL>,
    <MAXIMUM_VALUE_OR_NULL>,
    <TRUE_OR_FALSE>,
    <TRUE_OR_FALSE>,
    CURRENT_TIMESTAMP(6),
    CURRENT_TIMESTAMP(6)
);
```

Only activate the official version after the dataset covers the intended production scope:

```sql
UPDATE paps_standard_version
SET active = FALSE, updated_at = CURRENT_TIMESTAMP(6)
WHERE active = TRUE;

UPDATE paps_standard_version
SET active = TRUE, updated_at = CURRENT_TIMESTAMP(6)
WHERE code = 'PAPS_OFFICIAL_YYYY_V1';
```

## Data Entry Rules

- Keep official and internal criteria in separate versions.
- Use `code` lookups instead of numeric foreign key IDs.
- Keep the official unit and boundary precision.
- Express open-ended ranges with `NULL`, not artificial large numbers.
- Preserve official inclusive/exclusive wording exactly.
- Insert five grade rows for each supported school level, grade, gender, and test item combination.

## Validation Required

- Exactly one active standard version.
- Official version metadata has `sourceName` and `sourceUrl`.
- Every supported item, school level, school grade, gender, and grade combination exists.
- Each combination has grades 1 through 5.
- No overlapping intervals.
- No unintended gaps between adjacent intervals.
- Integer items do not use unnecessary decimal boundaries.
- Representative and boundary values evaluate through `PapsGradeEvaluator`.

## Unsupported In This Branch

- No official criterion number was inserted.
- No official standard version was activated.
- No PAPS criterion value was copied into Java code.
- No school grade was converted to age.
