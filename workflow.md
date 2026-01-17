# AI Workflow Dokumentácia

**Meno:** Jakub Toth

**Dátum začiatku:** 16.01.2026

**Dátum dokončenia:** 

**Zadanie:** Backend

---

## 1. Použité AI Nástroje

Vyplň približný čas strávený s každým nástrojom:

- [ ] **Cursor IDE:** _____ hodín
- [x] **Claude Code:** 5m +  hodín  
- [ ] **GitHub Copilot:** _____ hodín
- [ ] **ChatGPT:** _____ hodín
- [ ] **Claude.ai:** _____ hodín
- [x] **Iné:** **gemini pro** 5m hodín

**Celkový čas vývoja (priližne):** _____ hodín

---

## 2. Zbierka Promptov

> 💡 **Tip:** Kopíruj presný text promptu! Priebežne dopĺňaj po každej feature.

### Prompt #1: Rozsirenie kontextu o presne zadanie

**Nástroj:** [ Gemini Pro ]  
**Kontext:** [ Rozsirenie kontextu o presne zadanie]

**Prompt:**
```
act like Senior Solutions Architect and carefully read attached image with requirements and merge it to #### STEP 3: GENERATE SOURCE OF TRUTH (INITIAL.md) in master-init.md - give me whole md file with changes
```
+ attached screenshot zo zadania prva cast + mnou vytvoreny master-init.md context
+ mam zapametane v mojom protokole aby pouzival konstruktivnu kritiku

**Výsledok:**  
[x] ✅ Fungoval perfektne (first try)  
[ ] ⭐⭐⭐⭐ Dobré, potreboval malé úpravy  
[ ] ⭐⭐⭐ OK, potreboval viac úprav  
[ ] ⭐⭐ Slabé, musel som veľa prepísať  
[ ] ❌ Nefungoval, musel som celé prepísať

**Čo som musel upraviť / opraviť:**
```
Nic
```

**Poznámky / Learnings:**
```
-
```

### Prompt #2: Improve zakladny context (master-init)

**Nástroj:** [ Gemini Pro ]  
**Kontext:** [ Rozsirenie kontextu - vygenerovanie .gitignore a upravu generate-prp + execute-prp pre podporu rozdelenie PRPs na casti]

**Prompt:**
```
Update and merge commands for generating and executing PRP (from attached file - #### STEP 2: DEFINE CORE STANDARDS (The Framework) C and D) to consider that if it will be big file then split it to multiple PRP parts to take into account token consumption limit (make the splitting strategy to be general), use "PRPs/{feature-name}/{feature-name}-part1.md` (and part2/part3 etc outlines if needed)" for generate-prp command as output and take into consideration the new PRPs folder structure
Generate '.gitignore' based on your selection.
Give me whole master-init.md file here.
```
+ attachnuty mnou vytvoreny master-init.md context

**Výsledok:**  
[ ] ✅ Fungoval perfektne (first try)  
[x] ⭐⭐⭐⭐ Dobré, potreboval malé úpravy  
[ ] ⭐⭐⭐ OK, potreboval viac úprav  
[ ] ⭐⭐ Slabé, musel som veľa prepísať  
[ ] ❌ Nefungoval, musel som celé prepísať

**Čo som musel upraviť / opraviť:**
```
Vyhodil mi ultrathinking z commandu pre generovanie PRPs
```

**Poznámky / Learnings:**
```
-
```

### Prompt #3: Oprava kde sa vyhodilo utrathinking pre generate-prp (Prompt #2)

**Nástroj:** [ Gemini Pro ]  
**Kontext:** [ Oprava kontextu]

**Prompt:**
```
in **D. `.claude/commands/generate-prp.md`** you lost ultrathinking ..
```

**Výsledok:**  
[x] ✅ Fungoval perfektne (first try)  
[ ] ⭐⭐⭐⭐ Dobré, potreboval malé úpravy  
[ ] ⭐⭐⭐ OK, potreboval viac úprav  
[ ] ⭐⭐ Slabé, musel som veľa prepísať  
[ ] ❌ Nefungoval, musel som celé prepísať

**Čo som musel upraviť / opraviť:**
```
Nic.
```

**Poznámky / Learnings:**
```
-
```

### Prompt #4: Spusti init projektu

**Nástroj:** [ claude code ]  
**Kontext:** [ Inicializacia projektu]

**Prompt:**
```
execute master-init.md
```

**Výsledok:**  
[x] ✅ Fungoval perfektne (first try)  
[ ] ⭐⭐⭐⭐ Dobré, potreboval malé úpravy  
[ ] ⭐⭐⭐ OK, potreboval viac úprav  
[ ] ⭐⭐ Slabé, musel som veľa prepísať  
[ ] ❌ Nefungoval, musel som celé prepísať

**Čo som musel upraviť / opraviť:**
```
Nic.
```

**Poznámky / Learnings:**
```
-
```

### Prompt #5: Enhance init

**Nástroj:** [ claude code ]  
**Kontext:** [ Enhance INITIAL.md]

**Prompt:**
```
/enhance-init
```

**Výsledok:**  
[x] ✅ Fungoval perfektne (first try)  
[ ] ⭐⭐⭐⭐ Dobré, potreboval malé úpravy  
[ ] ⭐⭐⭐ OK, potreboval viac úprav  
[ ] ⭐⭐ Slabé, musel som veľa prepísať  
[ ] ❌ Nefungoval, musel som celé prepísať

**Čo som musel upraviť / opraviť:**
```
Nic.
```

**Poznámky / Learnings:**
```
Tento prvy INITIAL.md som si ulozil aby som ho nestratil kedze tam mam zaklad vsetkeho
```


### Prompt #6: Commit,push, create PR

**Nástroj:** [ claude code ]  
**Kontext:** [Commit,push, create PR]

**Prompt:**
```
commit all changes with good description (ask for approval from me), push and create PR 
```

**Výsledok:**  
[x] ✅ Fungoval perfektne (first try)  
[ ] ⭐⭐⭐⭐ Dobré, potreboval malé úpravy  
[ ] ⭐⭐⭐ OK, potreboval viac úprav  
[ ] ⭐⭐ Slabé, musel som veľa prepísať  
[ ] ❌ Nefungoval, musel som celé prepísať

**Čo som musel upraviť / opraviť:**
```
Nic.
```

**Poznámky / Learnings:**
```
-
```

### Prompt #7: Generate PRPs

**Nástroj:** [ claude code ]  
**Kontext:** [Generate PRPs]

**Prompt:**
```
/generate-prp ENHANCED-INITIAL-gpu-ecommerce-platform
```

**Výsledok:**  
[x] ✅ Fungoval perfektne (first try)  
[ ] ⭐⭐⭐⭐ Dobré, potreboval malé úpravy  
[ ] ⭐⭐⭐ OK, potreboval viac úprav  
[ ] ⭐⭐ Slabé, musel som veľa prepísať  
[ ] ❌ Nefungoval, musel som celé prepísať

**Čo som musel upraviť / opraviť:**
```
Nic.
```

**Poznámky / Learnings:**
```
-
```

### Prompt #8: Install mcp for github

**Nástroj:** [ claude code ]  
**Kontext:** [Generate PRPs]

**Prompt:**
```
install mcp for github
```

**Výsledok:**  
[x] ✅ Fungoval perfektne (first try)  
[ ] ⭐⭐⭐⭐ Dobré, potreboval malé úpravy  
[ ] ⭐⭐⭐ OK, potreboval viac úprav  
[ ] ⭐⭐ Slabé, musel som veľa prepísať  
[ ] ❌ Nefungoval, musel som celé prepísať

**Čo som musel upraviť / opraviť:**
```
Nic.
```

**Poznámky / Learnings:**
```
restartol som session a overil ci tam je cez /mcp - nebolo na prvy krat takze som to musel vyriesit cez dalsi prikaz (claude mcp add github and use the one from .mcp.json)
```

## 3. Problémy a Riešenia 

> 💡 **Tip:** Problémy sú cenné! Ukazujú ako riešiš problémy s AI.

### Problém #1: Mysliet na to ktore MCP budem vyuzivat a pripravit si vopred

**Čo sa stalo:**
```
Instaloval som si github mcp cez claudeho - to bolo fajn, ale nasledne to  mi to pri restarte ukazalo ze ziadne MCP tam nie su. Zdrzalo ma to pri vyvoji.
```

**Prečo to vzniklo:**
```
Nepridal som to do claude.
```

**Ako som to vyriešil:**
```
zavolal som tento command 'claude mcp add github and use the one from .mcp.json'
```

**Čo som sa naučil:**
```
nastudovat danu problematiku popripade sa spytat AI ako to spravit spravne - pripravit si taketo veci vopred (mysliet na to pri planovani)
```

**Screenshot / Kód:** [ - ]

---

### Problém #2: _________________________________

**Čo sa stalo:**
```
```

**Prečo:**
```
```

**Riešenie:**
```
```

**Learning:**
```
```

## 4. Kľúčové Poznatky

### 4.1 Čo fungovalo výborne

**1.** 
```
[Príklad: Claude Code pre OAuth - fungoval first try, zero problémov]
```

**2.** 
```
```

**3.** 
```
```

**[ Pridaj viac ak chceš ]**

---

### 4.2 Čo bolo náročné

**1.** 
```
[Príklad: Figma MCP spacing - často o 4-8px vedľa, musel som manuálne opravovať]
```

**2.** 
```
```

**3.** 
```
```

---

### 4.3 Best Practices ktoré som objavil

**1.** 
```
[Príklad: Vždy špecifikuj verziu knižnice v prompte - "NextAuth.js v5"]
```

**2.** 
```
```

**3.** 
```
```

**4.** 
```
```

**5.** 
```
```

---

### 4.4 Moje Top 3 Tipy Pre Ostatných

**Tip #1:**
```
[Konkrétny, actionable tip]
```

**Tip #2:**
```
```

**Tip #3:**
```
```

---

## 6. Reflexia a Závery

### 6.1 Efektivita AI nástrojov

**Ktorý nástroj bol najužitočnejší?** _________________________________

**Prečo?**
```
```

**Ktorý nástroj bol najmenej užitočný?** _________________________________

**Prečo?**
```
```

---

### 6.2 Najväčšie prekvapenie
```
[Čo ťa najviac prekvapilo pri práci s AI?]
```

---

### 6.3 Najväčšia frustrácia
```
[Čo bolo najfrustrujúcejšie?]
```

---

### 6.4 Najväčší "AHA!" moment
```
[Kedy ti došlo niečo dôležité o AI alebo o developmente?]
```

---

### 6.5 Čo by som urobil inak
```
[Keby si začínal znova, čo by si zmenil?]
```

### 6.6 Hlavný odkaz pre ostatných
```
[Keby si mal povedať jednu vec kolegom o AI development, čo by to bylo?]
```
