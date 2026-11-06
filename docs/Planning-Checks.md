# Planning checks - recorded validation evidence

Docs-phase validation record. This is a log of commands actually executed
and their results - NOT a scoring rubric or test harness. All
build/game/runtime statuses remain `NOT RUN` everywhere.

## Environment

- Date: 2026-11-02. Shell: Git Bash (MINGW64) on Windows, git
  2.51.0.windows.1, python (system) for checks. No servers started.
- Repo: `C:\Users\Vlad\Documents\Github\create-contour`.

## Check 1 - git state (local-only, no commits/remotes)

Command:

```text
git status --short --branch; git remote -v; git log --oneline
```

Actual output:

```text
## No commits yet on main
?? .github/
?? .gitignore
?? AGENTS.md
?? CHANGELOG.md
?? LICENSE
?? README.md
?? docs/
(remote -v: empty output - no remotes)
fatal: your current branch 'main' does not have any commits yet
```

Result: PASS (expected state - commits not authorized; all files
untracked by design).

## Check 2 - ASCII / links / mandatory issue sections / DAG / credit consistency

Command: inline Python (not persisted) walking all tracked-path files:

- byte-level ASCII check and trailing-whitespace check;
- relative Markdown link resolution;
- mandatory issue sections present
  (`## Intent`, `## Expectation`, `## Acceptance criteria`,
  `## Context the code cannot infer`, `## Scope`, `Non-goals`,
  `## Dependencies`, `## Verification`, `## PR contract`);
- issue dependency DAG cycle check against the declared set;
- stale-phrase sweep (old economy wording, prior "supported-profiles"
  phrasing, prior assertions that the pack "runs" specific versions,
  prior dev-POM "optional" phrasing, prior competitor-comparison
  phrasing, etc.);
- Testing.md matrix rows all carry `NOT RUN`.

Actual results (run after the consolidated correction pass):

```text
files=27
issue-sections: all present
DAG: acyclic
BROKEN README.md -> docs/Planning-Checks.md   (resolved: this file now exists)
```

Two heuristic hits were manually inspected and dismissed:

- `02-legal-asset-provenance.md` matched a competitor-verb pattern inside
  the sentence "This scan MUST NOT open, read, hash, decompile, or diff
  any competitor source or asset" - a prohibition, not an instruction.
- `docs/Milestones.md` matched a substring in a line about full-pack
  test executions (not a version assertion); the line has since been
  reworded to "full-pack executions expose" to remove the ambiguity.

### Final re-run (recorded verbatim)

Executed 2026-11-02 in Git Bash from the repo root. Command:

```text
cd "C:/Users/Vlad/Documents/Github/create-contour" && python - <<'EOF'
import os, re
mds=[]
for dp,dn,fn in os.walk("."):
    if ".git" in dp.split(os.sep): continue
    for f in fn: mds.append(os.path.join(dp,f))
errs=[]
stale=["legitimate material accounting","fidelity for free","supported profile","Optional entries","OD-3 (package","competitor scan","Two supported","owner-settled","pack runs","paid-unit count","register and inventory committed"]
for p in mds:
    b=open(p,"rb").read()
    if any(c>127 for c in b): errs.append(f"NON-ASCII {p}")
    txt=b.decode("ascii")
    for n,l in enumerate(txt.splitlines(),1):
        if l!=l.rstrip(): errs.append(f"TRAILWS {p}:{n}")
    if os.path.basename(p) != "Planning-Checks.md":
        for s in stale:
            if s in txt: errs.append(f"STALE '{s}' in {p}")
    for m in re.finditer(r'\]\(([^)\s]+)\)', txt):
        t=m.group(1).split('#')[0]
        if not t or '://' in t: continue
        if not os.path.exists(os.path.normpath(os.path.join(os.path.dirname(p),t.replace('/',os.sep)))):
            errs.append(f"BROKEN {p} -> {m.group(1)}")
req=["## Intent","## Expectation","## Acceptance criteria","## Context the code cannot infer","## Scope","Non-goals","## Dependencies","## Verification","## PR contract"]
for f in sorted(os.listdir("docs/issues")):
    if not re.match(r'\d+-',f): continue
    txt=open(os.path.join("docs/issues",f),encoding="ascii").read()
    miss=[r for r in req if r not in txt]
    if miss: errs.append(f"{f} MISSING {miss}")
intended={'01':set(),'02':{'01'},'03':{'01','02'},'04':{'03'},'05':{'04'},'06':{'05'},
          '07':{'04','05','06'},'08':{'07'},'13':{'01','07','08'},'09':{'13'},
          '10':{'09'},'11':{'07'},'12':{'10'}}
state={}
def visit(n,st):
    state[n]=1
    for d in intended[n]:
        if state.get(d)==1: errs.append(f"CYCLE {st+[n,d]}")
        elif state.get(d)!=2: visit(d,st+[n])
    state[n]=2
for n in intended:
    if state.get(n)!=2: visit(n,[])
print(f"files={len(mds)}")
print("DAG: acyclic" if not any('CYCLE' in e for e in errs) else "CYCLE FOUND")
print("RESULT:", errs if errs else "ALL CHECKS CLEAN")
EOF
echo "=== GIT ==="; git status --short --branch; git remote -v; echo "remotes:$(git remote | wc -l)"; git log --oneline 2>&1 | head -2
```

Raw stdout:

```text
files=28
DAG: acyclic
RESULT: ALL CHECKS CLEAN
=== GIT ===
## No commits yet on main
?? .github/
?? .gitignore
?? AGENTS.md
?? CHANGELOG.md
?? LICENSE
?? README.md
?? docs/
remotes:0
fatal: your current branch 'main' does not have any commits yet
```

Note: the stale-phrase sweep skips `Planning-Checks.md` itself because
this log legitimately quotes the swept phrases verbatim; all other
checks (ASCII, links, sections, DAG, git) still cover it.

## Check 3 - declared dependency DAG

Declared direct dependencies (from each issue's "Depends on:" line and
the index): 01:-, 02:01(partial), 03:01+02, 04:03, 05:04, 06:05,
07:04+05+06, 08:07, 13:01+07+08, 09:13, 10:09, 11:07, 12:10.
Result: acyclic; gate direction 07/08 -> 13 -> 09 -> 10 confirmed; issue
11 depends only on 07 and is not gate-blocked.

## Check 4 - statuses

Every runtime/build/game status in `docs/Testing.md` and issue files is
`NOT RUN`; the only claimed PASS/FAIL results are for the docs-phase
checks recorded here. No uniqueness, legal-clearance, or compatibility
promises are made anywhere (name check is bounded; pack profile is a
declared target, not a proven runtime).
