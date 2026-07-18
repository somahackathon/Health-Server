# PAPS Standard Data

## Active Version

- code: `PAPS_OFFICIAL_2025_V1`
- name: `2025 PAPS Official Standard V1`
- official: `true`
- source type: `OFFICIAL`
- source name: `학교건강검사규칙 별표 4 신체능력검사 기준표`
- source URL: `https://www.law.go.kr/LSW/flDownload.do?bylClsCd=110201&flSeq=149708821&gubun=`
- effective from: `2025-03-10`

`HACKATHON_V1` remains in the database for traceability but is deactivated by the official seed migration.

## Scope

Official rows are seeded for:

- elementary school grades 4 to 6
- middle school grades 1 to 3
- high school grades 1 to 3
- `MALE` and `FEMALE`

The official criteria are school-grade based. The server stores `minimum_age=0` and `maximum_age=99` for compatibility, but evaluation lookup uses `schoolLevel`, `schoolGrade`, `gender`, and `testItem`.

## General Fitness Standards

General item-level standards are stored in `paps_standard` as 1 to 5 grade intervals.

The seed migration inserts 170 supported school-level, grade, gender, and item combinations, producing 850 grade rows.

Supported official item codes:

- `SHUTTLE_RUN`
- `LONG_RUN_WALK`
- `STEP_TEST`
- `SIT_AND_REACH`
- `TOTAL_FLEXIBILITY`
- `PUSH_UP`
- `CURL_UP`
- `GRIP_STRENGTH`
- `SPRINT_50M`
- `STANDING_LONG_JUMP`

Elementary grade 4 to 6 `PUSH_UP` is not seeded because the official table says elementary grades 3 to 6 do not conduct that item. For girls, the official item is knee push-up; the server uses the shared `PUSH_UP` code and applies the female official knee-push-up ranges.

## Boundary Conversion

The official table prints display ranges for grade 5 and grade 1, but those outer ranges are not closed scoring limits. Values outside those printed ranges still map to grade 5 or grade 1 respectively.

For higher-is-better items, a row such as:

- grade 5: 19 to 25
- grade 4: 26 to 44
- grade 3: 45 to 68
- grade 2: 69 to 95
- grade 1: 96 to 103

is stored as:

- grade 5: `< 26`
- grade 4: `26 <= value < 45`
- grade 3: `45 <= value < 69`
- grade 2: `69 <= value < 96`
- grade 1: `96 <= value`

Lower-is-better items use the same adjacent half-open interval principle in the opposite direction.

## BMI Standards

BMI is not mapped to the general 1 to 5 grade scale. It is stored separately in `paps_bmi_standard` and returned as one of:

- `THIN`
- `NORMAL`
- `OVERWEIGHT`
- `MILD_OBESITY`
- `SEVERE_OBESITY`

The seed migration inserts 88 BMI category rows. Male high school grade 2 and grade 3 do not have an `OVERWEIGHT` category in the official table, so no artificial row is inserted for those combinations.

BMI is used only as a fitness-management reference category and must not be presented as medical diagnosis or appearance evaluation.

## Excluded Item

`BODY_FAT_PERCENTAGE` is not seeded because this official source does not provide body-fat-percentage criteria. The migration deactivates this item so the RN app does not expose an unsupported official evaluation item.

## Validation

The test suite verifies:

- exactly one active official version
- MariaDB Flyway migration success through Testcontainers
- 850 general standard rows
- 88 BMI standard rows
- 1 to 5 grade completeness for every supported general item combination
- no elementary `PUSH_UP` standards
- no active `BODY_FAT_PERCENTAGE`
- no artificial male high school grade 2 or grade 3 `OVERWEIGHT` BMI category
- representative official evaluation and boundary values through `EvaluatePapsService`
