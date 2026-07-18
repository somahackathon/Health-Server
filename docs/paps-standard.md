# PAPS Standard Data

## Status

Official PAPS seed data is not added yet.

The current official source uses school grade rows such as high school grade 1, grade 2, and grade 3. The current `PapsStandard` entity stores only `minimumAge` and `maximumAge`. Because school grade and age are not equivalent, the official table cannot be safely converted into the current schema without changing the model.

Do not insert official PAPS grade ranges by mapping `HIGH 1/2/3` to ages such as 15/16/17. That would distort the official classification axis.

## Checked Official Sources

- Ministry of Education, `학생건강체력평가제(PAPS)측정 매뉴얼(2009 수정본)`, published 2009-04-27, https://www.moe.go.kr/boardCnts/viewRenew.do?boardID=316&boardSeq=14508&lev=0&m=0302&opType=N&page=1&s=moe&searchType=null&statusYN=C
- Student Health Information Center, `학생건강체력평가(PAPS) 운영 매뉴얼`, source `제주특별자치도교육청`, production date 2020-04-01, registered 2022-02-28, https://www.schoolhealth.kr/web/search/selectTotalSearchList.do?bbsId=&bbsTyCode=&kwdLogYn=N&lstnum1=3744&pageIndex=1&pageUnit=10&searchWrd=%ED%95%99%EC%83%9D%EA%B1%B4%EA%B0%95%EC%B2%B4%EB%A0%A5%ED%8F%89%EA%B0%80%2F&sortOrder=
- Student Health Information Center, `PAPS (학생건강체력평가) 도움자료`, source `교육부`, registered 2021-09-16, https://www.schoolhealth.kr/web/search/selectTotalSearchList.do?searchWrd=paps
- National Law Information Center, `학교건강검사규칙`, effective 2025-03-10, Ministry of Education Ordinance No. 354, https://law.go.kr/LSW/lsInfoP.do?lsiSeq=269791
- National Law Information Center, `학교건강검사규칙` 2025 amendment text, https://law.go.kr/LSW/lsRvsDocListP.do?chrClsCd=010102&lsId=008594

## Confirmed Facts

- Current official regulation references `별표 4` for physical ability test criteria and `별표 5` for item-level grade and score rules.
- The 2025 amendment states that `별표 4` was replaced.
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

The current model can represent `HIGH` and `MALE/FEMALE`, but it cannot represent `고1`, `고2`, and `고3` without using age as a proxy. That proxy is not acceptable.

## Required Model Change Before Seed Data

Add an explicit grade axis before importing official data. A minimal option is:

- Add `schoolGrade` to `PapsStandard`.
- Add `schoolGrade` to the evaluation request or define a product contract that maps RN profile data to a school grade without using age alone.
- Update repository lookup to use `schoolLevel`, `schoolGrade`, `gender`, and `testItem`.
- Keep `minimumAge` and `maximumAge` only if another confirmed standard source actually uses age.

This change should be handled before adding official seed data.

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
