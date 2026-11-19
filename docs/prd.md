# Product Requirements Document: StudyBuddy

**Team:** `Group 4 — Linus Schaub, Victor Borba, Braeden Migay, Chase Miranda`  
**Repo:** `https://github.com/orgs/438-p03-g4/repositories`  
**Last updated:** `11/02/2026`

## 1. Users

**Primary user:** A college student studying for a specific course who struggles to stay focused and find classmates to study with. They want to organize personal study time, track completed sessions, and join course-specific study groups.
**Alternative user:** An instructor can create study groups for their courses for students to access community notes and resources.

## 2. Problem

Students struggle to maintain focused study routines and find classmates studying the same course. Separate timers and general group chats leave study progress and course-specific group discovery disconnected.

## 3. Success measures

All five test participants must complete each task without assistance. Tests use a one-minute study session and an existing group for a specified course.

| ID | Measure | Target | How we check it |
|---|---|---|---|
| S1 | Start studying | Start a timer within 1 minute after signing in. | Time each participant. |
| S2 | Control and track a session | Pause and resume the timer, complete the session, and find its saved duration within 30 seconds of completion. | Observe the timer and compare the history entry with the selected duration. |
| S3 | Find classmates | Find and join a group for a specified course within 2 minutes after signing in. | Time the task and verify membership under “My Groups.” |

## 4. Scope: MVP, vertical slice, and not building

**MVP stories:** PRD-1–PRD-8 below. Timer and study-group features have equal product priority. All MVP features are available on web and Android.

**First vertical slice — PRD-7:** Find an existing study group by course and join it. Membership must persist and appear under “My Groups.” This flow will be implemented end to end on both platforms and tested with Playwright on web.

**Not building this term:** Chat or video calls; shared live timers; calendar integration or automatic scheduling; blocking other apps or websites; timer continuity after closing the app.

## 5. User stories and acceptance criteria

| ID | User story | Acceptance criteria |
|---|---|---|
| PRD-1 | As a student, I want to sign in with Google or GitHub so that I can access my account. | **Given** I am signed out, **when** I successfully authenticate with either supported provider, **then** I reach my signed-in home screen. Test separately with Google and GitHub. |
| PRD-2 | As a student, I want to edit my display name so that my profile identifies me. | **Given** I am signed in, **when** I save a new display name, **then** my profile shows it after reopening the app. |
| PRD-3 | As a student, I want to delete my account so that my data is removed. | **Given** I am signed in, **when** I confirm account deletion, **then** I am signed out and my profile, sessions, memberships, and groups I created are deleted; other users’ accounts and sessions remain. |
| PRD-4 | As a student, I want to choose a study duration and start, pause, resume, or cancel my timer so that I control my study time. | **Given** I select a duration, including 1 minute, **when** I start, pause, and resume, **then** the countdown runs, freezes while paused, and continues from the remaining time. **Given** an unfinished session, **when** I cancel it, **then** it stops without a completed history entry. |
| PRD-5 | As a student, I want completed sessions saved so that I can review my progress. | **Given** my timer reaches zero, **when** I open my history, **then** exactly one entry shows the completion date and selected study duration, excluding paused time, and remains after reopening the app. Only my sessions appear. |
| PRD-6 | As a student, I want to create a course-specific group so that classmates can find it. | **Given** I am signed in, **when** I submit a group name and course name, **then** the group appears in course search and I am its first member. Missing names prevent creation and show a validation message. |
| PRD-7 | As a student, I want to find and join a group for my course so that I can connect with classmates. | **Given** a matching group exists and I am not a member, **when** I search its course and select “Join,” **then** it appears under “My Groups” after reopening the app, and I cannot join twice. A search without matches shows “No groups found.” |
| PRD-8 | As a student, I want to leave a group so that I control my memberships. | **Given** I belong to a group, **when** I select “Leave,” **then** it disappears from “My Groups” and remains absent after reopening the app. Other members retain their memberships. |

### Change log

| Date | PR | What changed and why |
|---|---|---|
| 2026-11-02 | — | Initial draft defining the agreed MVP and first vertical slice. |
