# Workout Journal — Feature Roadmap

## Project Context
Android app (Kotlin + Jetpack Compose, Room DB, MVVM). Dark navy/indigo theme.
Three screens: History, Session (exercise/set logging), Progress (charts).
Tools menu in top bar: Rest Timer (persistent background pill), Bench Angle (accelerometer).

**Existing data model:**
- `sessions` (id, dateEpochDay, name)
- `exercises` (id, sessionId, name, orderIndex)
- `sets` (id, exerciseId, setNumber, weightKg, reps)

---

## Phase 1 — High value, no schema changes

### Step 1: 1RM Estimator
Show an estimated one-rep max on each exercise card, calculated from the best set in the current session.

**Formula:** `weight × (1 + reps / 30)` (Epley). Only meaningful when reps < 10.

**What to build:**
- Pure Kotlin function `estimateOneRepMax(weightKg: Float, reps: Int): Float`
- Add `estimatedOneRepMax: Float?` to `ExerciseUi` — computed in `SessionViewModel` from the heaviest set with reps in 1–9 range
- Show below the exercise name in `ExerciseCard`, alongside the existing "Previous best" line
- Format: `"~107.5 kg est. 1RM"`

**Files to touch:** `SessionViewModel.kt`, `SessionScreen.kt`

---

### Step 2: PR Detection
Automatically detect when a set beats the all-time best for that exercise and badge it.

**What to build:**
- New DAO query: `getAllTimeBestForExercise(name: String): Flow<LastSetData?>` — same as existing `getLastBestSetForExercise` but without the `currentSessionId` exclusion
- Add `allTimeBest: Pair<Float, Int>?` to `ExerciseUi` in `SessionViewModel`
- In `SetRow`, compare `set.weightKg >= allTimeBest.first && set.reps >= allTimeBest.second` → show a small PR badge (star icon or "PR" chip) on that row
- Keep it subtle — a small teal accent, not intrusive

**Files to touch:** `SetDao.kt`, `WorkoutRepository.kt`, `SessionViewModel.kt`, `SessionScreen.kt`

---

### Step 3: Plate Calculator
Given a target weight, calculate which plates to load on each side of the bar.

**What to build:**
- New dialog in `ToolsMenu` (alongside Rest Timer and Bench Angle)
- Input: target weight (kg), bar weight selector (standard presets: 20 kg / 15 kg / 10 kg)
- Plate sizes: 25, 20, 15, 10, 5, 2.5, 1.25 kg — greedy algorithm per side
- Display: visual plate stack or simple text list per side
- No data persistence needed

**Files to touch:** `ToolsMenu.kt`

---

## Phase 2 — Progress screen enhancements, no schema changes

### Step 4: Volume Tracking
Add total session volume (tonnage) as a second chart on the Progress screen.

**What to build:**
- New DAO query: `getVolumePerSessionForExercise(name: String)` — `SUM(weightKg * reps)` grouped by session date
- New repository method + domain model `VolumePoint(date: LocalDate, totalVolume: Float)`
- Add a second line/bar chart to `ProgressScreen` below the existing max-weight chart
- Toggle between "Max Weight" and "Volume" views, or show both stacked

**Files to touch:** `SetDao.kt`, `WorkoutRepository.kt`, `ProgressViewModel.kt`, `ProgressScreen.kt`

---

### Step 5: Calendar Heatmap
Month-view calendar in History screen showing which days had a workout session.

**What to build:**
- No new queries needed — derive workout dates from existing `getAllSessionSummaries()`
- New composable `WorkoutCalendar` in `HistoryScreen.kt` above the session list
- 7-column grid of day cells, current month shown by default, prev/next month navigation
- Days with sessions: highlighted with gradient accent; today: outline ring; no session: dim
- Tapping a highlighted day scrolls the list to that session

**Files to touch:** `HistoryScreen.kt`, `HistoryViewModel.kt`

---

## Phase 3 — Simple schema additions (migrations required)

### Step 6: Streak Counter
Count consecutive weeks (or days) with at least one logged session.

**What to build:**
- Pure calculation from existing session dates — no schema change needed
- `fun calculateStreak(sessionDates: List<LocalDate>): Int` — count back from today week by week
- Show as a stat chip at the top of History screen: `"🔥 5-week streak"` or similar
- Edge case: if no session this week yet but last week had one, streak still counts (grace period until week ends)

**Files to touch:** `HistoryViewModel.kt`, `HistoryScreen.kt`

---

### Step 7: Session Duration
Record how long each workout session took.

**Schema change:** Add `startedAtEpochSecond: Long` and `endedAtEpochSecond: Long?` to `sessions` table (nullable, default 0 — backward compatible).

**What to build:**
- DB migration (version 1 → 2): `ALTER TABLE sessions ADD COLUMN startedAtEpochSecond INTEGER NOT NULL DEFAULT 0` (same for ended)
- Set `startedAt` when session is first opened (in `SessionViewModel.init`)
- Add "End Session" button or set `endedAt` when user navigates back from a session
- Show formatted duration on `SessionCard` in History: `"1h 23m"`

**Files to touch:** `WorkoutDatabase.kt` (migration), `SessionEntity.kt`, `SessionDao.kt`, `WorkoutRepository.kt`, `SessionViewModel.kt`, `SessionScreen.kt`, `HistoryScreen.kt`

---

### Step 8: Notes Field
Free-text notes per session and optionally per exercise.

**Schema change:** Add `notes: String` to `sessions` table (default `""`).

**What to build:**
- DB migration: `ALTER TABLE sessions ADD COLUMN notes TEXT NOT NULL DEFAULT ''`
- Collapsible notes section at the bottom of `SessionScreen` (expand with a chevron)
- `AppTextField` (multiline) — auto-saves on focus loss via `SessionViewModel.saveNotes()`
- Show a note preview icon on `SessionCard` in History if notes are non-empty

**Files to touch:** `WorkoutDatabase.kt` (migration), `SessionEntity.kt`, `WorkoutRepository.kt`, `SessionViewModel.kt`, `SessionScreen.kt`, `HistoryScreen.kt`

---

## Phase 4 — Larger feature: Workout Templates

### Step 9: Workout Templates
Save any session as a reusable template; start new sessions pre-populated from a template.

**Schema changes:** Two new tables:
```
templates (id PK, name, createdAtEpochDay)
template_exercises (id PK, templateId FK, name, orderIndex)
```

**What to build:**
- New DAOs: `TemplateDao` with insert/delete/getAll/getWithExercises
- New entities: `TemplateEntity`, `TemplateExerciseEntity`
- Repository methods for template CRUD
- "Save as template" action in `SessionScreen` top bar menu (prompts for template name)
- New `TemplatesScreen` or a modal sheet accessible from History screen showing saved templates
- "Start from template" button on the date-picker dialog when creating a new session — pre-inserts exercises into the new session
- ViewModel: `TemplatesViewModel`

**Files to touch:** New files for entities, DAOs, ViewModel, Screen. `WorkoutDatabase.kt`, `WorkoutRepository.kt`, `HistoryScreen.kt`, `HistoryViewModel.kt`, `SessionScreen.kt`, `AppNavigation.kt`

---

## Implementation Notes
- Steps 1–3 can be done in any order (fully independent)
- Steps 4–5 can be done in any order after steps 1–3
- Steps 7–8 each require a Room migration — increment `DATABASE_VERSION` and add a `Migration` object in `WorkoutDatabase.kt`
- Step 9 is the largest single change; consider doing it last
- Each step should be built, installed, and tested on device before moving to the next
