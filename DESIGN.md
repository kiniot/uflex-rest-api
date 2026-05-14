---
name: uFlex Design System
colors:
  primary: "#074D61"
  secondary: "#48CBB6"
  surface: "#F7F5ED"
  on-surface: "#1A1C1E"
  error: "#EF5350"
  info: "#00B4D8"
  success: "#3BC48E"
  warning: "#FFAD33"
typography:
  headings:
    fontFamily: Plus Jakarta Sans
    fontWeight: 600
  body:
    fontFamily: Inter
    fontSize: 16px
    fontWeight: 400
rounded:
  md: 8px
  lg: 12px
---

# Design System: uFlex

## Overview
uFlex is a medical-grade IoT startup focused on tele-rehabilitation. The design system balances clinical professionalism with innovative technology, ensuring high legibility for patients and data precision for specialists. It follows a "Tech-Organic" aesthetic, using warm backgrounds to reduce hospital-related stress while maintaining sharp, tech-focused interactive elements.

## Colors

### Brand Colors
- **Primary (Cyber Petroleum)** (#074D61): Used for branding, primary navigation, and high-emphasis structural elements.
- **Secondary (Vibrant Eucalyptus)** (#48CBB6): Used for kinetic feedback, sensor data visualization, and primary Call-to-Actions (CTAs).

### Functional Colors
- **Surface (Premium Bone)** (#F7F5ED): The main application background. Softens the UI to reduce eye strain compared to pure white.
- **On-surface (Charcoal)** (#1A1C1E): Primary text color for high readability.
- **Success** (#3BC48E): Positive reinforcement for completed therapy goals and sensor calibration.
- **Error** (#EF5350): Critical alerts regarding sensor disconnection or incorrect movement execution.
- **Warning** (#FFAD33): Cautionary feedback for range-of-motion limits.
- **Info** (#00B4D8): Educational tooltips and general system information.

## Typography
- **Headlines**: Plus Jakarta Sans. Bold/Semi-bold. Used to convey a modern, geometric, and technological feel.
- **Body**: Inter. Regular/Medium. Optimized for high-density data and clinical reports to ensure no misinterpretation of metrics.
- **Data/Metrics**: Inter Medium. 14px–16px. Used for real-time sensor feedback.

## Components (PrimeNG Aura Based)
- **Cards**: Surface color #FFFFFF (Pure White) elevated over the Bone background to create depth. Border radius: 12px.
- **Buttons**: Primary buttons use the Petroleum fill. Secondary/Action buttons use the Turquoise fill. Border radius: 8px.
- **Inputs**: 1px border using Gray 1 (#E6E4D9). Focus state utilizes a 2px Turquoise ring.
- **Navigation**: Sidebars use Petroleum backgrounds in dark mode or Bone backgrounds with subtle borders in light mode.

## Spacing & Grid
- **Base Unit**: 8px (8, 16, 24, 32, 48, 64).
- **Page Padding**: 24px (1.5rem) standard container padding.

## Do's and Don'ts
- **Do** use the Bone (#F7F5ED) background for the main canvas to maintain a "warm" medical feel.
- **Don't** use pure black (#000000) for text; use the Charcoal (#1A1C1E) variant for better visual harmony.
- **Do** prioritize the Turquoise (#48CBB6) for "active" rehabilitation states and kinetic data.
- **Don't** use the Error red for anything other than critical physiological or hardware failures.
