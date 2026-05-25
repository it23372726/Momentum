# Momentum

Momentum is a behavioral financial awareness system for irregular-income tech professionals.

## Phase 2: Authentication Foundation

This phase sets up Firebase Authentication (email/password), a strict MVVM architecture, and a minimal Compose UI flow for splash, login, register, and dashboard placeholder screens.

## Phase 3: Financial Data Foundation

This phase adds Firestore-ready domain models, repository/data source architecture, and ViewModel skeletons for income, expenses, and goals to support future analytics and insights.

## Phase 4: Financial Pulse Dashboard

This phase introduces the dashboard UI backed by real Firestore data streams, including financial pulse, goal momentum, spending visibility, and recent activity sections.

## Phase 5: Low-Friction Expense Capture

This phase adds the add-expense flow with low-friction UI components, Firestore persistence, validation, and navigation back to the dashboard.

## Phase 6: Multi-Source Income Capture

This phase adds the income capture flow with currency and source selection, optional exchange rate handling, Firestore persistence, and navigation back to the dashboard.

## Phase 7: Goal Momentum Engine

This phase adds goal creation, momentum calculations, progress visualization, and behavioral feedback tied to real income and expense data.

## Phase 11: Multi-Goal System + Goal Contribution Engine

Summary

Phase 11 upgrades the goal subsystem to support multiple goals, goal contributions, realtime progress updates, and a contribution history. The implementation follows the existing MVVM + Repository + StateFlow architecture and keeps calculations inside ViewModels and repositories. No AI or predictive forecasting is implemented in this phase.

Multi-goal architecture

- Goals are modeled as `SavingsGoal` domain objects and the repository returns a list of goals for the current user. Goals may be active, inactive, or archived (the `active` flag marks the user's primary/active goal).
- Contributions are nested under each goal in Firestore: `users/{uid}/goals/{goalId}/contributions`.

Goal contribution system

- New domain model: `GoalContribution` with fields id, goalId, amount, contributionSource, date, note, createdAt.
- Contribution sources supported: MANUAL, SALARY, FREELANCE, CRYPTO, ROUNDUP, SAVINGS.
- Contributions are written transactionally alongside an atomic increment of the goal's `currentAmount` to ensure safe totals.

Updated Firestore schema

- users/{uid}/goals/{goalId}  (SavingsGoal documents)
- users/{uid}/goals/{goalId}/contributions/{contributionId}  (GoalContribution documents)

Contribution flow

1. Dashboard -> Goal Details -> Add Contribution
2. User enters amount, source, optional note, date
3. Contribution saved to `contributions` subcollection and goal `currentAmount` is incremented in a transaction
4. ViewModels and dashboard listeners react to the change and update UI in realtime

Realtime synchronization behavior

- Goal lists and contribution lists use Firestore snapshot listeners exposed via Flow to provide realtime updates.
- Adding a contribution executes a transaction that writes the contribution and updates the parent goal's balance. Listeners reflect changes immediately.

New screens/components

- `AddContributionScreen`, `AddContributionRoute`
- `ContributionUiState`, `ContributionEvent`, `ContributionViewModel`
- UI components: `GoalContributionCard`, `ContributionHistoryList`, `AddContributionButton`, `GoalProgressSummary`, `ContributionSourceSelector`, `ContributionSuccessSnackbar`

Architecture summary

- Follows MVVM, uses StateFlow and Repository pattern
- Hilt for DI, Kotlin coroutines for async
- Calculations remain inside ViewModels/repositories
- Composables are stateless when possible

## Phase 12: Goal Contribution UX + UI Integration

Summary

Phase 12 transforms the Phase 11 contribution backend into a polished, integrated feature that feels native to Momentum. The focus is on user experience, visual hierarchy, and seamless navigation.

UI Polish & Components

- **AddContributionScreen** — Modern modal form with TopAppBar, outline text fields, snackbar feedback, and loading states
- **ContributionSourceSelector** — FilterChip selection UI with 6 sources across 2 rows (MANUAL, SALARY, FREELANCE, CRYPTO, ROUNDUP, SAVINGS)
- **GoalContributionCard** — Enhanced card showing amount (+green), source badge, formatted date, optional note, with proper visual hierarchy
- **ContributionHistoryList** — LazyColumn with empty state messaging ("No contributions yet. Start building momentum...")
- **GoalProgressSummary** — Visual progress bar showing current/target, percentage, and remaining amount
- **AddContributionButton** — Icon button with "Add Contribution" label (added to Goal Details screen)
- **ContributionEmptyState** — Minimal empty state component for zero contributions

Goal Details Screen Redesign

- Added "Add Contribution" button between goal progress and momentum metrics
- Visual hierarchy improved with spacing and typography
- Ready to display contribution history (architectural foundation in place)
- Seamlessly integrates with existing momentum cards

Navigation Integration

- Added `AuthRoutes.AddContribution` with `goalId` parameter to `AuthRoutes.kt`
- Added `addContribution(goalId)` function to generate navigation paths
- Integrated AddContributionRoute into NavHost in `AuthNavGraph.kt`
- Updated `GoalDetailsRoute` to accept `onAddContribution` callback
- Flow: Dashboard → Goal Details → Add Contribution (bottom sheet) → Save → Dashboard

Material 3 Theme Consistency

- Uses existing app color scheme (primary, surfaceVariant, outline)
- Follows Material 3 typography (headlineSmall, titleMedium, bodySmall, labelSmall)
- Maintains consistent spacing (8dp, 16dp, 24dp)
- Reuses existing card, button, and chip components
- Matches visual language of Income/Expense/Goal screens

Animations & Polish

- LinearProgressIndicator for progress visualization
- animateContentSize for form expansion on validation error
- Smooth loading state feedback with disabled inputs
- Snackbar notifications for success/error messages
- Visual feedback on FilterChip selection

Build Status

✅ **Project compiles successfully** — Full APK assembly verified

Next Steps Available

- Integrate contribution history display in Goal Details (architectural foundation ready)
- Add dashboard widget showing latest contribution and contribution count
- Add milestone celebrations (visual animations when goals near completion)
- Implement contribution streak tracking
- Add advanced analytics on contribution sources and patterns

## Phase 11/12 Bug Fix — Goal Contribution Crash + Goal UI Synchronization

Summary

This phase fixed navigation and UI synchronization issues in Goal Contribution feature:

Navigation Fix

- **Root Cause:** AddContributionRoute was registered in outer NavHost but navigation used childNavController
- **Solution:** Changed navigation to use parent navController (consistent with AddGoal/AddExpense pattern)
- **Result:** Navigation now works from GoalDetails → Add Contribution without crashes

Debugging Enhancements

Added comprehensive debug logging throughout the flow:

- `CONTRIBUTION_DEBUG` logs in AddContributionRoute, ContributionViewModel, AddContributionScreen
- `NAVIGATION_DEBUG` capability for tracking route transitions
- `GOAL_DEBUG` hooks for goal loading state

Logs track:
- Route opening with goalId value
- ViewModel creation and injection
- Event handling (AmountChanged, SaveClicked, etc.)
- Repository calls and success/failure
- StateFlow updates and message handling

UI Synchronization

**Goal Details Screen Redesigned**

- Updated header to match Dashboard styling (primary color, bold typography)
- Wrapped Add Contribution in MomentumCard for visual consistency
- Added descriptive text: "Boost your momentum by contributing right now"
- Maintains existing momentum/forecast cards below

**Component Design**

- All components use Material 3 colorScheme (primary, surfaceVariant, onSurface)
- Consistent spacing (16dp, 24dp) with rest of app
- Typography hierarchy matches Dashboard (headlineMedium, bodySmall, titleMedium)
- Added GoalEmptyState component with descriptive messaging

**Empty States**

- GoalEmptyState: "No goals created" + Create button
- ContributionEmptyState: "No contributions yet" + motivational text

All changes are fully backward compatible with Phase 11 backend code.

Build Status

✅ **Project compiles successfully** after fixes
✅ **Navigation bug fixed** — Add Contribution flow works end-to-end
✅ **UI synchronized** — Goal screens match app design system

## Phase 14: Analytics Completion + Insight Experience

Summary

Phase 14 transforms the analytics foundation into a complete behavioral insight system, providing users with a visual and actionable understanding of their financial behavior, spending patterns, and goal progress.

Analytics Architecture

- **MVVM + StateFlow**: UI reacts to a single `AnalyticsUiState` emitted by the `AnalyticsViewModel`.
- **Repository Pattern**: Data is aggregated from `IncomeRepository`, `ExpenseRepository`, `GoalRepository`, and `CategoryRepository`.
- **Hilt DI**: ViewModel and Repositories are injected using Dagger Hilt.
- **Calculations**: All financial calculations, percentages, and insight generation logic reside within the `AnalyticsViewModel`.

Behavioral Insight System

- **Insight Engine**: Generates real-time feedback based on spending habits, savings rates, and goal alignment.
- **Insight Types**: `POSITIVE` (motivating), `NEGATIVE` (corrective), and `NEUTRAL` (informational).
- **Actionable Feedback**: Insights like "Lifestyle Inflation" or "High Momentum" help users adjust their behavior immediately.

New Components & Charts

- **AnalyticsSummaryCard**: High-level overview of monthly momentum, goal allocations, and savings rate with trend indicators.
- **SpendingBreakdownCard**: Donut chart visualization of spending by category with percentage distribution and discretionary spending ratio.
- **IncomeDistributionCard**: Bar chart (LinearProgress) visualization of income sources and their contribution to total earnings.
- **TrendChartCard**: Bar chart showing Income vs Expenses across Daily (Weekly), Monthly, and Yearly timeframes.
- **AwarenessScoreCard**: Circular indicator showing the "Financial Awareness Score" based on logging consistency and savings rate.
- **GoalAnalyticsCard**: Detailed velocity and projected completion tracking for active goals.
- **EmptyAnalyticsState**: Motivational empty state for users with no data.

Analytics Calculations

- **Savings Rate**: `(Total Income - Regular Expenses) / Total Income`.
- **Awareness Score**: Calculated using logging frequency (last 7 days), monthly savings rate, and goal activity.
- **Momentum Label**: Dynamic status (`HIGH`, `STABLE`, `LOW`) based on savings rate thresholds.
- **Timeframe Aggregation**: Flexible data grouping for daily, monthly, and yearly trend analysis.

Visual Language

- **Material 3**: Full integration with M3 components and typography.
- **Color Coding**: `MintPrimary` for positive trends/income, `ErrorCoral` for expenses/risks.
- **Clean UI**: Minimalist chart designs focused on readability and behavioral impact.

Build Status

✅ **Project compiles successfully**
✅ **Analytics dashboard integrated**
✅ **Insight engine functional**
✅ **Goal allocation analytics working**

## Requirements

- Android Studio (compatible with AGP 8.9.1)
- Android SDK Platform 36 installed
- Firebase project with Email/Password authentication enabled
- `google-services.json` placed in `app/`

## Run

```sh
./gradlew :app:assembleDebug
```

If you want to run tests:

```sh
./gradlew :app:testDebugUnitTest
```
