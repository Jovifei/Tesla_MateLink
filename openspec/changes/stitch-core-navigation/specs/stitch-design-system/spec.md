## ADDED Requirements

### Requirement: Design token source of truth
The system SHALL define all design tokens (colors, typography, spacing, shapes) in a single shared JSON file at `app_glm/shared/design-tokens.json` that both Android and iOS consume.

#### Scenario: Android reads design tokens
- **WHEN** Android app compiles
- **THEN** Theme.kt uses color values matching design-tokens.json exactly

#### Scenario: iOS reads design tokens
- **WHEN** iOS app launches
- **THEN** AppTheme.swift uses color values matching design-tokens.json exactly

### Requirement: Color palette
The system SHALL use the Stitch white-minimal palette: background `#fdf8f8`, surface `#fdf8f8`, on-surface `#1c1b1b`, primary `#000000`, secondary `#895200`, outline `#747878`, outline-variant `#c4c7c7`.

#### Scenario: Card background
- **WHEN** any card component renders
- **THEN** its background is `#fdf8f8` (surface) with 1px `#c4c7c7` border

### Requirement: Typography scale
The system SHALL use Inter font for all UI text and JetBrains Mono for all numerical data displays. Font sizes: display-lg=32px(700), headline-md=24px(600), body-lg=16px(400), body-sm=14px(400), data-lg=24px(500), data-md=16px(500), label-caps=12px(700/0.05em).

#### Scenario: Numerical value display
- **WHEN** battery percentage "78%" renders
- **THEN** it uses JetBrains Mono, 24px, weight 500, tabular-nums

#### Scenario: Section headline
- **WHEN** a section title like "Trip History" renders
- **THEN** it uses Inter, 24px, weight 600, letter-spacing -0.01em

### Requirement: Card component
The system SHALL render cards with: white background, 1px solid `#E5E5E5` border, 8px corner radius, 24px internal padding, no shadow.

#### Scenario: Dashboard card
- **WHEN** battery status card renders
- **THEN** it has 1px #E5E5E5 border, 8px radius, 24px padding, no elevation shadow

### Requirement: No shadows anywhere
The system SHALL NOT use any elevation shadows or blur effects. Depth is conveyed through border weight only.

#### Scenario: Any UI element
- **WHEN** any component renders
- **THEN** elevation/shadow is zero; borders are the only depth indicator
