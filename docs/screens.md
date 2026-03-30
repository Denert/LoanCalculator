# Экраны и навигация

## Карта навигации

```
App
├── BottomNavigation
│   ├── [Расчёт]       → CalculatorScreen  (реализован)
│   ├── [Сохраненные]  → SaveListScreen    (заглушка)
│   └── [Настройки]    → SettingsScreen    (заглушка)
│
└── LoanCalculatorScreen
        │  нажатие "Рассчитать" / auto-update
        ▼
    LoanResultScreen  (поверх или внутри CalculatorScreen)
```

---

## Экран 1: Калькулятор (LoanCalculatorScreen)

**Файлы:**
- `calculator/LoanCalculatorScreen.kt` — основной контейнер
- `calculator/LoanCalculatorViewModel.kt` — состояние и логика
- `calculator/LoanCalculatorState.kt` — модели состояния

**Описание:**
Форма ввода параметров кредита реализована в виде bottom sheet с кастомной формой (ручка-вырез сверху).

### Поля формы

| Поле | Тип ввода | Валидация |
|------|-----------|-----------|
| Сумма кредита | NumberField с `ThousandsVisualTransformation` | Только цифры, > 0 |
| Процентная ставка | NumberField (десятичная) | Только цифры и точка, > 0 |
| Срок | NumberField + Dropdown (дни / месяцы / годы) | Только цифры, > 0 |
| Дата начала | Read-only поле, открывает DatePicker | Любая дата |
| Тип платежа | Переключатель (Аннуитетный / Дифференцированный) | — |
| Доп. условия | Switch | — |

### Жесты

- Drag-to-dismiss bottom sheet с velocity-fling
- Nested scroll для прокрутки содержимого внутри sheet

### Состояние (LoanCalculatorState)

```
LoanCalculatorState
├── loanAmount: String
├── interestRate: String
├── termValue: String
├── termUnit: TermUnit        ← DAYS | MONTHS | YEARS
├── startDate: DateState?
├── paymentType: PaymentType  ← ANNUITY | DIFFERENTIATED
└── additionalConditions: Boolean
```

---

## Экран 2: Результаты (LoanResultScreen)

**Файлы:**
- `result/LoanResultScreen.kt` — контейнер
- `result/LoanResultBody.kt` — UI-компоненты
- `result/LoanResultState.kt` — отображаемое состояние

**Описание:**
Отображает результаты расчёта в двух вкладках.

### Шапка экрана

- Кнопка «Назад» (AnimatedVisibility)
- Крупный текст ежемесячного платежа
- Стрелка-разворот для детализации

### Вкладка 1: График платежей

```
┌─────────────────────────────────┐
│  Параметры кредита               │
│  Сумма | Ставка | Срок | Дата   │
├─────────────────────────────────┤
│  Итого                           │
│  Платёж | Переплата | Итого     │
│  Следующий платёж: DD.MM.YYYY   │
├─────────────────────────────────┤
│  № | Дата | Платёж | Долг | %  │  ← таблица
│  1  | ...  | ...    | ...  | ...│
│  2  | ...  | ...    | ...  | ...│
│  ...                             │
└─────────────────────────────────┘
```

### Вкладка 2: Диаграмма

- **Donut chart** — рисуется через `Canvas` (`drawArc`)
  - Синяя дуга — тело кредита (`loanAmountRaw`)
  - Красная дуга — переплата (`overpaymentRaw`)
- Легенда с цветовыми маркерами и подписями
- Карточка с суммарными цифрами

### Состояние (LoanResultState)

```
LoanResultState
├── loanAmount: String        "1 500 000 ₽"
├── interestRate: String      "12.5%"
├── term: String              "24 месяца"
├── startDate: String         "01.04.2026"
├── monthlyPayment: String    "70 492 ₽"
├── paymentSize: String       "70 492 ₽"
├── overpayment: String       "191 808 ₽"
├── totalPayment: String      "1 691 808 ₽"
├── nextPaymentDate: String   "01.05.2026"
├── paymentSchedule: List<MonthlyPayment>
├── loanAmountRaw: Double     1500000.0
└── overpaymentRaw: Double    191808.0
```

---

## Экран 3: Сохранённые (SaveListScreen)

**Статус:** Заглушка — не реализован.

**Планируемое содержимое:**
- Список сохранённых расчётов
- Краткая информация: сумма, ставка, срок, дата

---

## Экран 4: Настройки (SettingsScreen)

**Статус:** Заглушка — не реализован.

---

## Маршруты (Route.kt)

```kotlin
sealed interface Route : NavKey {
    sealed interface TopLevel : Route {
        @Serializable data object CalculatorScreen : TopLevel
        @Serializable data object SaveListScreen   : TopLevel
        @Serializable data object SettingsScreen   : TopLevel
    }
}
```

Все маршруты реализуют `NavKey` из Navigation3 и аннотированы `@Serializable` для type-safe navigation.

---

## Bottom Navigation Bar (LoanNavigationBar.kt)

Компонент принимает:

```kotlin
@Composable
fun LoanNavigationBar(
    destinations: Map<Route.TopLevel, BottomNavItem>,
    currentDestination: Route.TopLevel,
    onNavigateTo: (Route.TopLevel) -> Unit
)
```

`BottomNavItem`:

```kotlin
data class BottomNavItem(
    val icon: ImageVector,
    val label: String
)
```

---

## Жизненный цикл экранов

```
App запуск
    │
    ▼
CalculatorScreen  ←────────────────────────────┐
    │  пользователь вводит данные               │
    │  ViewModel реагирует на StateFlow          │
    │  LoanResultScreen появляется автоматически│
    ▼                                            │
ResultScreen (показан поверх или в той же       │
    │         зоне прокрутки)                    │
    │  back button                               │
    └────────────────────────────────────────────┘
```
