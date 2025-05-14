# Digital Signage Android TV App - Development Guide

## 🤖 AI Agent Task Checklist
- [ ] **Project Setup**
  - [ ] Create new Android TV project in Android Studio
  - [ ] Configure Gradle files with latest dependencies
  - [ ] Set up project structure following architecture guidelines
  - [ ] Initialize Git repository

- [ ] **Core Infrastructure**
  - [ ] Set up Dependency Injection (Hilt)
  - [ ] Configure Networking layer (Retrofit)
  - [ ] Implement local storage (Room, SharedPreferences)
  - [ ] Set up background tasks (WorkManager)

- [ ] **Features Implementation**
  - [ ] Implement Splash screen
  - [ ] Build Registration flow with QR code
  - [ ] Create Display engine for content
  - [ ] Add Settings/Debug screens

- [ ] **Testing & Quality**
  - [ ] Write unit tests
  - [ ] Add integration tests
  - [ ] Perform UI tests
  - [ ] Run performance profiling

- [ ] **Deployment**
  - [ ] Configure release signing
  - [ ] Set up CI/CD pipeline
  - [ ] Prepare store listing
  - [ ] Plan maintenance strategy

## 🎯 Success Criteria
- App successfully registers with backend
- Displays content smoothly with transitions
- Handles offline scenarios gracefully
- Auto-starts on boot
- Maintains stable 24/7 operation

Welcome to the development guide for the Digital Signage Android TV Application. This document series aims to provide a comprehensive set of rules, prompts, and instructions to build, run, and maintain the Kotlin-based Android TV app.

**Project Goal:** To create a highly reliable, remotely manageable, and visually engaging Android TV digital signage application that seamlessly pairs with a Laravel backend.

## How to Use This Documentation

This documentation is broken down into several Markdown files, each covering a specific aspect of the project.

1.  **Start with the Basics:**
    *   [01_Project_Vision_And_Goals.md](./01_Project_Vision_And_Goals.md): Understand the overall objectives and target scenarios.
    *   [02_Getting_Started.md](./02_Getting_Started.md): Set up your development environment and learn how to build and run the app.
2.  **Understand the Foundation:**
    *   [03_Technology_Stack_And_Architecture.md](./03_Technology_Stack_And_Architecture.md): Get familiar with the chosen technologies and the app's architectural design.
    *   [04_Core_Application_Flow.md](./04_Core_Application_Flow.md): Learn about the main user journeys and app interactions.
    *   [05_Backend_API_Contract.md](./05_Backend_API_Contract.md): Review the API endpoints the app will communicate with.
3.  **Dive into Implementation:**
    *   [06_Android_App_Implementation_Guide/](./06_Android_App_Implementation_Guide/): This directory contains detailed guides for implementing each part of the application. Pay attention to:
        *   `06_01_Core_Setup_DI_Networking.md`: Essential for project setup.
        *   `06_02_Data_Layer_And_Mocking.md`: Crucial for data handling and development/testing with mock data.
4.  **Testing, Deployment, and Beyond:**
    *   [07_Testing_Strategy.md](./07_Testing_Strategy.md): How to ensure the app is robust.
    *   [08_Deployment_And_Maintenance.md](./08_Deployment_And_Maintenance.md): Guidelines for releasing and maintaining the app.
    *   [09_Future_Enhancements.md](./09_Future_Enhancements.md): Ideas for future development.
    *   [10_Key_Success_Metrics_And_Checklist.md](./10_Key_Success_Metrics_And_Checklist.md): Important metrics and a pre-launch checklist.

Throughout these documents, you'll find:
*   **Rules:** Best practices or requirements for development.
*   **Prompts:** Questions and action items to guide your implementation.
*   **Code Snippets:** Examples in Kotlin, XML, JSON, and Gradle to help you get started.

This guide assumes the Android TV app is the primary focus, interacting with a pre-existing Laravel backend. No backend administration features will be covered here, only the client-side Android TV application.