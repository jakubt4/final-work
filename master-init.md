ACT as a Senior Solutions Architect. Initialize a "Context-Driven Development" (CDD) workflow in the current directory for a GPU E-commerce platform.

### INSTRUCTIONS
Perform the following 3 steps sequentially.

#### STEP 1: SYSTEM BOOTSTRAP
1.  Check if the current directory is a git repository. If not, execute `git init`.
2.  Infer the Project Name from the current root directory name.
3.  Create a directory named `.claude/commands` in the project root.
4.  Create a directory named `PRPs` in the project root.

#### STEP 2: DEFINE CORE STANDARDS (The Framework)
Create the following **5 files** with the exact content specified below.

**A. `.gitignore` (in Project Root)**
> Content:
> ```gitignore
> # Maven / Gradle
> target/
> build/
> .mvn/wrapper/maven-wrapper.properties
> !.mvn/wrapper/maven-wrapper.jar
> .gradle/
> 
> # IDEs
> .idea/
> *.iml
> .vscode/
> *.classpath
> *.project
> *.settings/
> .DS_Store
> 
> # Logs & Local Config
> logs/
> *.log
> secrets.properties
> .env
> 
> # Docker / Testcontainers
> .testcontainers.properties
> 
> # OS
> Thumbs.db
> ```

**B. `INITIAL-TEMPLATE.md` (in Project Root)**
*Context: A generic, living schema for the project state.*
> Content:
> * `# System Context` (Global Goal & Scope)
> * `# Technology Standards` (Immutable constraints: Java 21, Spring Boot, Infra)
> * `# Active Iteration Scope` (The specific feature/change being implemented *right now*)
> * `# Functional Specifications` (The cumulative list of User Stories & Requirements)
> * `# Architecture & Data Model` (The evolving System Design, ERD, and Auth Strategy)
> * `# Implementation Roadmap` (Checklist: Done vs. Todo)

**C. `.claude/commands/enhance-init.md`**
> Content:
> Enhance Initial Feature Description
>
> Enhances INITIAL.md by adding concrete codebase context and validating architectural alignment.
>
> Process
> 1. **Ingest & Validate**
>    - Read `INITIAL.md` and the new feature request.
>    - **Architectural Compliance Check:** Verify the request does not violate the Modular Monolith structure or Java 21 standards.
>    - **Gap Analysis:** Identify missing data, edge cases, or security implications (e.g., AuthZ roles).
>
> 2. **Codebase Contextualization**
>    - Locate relevant existing files (Controllers, Entities, Repositories).
>    - Identify reusable patterns (e.g., "Use existing `BaseEntity`" or "Follow `OrderService` transaction pattern").
>
> 3. **Update Strategy**
>    - Update `# Active Iteration Scope`: Define the specific "Definition of Done" for this cycle.
>    - Update `# Functional Specifications`: Append the new requirements.
>    - Update `# Architecture & Data Model`: Add new tables/relationships if needed.
>    - **Concrete References:** Replace generic text with specific file paths (e.g., `src/main/java/.../UserEntity.java`).
>
> Output
> Create the new `ENHANCED-INITIAL-${feature-name}.md` with the enhanced, context-rich version.

**D. `.claude/commands/generate-prp.md`**
> Content:
> # Generate PRP
> Feature file: $ARGUMENTS
> 
> **Role:** Principal Software Architect.
> **Goal:** Create a rigorous, implementation-ready Product Requirements Document (PRP).
> 
> **Strategy: Dynamic Batching for Depth**
> To ensure maximum detail and prevent token limits from simplifying the requirements, you must partition the work.
> 
> 1. **Partitioning Logic:**
>    - Analyze total scope complexity.
>    - **Rule of Thumb:** A single PRP part should cover ~10-15 files maximum.
>    - **Determine N:** Calculate required parts ($N$) based on scope (e.g., Part 1..3).
> 
> 2. **Execution Strategy:**
>    - **File 1 (Active):** `PRPs/{feature}/{feature}-part1.md`
>      - Content: **FULL TECHNICAL DETAIL** for the first logical batch.
>      - Include: Exact file paths, library versions, full pseudocode, and validation gates.
>    
>    - **Files 2..N (Deferred):** `PRPs/{feature}/{feature}-part2.md` ... `partN.md`
>      - Content: **Structured Outline Only**.
>      - Header: `# PRP Part X: {ModuleName} (To Be Expanded)`
>      - Body: List the specific modules and high-level requirements allocated to this part.
>      - Note: Add a comment at the top: ``
> 
> **Research Process**
> 1. **Codebase Analysis:** Search for similar features, existing patterns, and test approaches.
> 2. **External Research:** Search for library documentation (URLs) and implementation examples (GitHub/StackOverflow).
> 3. **User Clarification:** Identify integration requirements.
> 
> *** CRITICAL: STOP AND THINK ***
> *** AFTER RESEARCHING BUT BEFORE WRITING: ULTRATHINK ***
> - Review your research.
> - Plan the implementation path step-by-step.
> - Verify: Does the plan fit the "Batching Strategy"?
> - Verify: Do I have the exact library versions?
> - **ONLY THEN START WRITING THE PRP.**
> 
> **PRP Content (For the Active Part)**
> - **Context:** Documentation URLs, Code Examples, Gotchas.
> - **Implementation Blueprint:**
>   - Pseudocode showing approach.
>   - Reference real files for patterns.
>   - Error handling strategy.
>   - List tasks in execution order.
> - **Validation Gates:**
>   - `./mvnw clean compile`
>   - `./mvnw clean verify` (All tests)
> 
> **Output:**
> - **Create Directory:** `PRPs/{feature}/`
> - Generate Part 1 (Full) and Part 2..N (Outlines) inside that directory.
> - Score confidence (1-10).
> 
> **Quality Checklist**
> [ ] All necessary context included
> [ ] Validation gates are executable
> [ ] References existing patterns
> [ ] Clear implementation path (or split path)
> [ ] Error handling documented

**E. `.claude/commands/execute-prp.md`**
> Content:
> # Execute PRP
> Implement a feature using the PRP file.
> PRP File: $ARGUMENTS
> 
> **Role:** Senior Implementation Engineer.
> **Constraint:** Strict Token Conservation Mode.
> 
> **Execution Process**
> 1. **Scope Analysis (CRITICAL)**
>    - Read the PRP file.
>    - **Check:** Is this a "Partial PRP" (Part X of N)?
>    - **Constraint:** If Part X, IGNORE references to future parts (Part X+1). Do NOT extend research into future phases. Focus 100% on current requirements.
> 
> 2. **Extend Research (Scoped)**
>    - Ensure you have all context *for the current scope*.
>    - Do web searches/codebase exploration *only* for the active part.
> 
> 3. **ULTRATHINK**
>    - Think hard before you execute. Create a comprehensive plan.
>    - Break down complex tasks using your todos tools.
>    - Use **TodoWrite** to track the implementation plan.
> 
> 4. **Execute the plan**
>    - Implement the code file-by-file.
>    - **Stop** exactly where the PRP ends.
> 
> 5. **Validate**
>    - Run validation commands defined in the PRP.
>    - Fix failures and retry until pass.
> 
> 6. **Complete**
>    - Ensure checklist done.
>    - Report completion status: "Part X Complete. Ready for Part X+1."
> 
> **Safety Override:**
> If the PRP contains a section "Part X Outline" or "Future Work", treat it as read-only context. Do NOT implement code for it.

#### STEP 3: GENERATE SOURCE OF TRUTH (INITIAL.md)
Using the `INITIAL-TEMPLATE.md` structure and the **Context & Directives** defined below, generate and write the **`INITIAL.md`** file to the project root.

**A. Business Context (The "Init" Feature):**
> The service functions as a comprehensive Backend API featuring four distinct modules: **Users**, **Authentication**, **Products**, and **Orders**.
>
> * **Users Module:** Manages user identity with strict validation (Name max 100, Email max 100 & Unique, Password string). Must expose CRUD REST APIs validating input DTOs (Return 400 on failure).
> * **Authentication Module:** Provides a Login REST API that checks credentials and returns a **JWT Bearer Token**. This token is mandatory for protecting all other endpoints (except registration/login).
> * **Products Module:** Manages inventory items (GPUs) with attributes: Name (max 100), Description, Price (must be >= 0), Stock (must be >= 0), and Created_At timestamp.
> * **Orders Module:** Handles the transaction lifecycle. An Order consists of: ID, User_ID, Total (>= 0), Status Enum (**pending, processing, completed, expired**), timestamps, and a list of items (Product_ID, Quantity > 0, Price > 0).
>
> The system must conform to **OpenAPI/Swagger** documentation standards and include a robust **Database Upgrade Mechanism** (e.g., Flyway or Liquibase) that handles schema creation and initial seed data.

**B. Architectural Directive (Global Constraints):**
> Adopt the mindset of a Senior Software Architect to engineer a modern Spring Boot ecosystem. The implementation must strictly leverage **Java 21 features** (Virtual Threads, Records for DTOs) within a Maven-wrapped environment (`mvnw`).
>
> **Architecture:** Follow **Domain-Driven Design** principles organized as a **Microservice-Ready Modular Monolith**.
>
> **Persistence & Data:**
> * Configure **PostgreSQL** via Docker Compose.
> * Implement a formal **DB Upgrade Mechanism** (do not use raw `schema.sql`). The upgrade tool must also handle **Initial Seed Data** (populating the database with at least 10 products/GPUs).
> * **Concurrency:** Secure DB transactions to prevent overselling (locking inventory during Order creation).
>
> **API Standards & Error Handling:**
> * All endpoints must be protected via JWT (except public auth).
> * Strictly implement the following HTTP Error States:
>     * **400:** Bad Request (Validation failure).
>     * **401:** Unauthorized (Invalid/Missing Token).
>     * **404:** Not Found (Resource missing).
>     * **500:** Internal Server Error.
> * Include **OpenAPI/Swagger** documentation for all endpoints.
>
> **Testing:**
> * Implement **Integration Tests** using **Testcontainers** (Singleton pattern).
> * Requirement: Minimum **5 distinct test cases** covering the critical flows (e.g., Auth success, Order creation, Stock validation).

**Requirements for INITIAL.md:**
* **Active Iteration Scope:** Set to "Project Initialization, Core Modules (User/Auth/Product/Order), and DB Migration Setup."
* **Tech Stack:** Java 21, Spring Boot, Maven, PostgreSQL, Testcontainers, Flyway/Liquibase, Swagger/OpenAPI.
* **Architecture:** Define the schemas for Users, Products, and Orders exactly as specified (including length constraints and Enums).
* **Data Model:** Explicitly list the fields and validation rules (e.g., `price >= 0`, `email unique`).
* **Documentation:** Add a requirement to update `README.md` with instructions on how to run the DB upgrade tool and start the service.