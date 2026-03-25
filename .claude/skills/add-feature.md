# Skill: Add Feature

> How to add new functionality to Jarvis Android.

## When to Use

- "добавь фичу"
- "добавь функционал"
- "новая возможность"
- Adding new screens, services, or capabilities

---

## 1. Adding a New Screen

### Step 1: Create Screen Composable

```kotlin
// ui/NewFeatureScreen.kt
package com.jarvis.android.ui

@Composable
fun NewFeatureScreen(
    viewModel: NewFeatureViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("New Feature") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        // Content here
    }
}
```

### Step 2: Create ViewModel

```kotlin
// ui/NewFeatureViewModel.kt
@HiltViewModel
class NewFeatureViewModel @Inject constructor(
    private val someRepository: SomeRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NewFeatureState())
    val state: StateFlow<NewFeatureState> = _state.asStateFlow()

    fun onAction(action: NewFeatureAction) {
        // Handle actions
    }
}

data class NewFeatureState(
    val isLoading: Boolean = false,
    val data: String = ""
)

sealed class NewFeatureAction {
    data object Load : NewFeatureAction()
}
```

### Step 3: Add Navigation

```kotlin
// ui/JarvisApp.kt
NavHost(...) {
    composable("new_feature") {
        NewFeatureScreen(
            onNavigateBack = { navController.popBackStack() }
        )
    }
}
```

---

## 2. Adding a New Service

### Step 1: Create Service Class

```kotlin
// service/NewService.kt
@AndroidEntryPoint
class NewService : Service() {

    @Inject
    lateinit var someDependency: SomeDependency

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        // Service logic
        return START_STICKY
    }
}
```

### Step 2: Register in Manifest

```xml
<!-- AndroidManifest.xml -->
<service
    android:name=".service.NewService"
    android:exported="false" />
```

---

## 3. Adding a New Hilt Module

```kotlin
// di/NewModule.kt
@Module
@InstallIn(SingletonComponent::class)
object NewModule {

    @Provides
    @Singleton
    fun provideSomething(): Something {
        return SomethingImpl()
    }
}
```

---

## 4. Adding New Permissions

### Step 1: Declare in Manifest

```xml
<uses-permission android:name="android.permission.NEW_PERMISSION" />
```

### Step 2: Request at Runtime (if dangerous)

```kotlin
// util/PermissionUtils.kt
val newPermissions = arrayOf(
    Manifest.permission.NEW_PERMISSION
)

// In Activity/Composable
val launcher = rememberLauncherForActivityResult(
    ActivityResultContracts.RequestMultiplePermissions()
) { results ->
    // Handle results
}

launcher.launch(newPermissions)
```

---

## 5. Adding Network Endpoint

### Step 1: Add to ApiClient

```kotlin
// network/JarvisApiClient.kt
suspend fun newEndpoint(data: RequestData): Response {
    return client.post("$baseUrl/api/new-endpoint") {
        contentType(ContentType.Application.Json)
        setBody(data)
    }.body()
}
```

### Step 2: Create Data Classes

```kotlin
// network/models/NewModels.kt
@Serializable
data class RequestData(
    val field: String
)

@Serializable
data class Response(
    val result: String
)
```

---

## Checklist

- [ ] Created necessary files (Screen, ViewModel, Service, etc.)
- [ ] Added Hilt annotations (@Inject, @HiltViewModel, @AndroidEntryPoint)
- [ ] Registered in Manifest (if Service/Receiver)
- [ ] Added navigation route (if Screen)
- [ ] Added permissions (if needed)
- [ ] Built successfully: `./gradlew assembleDebug`
- [ ] Tested on device
- [ ] Checked logs for errors

---

## Common Patterns

### State Management
```kotlin
// Use StateFlow for UI state
private val _state = MutableStateFlow(InitialState())
val state: StateFlow<State> = _state.asStateFlow()
```

### Error Handling
```kotlin
try {
    val result = repository.doSomething()
    _state.update { it.copy(data = result) }
} catch (e: Exception) {
    Log.e(TAG, "Error", e)
    _state.update { it.copy(error = e.message) }
}
```

### Coroutine Scopes
```kotlin
// In ViewModel
viewModelScope.launch {
    // Async work
}

// In Service
CoroutineScope(Dispatchers.IO).launch {
    // Background work
}
```

---

*See also: `troubleshooting.md` for common errors*
