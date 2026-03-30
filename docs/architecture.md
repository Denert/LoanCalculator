# Архитектура приложения

## Обзор

LoanCalculatorKmp — Compose Multiplatform приложение, построенное по **MVI-подобному паттерну** с использованием AndroidX ViewModel и StateFlow.

## Целевые платформы

| Платформа | Entry Point | Особенности |
|-----------|------------|-------------|
| Android | `MainActivity.kt` | `setContent { App() }` |
| iOS | `MainViewController.kt` | `ComposeUIViewController { App() }` |
| Desktop | `main.kt` | `singleWindowApplication { App() }` |

## Слои приложения

```
┌─────────────────────────────────────────────┐
│                 UI Layer                     │
│  Composables (Screen, Body, Components)      │
└───────────────┬─────────────────────────────┘
                │  collectAsState()
┌───────────────▼─────────────────────────────┐
│             ViewModel Layer                  │
│  LoanCalculatorViewModel                     │
│  - StateFlow<LoanCalculatorState>            │
│  - StateFlow<LoanResultState>                │
└───────────────┬─────────────────────────────┘
                │  вызов
┌───────────────▼─────────────────────────────┐
│            Service Layer                     │
│  LoanCalculatorService                       │
│  - calculateAnnuity()                        │
│  - calculateDifferentiated()                 │
└─────────────────────────────────────────────┘
```

## Паттерн управления состоянием

### Унидирекциональный поток данных

```
User Event → ViewModel.onXxxChanged() → MutableStateFlow.update() → UI recompose
```

Входные события пользователя:

| Метод ViewModel | Событие |
|----------------|---------|
| `onLoanAmountChanged(value)` | Ввод суммы кредита |
| `onInterestRateChanged(value)` | Ввод процентной ставки |
| `onTermValueChanged(value)` | Ввод срока |
| `onTermUnitChanged(unit)` | Смена единицы срока |
| `onPaymentTypeChanged(type)` | Смена типа платежа |
| `onStartDateChanged(date)` | Выбор даты начала |
| `onSaveClick()` | Сохранение расчёта (TODO) |

### Реактивный пересчёт

ViewModel подписывается на `calculatorState` во время инициализации и автоматически пересчитывает результаты при любом изменении:

```kotlin
// В init блоке ViewModel
calculatorState
    .onEach { state -> recalculate(state) }
    .launchIn(viewModelScope)
```

## Навигация

Приложение использует **Navigation3** (Jetpack Navigation 3, alpha) с типобезопасными маршрутами через sealed interface:

```
Route (sealed interface, implements NavKey)
└── TopLevel (sealed interface)
    ├── CalculatorScreen  → LoanCalculatorScreen
    ├── SaveListScreen    → (placeholder)
    └── SettingsScreen    → (placeholder)
```

Текущая реализация в `App.kt` использует `mutableStateOf` для хранения текущего маршрута. `NavigationRoot.kt` содержит заготовку для полной интеграции navigation3 backstack.

### Bottom Navigation

`LoanNavigationBar` отображает три вкладки. Маппинг маршрут → UI-элемент задан в `BottomNavItem.kt`:

```kotlin
val TOP_LEVEL_DESTINATION = mapOf(
    Route.TopLevel.CalculatorScreen to BottomNavItem(Icons.Outlined.Calculate, "Расчёт"),
    Route.TopLevel.SaveListScreen   to BottomNavItem(Icons.Outlined.Savings,   "Сохраненные"),
    Route.TopLevel.SettingsScreen   to BottomNavItem(Icons.Outlined.Settings,  "Настройки"),
)
```

## Platform Abstraction

Используется `expect/actual` для платформозависимого кода:

```kotlin
// commonMain
expect class Platform()
expect fun getPlatformName(): String

// androidMain
actual fun getPlatformName(): String = "Android ${android.os.Build.VERSION.SDK_INT}"

// iosMain
actual fun getPlatformName(): String = UIDevice.currentDevice.systemName()

// desktopMain
actual fun getPlatformName(): String = "Desktop JVM"
```

## Организация файлов

```
commonMain/kotlin/com/mickey/loan_calc/
├── App.kt                              # Root composable, навигация
├── Platform.kt                         # expect declarations
├── Greeting.kt                         # (шаблонный файл)
├── navigation/
│   ├── Route.kt                        # Sealed interface маршрутов
│   ├── BottomNavItem.kt                # Data class + маппинг
│   ├── LoanNavigationBar.kt            # Bottom nav composable
│   └── NavigationRoot.kt              # Navigation3 backstack wrapper
├── calculator/
│   ├── LoanCalculatorScreen.kt         # Контейнер экрана калькулятора
│   ├── LoanCalculatorState.kt          # State, enums (TermUnit, PaymentType)
│   ├── LoanCalculatorViewModel.kt      # ViewModel
│   ├── LoanCalculatorService.kt        # Бизнес-логика расчётов
│   ├── LoanCalculationResult.kt        # Модели результата расчёта
│   ├── BottomSheetShape.kt             # Custom Shape для bottom sheet
│   └── ThousandsVisualTransformation.kt # Форматирование ввода
└── result/
    ├── LoanResultScreen.kt             # Контейнер экрана результатов
    ├── LoanResultState.kt              # State с форматированными строками
    └── LoanResultBody.kt               # UI-компоненты: таблица, диаграмма
```

## Ключевые соглашения

1. **Нет DI-фреймворка** — ViewModel создаётся через `viewModel()` без Hilt/Koin
2. **Нет Room/SQLDelight** — данные не персистируются (save — TODO)
3. **Нет общих ресурсов строк** — тексты хардкодятся на русском прямо в composable
4. **Preview** — добавляются к переиспользуемым компонентам
5. **Pluralization** — утилитарная функция `pluralRu()` для русских склонений
