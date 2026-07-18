# PAPS Standard Data

## Status

Official PAPS seed data is not added yet.

The official source uses school grade rows such as high school grade 1, grade 2, and grade 3. The current server contract now carries `schoolGrade`, and `PapsStandard` stores `schoolGrade` so official grade-level criteria can be entered without converting school grade into age.

Do not insert official PAPS grade ranges by mapping `HIGH 1/2/3` to ages such as 15/16/17. Use the explicit `schoolGrade` field.

## Checked Official Sources

- Ministry of Education, `학생건강체력평가(PAPS) 측정 매뉴얼 2009 수정본`, published 2009-04-27, https://www.moe.go.kr/boardCnts/viewRenew.do?boardID=316&boardSeq=14508&lev=0&m=0302&opType=N&page=1&s=moe&searchType=null&statusYN=C
- Student Health Information Center, `학생건강체력평가(PAPS) 운영 매뉴얼`, production date 2020-04-01, registered 2022-02-28, https://www.schoolhealth.kr/web/search/selectTotalSearchList.do?bbsId=&bbsTyCode=&kwdLogYn=N&lstnum1=3744&pageIndex=1&pageUnit=10&searchWrd=%ED%95%99%EC%83%9D%EA%B1%B4%EA%B0%95%EC%B2%B4%EB%A0%A5%ED%8F%89%EA%B0%80%2F&sortOrder=
- Student Health Information Center, `PAPS 학생건강체력평가 참고자료`, source `교육부`, registered 2021-09-16, https://www.schoolhealth.kr/web/search/selectTotalSearchList.do?searchWrd=paps
- National Law Information Center, `학교건강검사규칙`, effective 2025-03-10, Ministry of Education Ordinance No. 354, https://law.go.kr/LSW/lsInfoP.do?lsiSeq=269791
- National Law Information Center, `학교건강검사규칙 2025 amendment text`, https://law.go.kr/LSW/lsRvsDocListP.do?chrClsCd=010102&lsId=008594

## Confirmed Facts

- Current official regulation references attached tables for physical ability test criteria and item-level grade and score rules.
- The 2025 amendment states that the physical ability criteria table was replaced.
- The official criteria are organized by school grade rows, not by exact age.
- PAPS required evaluation uses one selected item per fitness component.
- Overall physical ability grade is calculated from five item scores, but this project intentionally does not implement an overall grade until the aggregation policy is explicitly confirmed.
- BMI is part of body composition evaluation and must remain fitness-management reference information, not medical diagnosis.

## Current Model Gap

Current `PapsStandard` fields:

- `version`
- `testItem`
- `schoolLevel`
- `gender`
- `schoolGrade`
- `minimumAge`
- `maximumAge`
- `grade`
- `minimumValue`
- `maximumValue`
- `minimumInclusive`
- `maximumInclusive`

Official PAPS criteria require a grade-level axis:

- elementary, middle, high school level
- school grade within the school level
- gender
- item
- grade range

The current model can represent `HIGH`, `schoolGrade`, and `MALE/FEMALE`. `minimumAge` and `maximumAge` remain in the table for compatibility, but official criteria lookup uses school grade instead of using age as a proxy.

## Required Before Official Seed Data

- RN must send `schoolGrade` in PAPS evaluation requests.
- Official source rows must be entered with the exact school grade from the table.
- Do not use birth date or calculated age to choose official high school grade rows.

## Data Entry Rules After Model Fix

- Create a new Flyway migration, do not edit existing migrations.
- Add a new official `PapsStandardVersion` with `sourceType=OFFICIAL`, `official=true`, `sourceName`, and `sourceUrl`.
- Deactivate `HACKATHON_V1` only after the official dataset covers the full evaluation scope intended for activation.
- Insert ranges using `test_item.code` and `standard_version.code` lookup instead of hard-coded numeric IDs.
- Keep the official inclusive/exclusive boundary semantics.
- Do not mix official and internal criteria in one version.

## Validation Required After Model Fix

- Exactly one active standard version.
- Official version metadata has source name and URL.
- Every supported item, school grade, gender, and grade combination exists.
- Each combination has grades 1 through 5.
- No overlapping intervals.
- No unintended gaps between adjacent intervals.
- Integer items do not use unnecessary decimal boundaries.
- Representative values and boundary values evaluate to the expected grade through `PapsGradeEvaluator`.
- Evaluation integration tests use Flyway seed data, not duplicated production numbers in test fixtures.

## Unsupported In This Branch

- No official seed data migration was added.
- No official standard version was activated.
- No PAPS criterion value was copied into Java code.
- No official table was converted from school grade to age.
