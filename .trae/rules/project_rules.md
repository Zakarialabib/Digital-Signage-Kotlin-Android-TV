# Digital Signage Android TV App - Project Rules

## Architectural Principles

1. **MVVM Architecture**
   - UI (Composables) must not contain business logic; this belongs in ViewModels or Repositories
   - ViewModels should expose state via StateFlow/SharedFlow
   - Use unidirectional data flow (state flows down, events flow up)

2. **Clean Architecture**
   - Clear separation between data, domain, and presentation layers
   - Repository pattern for data access
   - Use cases for business logic when appropriate

3. **Dependency Injection**
   - Use Hilt for dependency injection
   - Prefer constructor injection
   - Use @HiltViewModel annotation for ViewModels

4. **Modular Design**
   - Features should be self-contained in their respective packages
   - Core functionality should be in the core package
   - Shared UI components should be in the ui package

## Coding Standards

1. **Kotlin Best Practices**
   - Use Kotlin idioms and language features
   - Prefer immutable data (val over var)
   - Use extension functions for utility methods
   - Use sealed classes for UI events and states

2. **Coroutines & Flow**
   - Use coroutines for asynchronous operations
   - Use Flow for reactive streams
   - Handle exceptions properly in coroutines
   - Use viewModelScope for ViewModel coroutines

3. **Compose UI**
   - Follow Material Design guidelines for TV
   - Use state hoisting pattern
   - Keep composables small and focused
   - Use previews for UI development

4. **Error Handling**
   - Graceful degradation
   - Consistent error models
   - Offline support
   - Proper logging

## TV-Specific Requirements

1. **Performance**
   - Optimize for TV hardware
   - Minimize memory usage
   - Efficient image loading
   - Smooth animations and transitions

2. **User Experience**
   - Design for D-pad navigation
   - Clear focus states
   - Appropriate text sizes for 10-foot UI
   - Minimize user input

3. **Reliability**
   - Auto-recovery from errors
   - Graceful handling of network issues
   - Proper lifecycle management
   - Automatic restart on crash

4. **Background Operations**
   - Use WorkManager for reliable background tasks
   - Handle system restarts
   - Minimize battery usage
   - Proper wake lock management