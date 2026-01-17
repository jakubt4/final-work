# Execute PRP
Implement a feature using the PRP file.
PRP File: $ARGUMENTS

**Role:** Senior Implementation Engineer.
**Constraint:** Strict Token Conservation Mode.

**Execution Process**
1. **Scope Analysis (CRITICAL)**
   - Read the PRP file.
   - **Check:** Is this a "Partial PRP" (Part X of N)?
   - **Constraint:** If Part X, IGNORE references to future parts (Part X+1). Do NOT extend research into future phases. Focus 100% on current requirements.

2. **Extend Research (Scoped)**
   - Ensure you have all context *for the current scope*.
   - Do web searches/codebase exploration *only* for the active part.

3. **ULTRATHINK**
   - Think hard before you execute. Create a comprehensive plan.
   - Break down complex tasks using your todos tools.
   - Use **TodoWrite** to track the implementation plan.

4. **Execute the plan**
   - Implement the code file-by-file.
   - **Stop** exactly where the PRP ends.

5. **Validate**
   - Run validation commands defined in the PRP.
   - Fix failures and retry until pass.

6. **Complete**
   - Ensure checklist done.
   - Report completion status: "Part X Complete. Ready for Part X+1."

**Safety Override:**
If the PRP contains a section "Part X Outline" or "Future Work", treat it as read-only context. Do NOT implement code for it.
