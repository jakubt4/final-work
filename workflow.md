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
**Kontext:** [MCPs]

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
restartol som session a overil ci tam je cez /mcp - nebolo na prvy krat takze som to musel vyriesit cez dalsi prikaz (claude mcp add github and use the one from .mcp.json) - bola to moja chyba, command spravil co som mu napisal
```

### Prompt #9: Execute PRP Part 1

**Nástroj:** [ claude code ]  
**Kontext:** [Generate PRPs]

**Prompt:**
```
/execute-prp gpu-ecommerce-platform-part1.md 
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

### Prompt #10: Execute PRP Part 2

**Nástroj:** [ claude code ]  
**Kontext:** [Generate PRPs]

**Prompt:**
```
/execute-prp gpu-ecommerce-platform-part2.md 
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
po tom som to commitol a pushol cez /gh-cpc a clearol context cez /clear
```

### Prompt #11: Execute PRP Part 3

**Nástroj:** [ claude code ]  
**Kontext:** [Generate PRPs]

**Prompt:**
```
/execute-prp gpu-ecommerce-platform-part3.md 
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
po tom som to commitol a pushol cez /gh-cpc a clearol cez /clear
```

### Prompt #11: Execute PRP Part 4

**Nástroj:** [ claude code ]  
**Kontext:** [Generate PRPs]

**Prompt:**
```
/execute-prp gpu-ecommerce-platform-part4.md 
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
po tom som to commitol a pushol cez /gh-cpc a clearol cez /clear
```

### Prompt #12: Missing logs and jdocs

**Nástroj:** [ claude code ]  
**Kontext:** [Generate logging and jdocs]

**Prompt:**
```
generate jdocs for all classes + logging
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
to som zabudol uplne zahrnut do planu/zakladneho kontextu + /gh-cpc
```

### Prompt #13: Compact

**Nástroj:** [ claude code ]  
**Kontext:** [Compact context]

**Prompt:**
```
/compact
```

**Výsledok:**  
[x] ✅ Fungoval perfektne (first try)  
[ ] ⭐⭐⭐⭐ Dobré, potreboval malé úpravy  
[ ] ⭐⭐⭐ OK, potreboval viac úprav  
[ ] ⭐⭐ Slabé, musel som veľa prepísať  
[ ] ❌ Nefungoval, musel som celé prepísať

**Čo som musel upraviť / opraviť:**
```
Nic. Len 4h dalsieho cakania na reset usage :)
```

**Poznámky / Learnings:**
```
command spravil co mal, ale zhltol mo strasne vela usage kedze predchadzajuci command bol dost velky a vytvoril strasne vela contexty - moja chyba.
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

### Problém #2: Compact

**Čo sa stalo:**
```
zavolal som compact za velkym contextom na velkom objeme dat
```

**Prečo:**
```
zacal mi dochadzat context
```

**Riešenie:**
```
radsej clear/exit
```

**Learning:**
```
Cesta A: "Incremental Development" (Počas vývoja)
Pridat požiadavku na Javadocs a Logovanie priamo do PRP (napr. do generate-prp.md).

Prečo: Keď Claude generuje súbor prvýkrát, pridanie Javadocu ho "nestojí" skoro nič navyše, lebo ten súbor aj tak práve píše. Je to zadarmo v rámci prvého prechodu.

Cesta B: "IDE Tools" (Zadarmo)
Na generovanie Javadocov a základných logov nepoužívat LLM (Claude Code).

V IntelliJ IDEA alebo VS Code existujú pluginy (napr. GhostDoc alebo zabudované AI asistenty ako Copilot s inline editovaním), ktoré ti vygenerujú Javadoc pre metódu jedným klikom.

```

## 4. Kľúčové Poznatky

### 4.1 Čo fungovalo výborne

**1.** 
```
Vytvorenie "Master Workflow":
Myslim ze subor master-init.md sa mi vydaril aj s pomocou Gemini PRO. Nie je to len prompt, je to cely operacny system pre vyvoj, ktory definuje strukturu, gitignore, prikazy aj sablony.
```

**2.** 
```
Architektura riesenia
Napriek limitom som nerezignoval na kvalitu. Moj PRP definuje moderny stack (Java 21, Spring Boot 3.4, Testcontainers, Flyway) a neprijal som ziadne skratky.
```

**3.** 
```
Adaptabilita
Ked som narazil na limit (10% baterky), nespanikaril som. Namiesto toho som prekopal proces (generate-prp.md a execute-prp.md) na verziu, ktora funguje bezpecne aj s nizkym rozpoctom.
```
**4.**
```
Automatizacia
Vdaka vlastnym custom prikazom (/enhance-init, /generate-prp, /execute-prp -> odkukane od https://github.com/StreetOfCode/task-managemenet-system ale vylepsne) teraz dokazem replikovat tento uspech na akomkolvek dalsom projekte v priebehu minut (teda podla usage:)).
```

---

### 4.2 Čo bolo náročné

**1.** 
```
Manazment zdrojov vs. Ambicie (vytvorenie kvalitneho contextu)
Bolo narocne vybalansovat snahu o "seniornu kvalitu" (robustna architektura, testy, dokumentacia) s tvrdymi limitmi nastroja. Musel som sa naucit strategicky davkovat ulohy a niekedy aj cakat na obnovu kvoty, namiesto toho, aby som to nechal bezat v kuse a dostal nekvalitny vysledok. 
```

**2.** 
```
Pasca s prikazom Compact
Moment, ked som minul 50% usage na generovanie dokumentacie a nasledne som to "dorazil" prikazom /compact, ktory musel cely ten obrovsky kontext nacitat znova. Bola to draha, ale cenna lekcia.
```

**3.** 
```
Tooling Friction (.mcp.json):
Zistenie, ze Claude Code (CLI) necita .mcp.json automaticky pri starte a ze nastroje musim registrovat imperativne cez claude mcp add
```

---

### 4.3 Best Practices ktoré som objavil

**1.** 
```
Token Economics & Context Hygiene
Zistil som, ze prikaz /compact nie je zadarmo – prave naopak, stoji to tokeny, lebo model musi precitat a zhrnut historiu.
Naucil som sa pouzivat /clear vzdy pred zaciatkom novej, logicky oddelenej ulohy (napr. prechod z Part 1 na Part 2), co setri usage.
```

**2.** 
```
Split Strategy (Lazy Loading)
Namiesto generovania 50 suborov naraz (co vedie k chybam a narazaniu na limity) som implementoval N-Part strategiu.
Generujem detailne len to, co idem prave teraz implementovat (Part 1), a pre zvysok si nechavam vygenerovat len "outline" (kostru), ktoru rozviniem az neskor.
```

**3.** 
```
Scope-Aware Execution
Upravil som prikazy tak, aby AI nehalucinovala o buducnosti. Pridal som pravidla typu "If Part 1, IGNORE Part 2 requirements", co drasticky setri tokeny a zvysuje presnost.
```

**4.** 
```
Planning (MCP)
Planovat aj s MCPs a najprv si ich pripravit
```

**5.** 
```
Planning (Logging & JDocs)
Naucil som sa, ze poziadavky ako "Javadocs" alebo "Logging" musim zadat hned na zaciatku (v generate-prp), a nie ich robit ako refaktoring na konci, lebo to zbytocne zdvojnasobuje spotrebu tokenov.
```

---

### 4.4 Moje Top 3 Tipy Pre Ostatných

**Tip #1:**
```
Bacha na compact vs usage (command (feature) -> github ops -> clear!; ak je to mozne)
```

**Tip #2:**
```
Tvoje prianie je mi rozkazom - bacha co si prajes (rozmyslaj nad commandmi ako aj nad vacsim kontextom)
```

**Tip #3:**
```
3x planuj, raz commanduj: rozmyslaj iterativne a rozkuskuj si to (hlavne pri greenfield projektoch).
```

---

## 6. Reflexia a Závery

### 6.1 Efektivita AI nástrojov

**Ktorý nástroj bol najužitočnejší?** master-init

**Prečo?**
```
Fungoval ako "exokostra" pre cely vyvoj. Namiesto chaotickeho chatovania mi dal pevnu strukturu. Definoval pravidla hry (tech stack, architekturu) a vdaka prikazom ako /generate-prp automaticky vynutil "Split Strategy", cim ma ochranil pred chybami z pretazenia kontextu. Bez neho by som sa stratil v detailoch a boilerplate kode.
```

**Ktorý nástroj bol najmenej užitočný?** Compact
**Prečo?**
```
Funguje paradoxne - na to, aby "zmensil" kontext, musi najprv precitat a spracovat celu historiu chatu. Kedze som bol na limite s tokenmi, tento prikaz mi namiesto pomoci "dozral" zvysok dennej kvoty, pretoze som zaplatil za spracovanie obrovskeho mnozstva textu len kvoli zhrnutiu.
```

---

### 6.2 Najväčšie prekvapenie
```
Zistenie, ako draha je "nepozornost". Ze jedna nedomyslena veta na konci (napr. "dopln javadocs vsade") dokaze v sekunde spalit polovicu dennej kvoty. Prekvapilo ma, ze pre AI neexistuje koncept "malej zmeny" - kazdy edit je v podstate prepisanie suboru, co ma naucilo vazit si kazdy token.
```

---

### 6.3 Najväčšia frustrácia
```
Cakanie ked dosiel usage..
```

---

### 6.4 Najväčší "AHA!" moment
```
Compact vs usage
```

---

### 6.5 Čo by som urobil inak
```
- Cross-cutting concerns (Javadocs, Logging) by som zahrnul priamo do 'generate-prp' promptu. Dorabat ich dodatocne znamenalo precitat a prepisat cely projekt znova, co ma stalo 50% usage
- Skorsia formalizacia workflowu. Namiesto ad-hoc pisania by som si hned na zaciatku vytvoril sadu custom prikazov v .claude/commands pre vsetky bezne operacie (nie len pre init)
- 'Environment Check' pred startom. Uistil by som sa, ze MCP servery (GitHub) a Docker (a ine) bezia a su spravne nacitane v CLI este predtym, nez miniem prvy token na generovanie kodu
```

### 6.6 Hlavný odkaz pre ostatných
```
AI nenahradza seniornych inzinierov, ale meni ich na architektov. Prestavame pisat syntax a zaciname dizajnovat kontext - kvalita vystupu je priamo umerna kvalite tvojho zadania (PRP) a discipline tvojho procesu.
```
