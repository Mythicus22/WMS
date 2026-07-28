# Warehouse Operations Suite

## Project Overview

Warehouse Operations Suite is a Kotlin Multiplatform (KMP) application designed to control, monitor, diagnose, maintain and manage multiple PLC-based Radio Shuttles operating inside a warehouse.

The application combines Warehouse Management System (WMS) and Warehouse Control System (WCS) functionality into a single mobile application.

The project is being developed with scalability in mind and will eventually support Android and Desktop from the same shared codebase.

---

# Tech Stack

- Kotlin Multiplatform
- Compose Multiplatform
- MVVM
- Clean Architecture
- Repository Pattern
- Koin Dependency Injection
- Room KMP (local database)
- DataStore
- Kotlin Serialization
- MQTT (future)
- PLC Communication (future)
- Napier Logging

---

# Platforms

Current:
- Android

Prepared:
- Desktop (not implemented yet)

---

# Architecture Rules

Most code must remain inside the shared module.

Only platform-specific implementations belong inside androidMain.

Desktop should remain untouched until later phases.

Business logic must never be placed inside UI.

Repositories must separate data sources from business logic.

UseCases should contain business rules.

ViewModels should coordinate UI only.

---

# Project Structure

shared

- core
- communication
- data
- domain
- features
- presentation
- analytics
- reports
- resources
- docs

Every feature follows the same structure:

- ui
- viewmodel
- repository
- domain
- navigation
- model
- components

---

# Current Development Phase

Completed:

Phase 0
Project setup and architecture foundation.

Phase 1
Application skeleton, navigation, theme, reusable UI components, base architecture.

Phase 2
Authentication, Room Database setup, DataStore session manager, SHA-256 hashing, User Management & Checkbox Permissions.

---

# Development Roadmap

Phase 0
Project Setup

Phase 1
Architecture

Phase 2
Authentication & Database

Phase 3
Shuttle Management

Phase 4
Dashboard

Phase 5
Operator Module

Phase 6
Communication Layer (MQTT + PLC)

Phase 7
Diagnostics

Phase 8
Maintenance

Phase 9
Reports & Analytics

Phase 10
Settings

Phase 11
Desktop Support

Phase 12
Testing & Polish

---

# Development Principles

Do not implement future phases.

Do not generate fake business logic.

Do not create placeholder implementations unless requested.

Prefer interfaces over concrete implementations.

Keep code modular and scalable.

Follow existing architecture instead of introducing new patterns.

Maintain consistency with existing naming conventions.

Project must compile after every change.

---

# UI Guidelines

The application should have a clean industrial design suitable for warehouse operations.

Design priorities:

- Professional
- Modern
- Minimal
- Consistent
- Easy to use with one hand
- Large touch targets
- Clear status indicators
- Material 3
- Good dark mode support

Avoid unnecessary animations.

Focus on readability and operational efficiency.

---

# Design References

Design reference images will be provided separately.

They are references only.

Do NOT copy them exactly.

Instead:

- Understand the overall style.
- Keep the layout similar.
- Improve spacing.
- Improve consistency.
- Improve typography.
- Improve icon alignment.
- Improve color hierarchy.
- Improve responsiveness.
- Fix usability issues.
- Maintain a professional industrial look.

The goal is to create a better version of the reference designs while keeping the same overall feel.

---

# Important Rules

Before implementing any feature:

- Understand the existing architecture.
- Reuse existing components whenever possible.
- Do not duplicate code.
- Do not reorganize completed architecture without reason.
- Prefer composition over duplication.
- Keep shared code inside shared module.

Always prioritize maintainability over quick implementation.