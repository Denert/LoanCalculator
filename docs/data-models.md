# Схемы данных

## Перечисления (Enums)

### TermUnit — единица измерения срока кредита

```kotlin
enum class TermUnit(val displayName: String) {
    DAYS("Дни"),
    MONTHS("Месяцы"),
    YEARS("Годы")
}
```

### PaymentType — тип платежа

```kotlin
enum class PaymentType(val displayName: String) {
    ANNUITY("Аннуитетный"),          // Фиксированный ежемесячный платёж
    DIFFERENTIATED("Дифференцированный") // Убывающий платёж
}
```

---

## Входные данные (Calculator Layer)

### DateState — дата начала кредита

```kotlin
data class DateState(
    val year: Int,
    val monthNumber: Int,   // 1–12
    val dayOfMonth: Int     // 1–31
)
```

### LoanCalculatorState — состояние формы ввода

```kotlin
data class LoanCalculatorState(
    val loanAmount: String = "",           // Сумма кредита (строка, т.к. TextField)
    val interestRate: String = "",         // Годовая ставка, % (строка)
    val termValue: String = "",            // Срок (строка)
    val termUnit: TermUnit = TermUnit.MONTHS,
    val startDate: DateState? = <today>,   // null = не задана
    val paymentType: PaymentType = PaymentType.ANNUITY,
    val additionalConditions: Boolean = false
)
```

> Поля суммы, ставки и срока хранятся как `String`, чтобы TextField отображал их без округлений. Парсинг в `Double`/`Int` происходит в сервисном слое.

---

## Модели результата (Service Layer)

### MonthlyPayment — строка графика платежей

```kotlin
data class MonthlyPayment(
    val month: Int,                  // Порядковый номер платежа (1-based)
    val date: LocalDate,             // Дата платежа
    val payment: Double,             // Итоговая сумма платежа за месяц
    val principal: Double,           // Часть платежа — основной долг
    val interest: Double,            // Часть платежа — проценты
    val remainingBalance: Double     // Остаток долга после платежа
)
```

**Инварианты:**
- `payment ≈ principal + interest` (возможны расхождения из-за округления)
- `remainingBalance[n] = remainingBalance[n-1] - principal[n]`
- `remainingBalance[last] ≈ 0`

### LoanCalculationResult — итог расчёта

```kotlin
data class LoanCalculationResult(
    val monthlyPayments: List<MonthlyPayment>,  // Полный график
    val totalPayment: Double,                    // Сумма всех платежей
    val totalInterest: Double,                   // Сумма всех процентных частей
    val overpayment: Double                      // totalPayment - loanAmount
)
```

---

## Состояние экрана результатов (UI Layer)

### LoanResultState — форматированные строки для отображения

```kotlin
data class LoanResultState(
    // Параметры кредита
    val loanAmount: String = "0 ₽",        // "1 500 000 ₽"
    val interestRate: String = "—",         // "12.5%"
    val term: String = "—",                 // "24 месяца"
    val startDate: String = "—",            // "01.04.2026"

    // Итоговые показатели
    val monthlyPayment: String = "0 ₽",    // Первый платёж (аннуитет — фиксированный)
    val paymentSize: String = "0 ₽",       // Псевдоним monthlyPayment
    val overpayment: String = "0 ₽",       // Переплата
    val totalPayment: String = "0 ₽",      // Итого к выплате
    val nextPaymentDate: String = "—",      // Дата следующего платежа

    // Список платежей для таблицы
    val paymentSchedule: List<MonthlyPayment> = emptyList(),

    // Сырые значения для диаграммы
    val loanAmountRaw: Double = 0.0,        // Тело кредита
    val overpaymentRaw: Double = 0.0        // Переплата (для процентов дуги)
)
```

> `LoanResultState` содержит только форматированные строки. Сырые double-значения (`loanAmountRaw`, `overpaymentRaw`) нужны исключительно для расчёта долей в donut-диаграмме.

---

## Потоки данных

### Ввод → Расчёт → Отображение

```
LoanCalculatorState (UI input)
        │
        │ ViewModel.recalculate()
        ▼
LoanCalculatorService.calculate(
    amount: Double,
    rate: Double,
    termMonths: Int,
    startDate: LocalDate,
    paymentType: PaymentType
) → LoanCalculationResult
        │
        │ ViewModel.mapToResultState()
        ▼
LoanResultState (formatted for UI)
        │
        │ collectAsState()
        ▼
LoanResultScreen / LoanResultBody
```

### Формат дат

Внутри приложения используется `kotlinx.datetime.LocalDate`.
Отображение — в формате `DD.MM.YYYY` (русский порядок).

### Формат чисел

Денежные суммы форматируются с пробелом в качестве разделителя тысяч и суффиксом `₽`:

```
1500000.0  →  "1 500 000 ₽"
```

`ThousandsVisualTransformation` применяется к TextField при вводе суммы кредита.

---

## Диаграмма состояний ввода

```
Начало
  │
  ▼
[Пустая форма]
  │  пользователь вводит данные
  ▼
[Частично заполнена]
  │  все поля валидны
  ▼
[Готово к расчёту] ──────────────────┐
  │  автоматический пересчёт          │
  ▼                                   │
[Результат актуален]                  │
  │  пользователь меняет поле ────────┘
  ▼
[Результат обновляется]
```

Расчёт не запускается, если:
- `loanAmount` не парсится в положительное число
- `interestRate` не парсится в положительное число
- `termValue` не парсится в положительное целое
