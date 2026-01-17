# Generate PRP
Feature file: $ARGUMENTS

**Role:** Principal Software Architect.
**Goal:** Create a rigorous, implementation-ready Product Requirements Document (PRP).

**Strategy: Dynamic Batching for Depth**
To ensure maximum detail and prevent token limits from simplifying the requirements, you must partition the work.

1. **Partitioning Logic:**
   - Analyze total scope complexity.
   - **Rule of Thumb:** A single PRP part should cover ~10-15 files maximum.
   - **Determine N:** Calculate required parts ($N$) based on scope (e.g., Part 1..3).

2. **Execution Strategy:**
   - **File 1 (Active):** `PRPs/{feature}/{feature}-part1.md`
     - Content: **FULL TECHNICAL DETAIL** for the first logical batch.
     - Include: Exact file paths, library versions, full pseudocode, and validation gates.

   - **Files 2..N (Deferred):** `PRPs/{feature}/{feature}-part2.md` ... `partN.md`
     - Content: **Structured Outline Only**.
     - Header: `# PRP Part X: {ModuleName} (To Be Expanded)`
     - Body: List the specific modules and high-level requirements allocated to this part.
     - Note: Add a comment at the top: ``

**Research Process**
1. **Codebase Analysis:** Search for similar features, existing patterns, and test approaches.
2. **External Research:** Search for library documentation (URLs) and implementation examples (GitHub/StackOverflow).
3. **User Clarification:** Identify integration requirements.

*** CRITICAL: STOP AND THINK ***
*** AFTER RESEARCHING BUT BEFORE WRITING: ULTRATHINK ***
- Review your research.
- Plan the implementation path step-by-step.
- Verify: Does the plan fit the "Batching Strategy"?
- Verify: Do I have the exact library versions?
- **ONLY THEN START WRITING THE PRP.**

**PRP Content (For the Active Part)**
- **Context:** Documentation URLs, Code Examples, Gotchas.
- **Implementation Blueprint:**
  - Pseudocode showing approach.
  - Reference real files for patterns.
  - Error handling strategy.
  - List tasks in execution order.
- **Validation Gates:**
  - `./mvnw clean compile`
  - `./mvnw clean verify` (All tests)

**Output:**
- **Create Directory:** `PRPs/{feature}/`
- Generate Part 1 (Full) and Part 2..N (Outlines) inside that directory.
- Score confidence (1-10).

**Quality Checklist**
[ ] All necessary context included
[ ] Validation gates are executable
[ ] References existing patterns
[ ] Clear implementation path (or split path)
[ ] Error handling documented
