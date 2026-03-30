# Логика расчёта платежей

**Файл реализации:** `calculator/LoanCalculatorService.kt`

---

## Входные параметры

| Параметр | Тип | Описание |
|---------|-----|---------|
| `amount` | `Double` | Сумма кредита, ₽ |
| `annualRate` | `Double` | Годовая процентная ставка, % |
| `termMonths` | `Int` | Срок кредита в месяцах |
| `startDate` | `LocalDate` | Дата выдачи кредита |
| `paymentType` | `PaymentType` | `ANNUITY` или `DIFFERENTIATED` |

---

## Преобразование срока

Перед расчётом срок конвертируется в месяцы функцией `termToMonths()`:

```
DAYS   → termValue / 30  (целая часть)
MONTHS → termValue
YEARS  → termValue * 12
```

---

## Аннуитетный платёж (ANNUITY)

### Формула ежемесячного платежа

```
r = annualRate / 100 / 12          // месячная ставка

payment = amount × r × (1 + r)^n
          ─────────────────────
              (1 + r)^n − 1
```

где `n` — количество месяцев.

### Алгоритм построения графика

```
balance = amount
for month in 1..n:
    interest   = balance × r
    principal  = payment − interest
    balance   -= principal

    schedule += MonthlyPayment(
        month            = month,
        date             = startDate + (month - 1) months,
        payment          = payment,
        principal        = principal,
        interest         = interest,
        remainingBalance = max(0, balance)
    )
```

**Итог:**
- `totalPayment  = payment × n`
- `totalInterest = totalPayment − amount`
- `overpayment   = totalInterest`

### Свойства аннуитета

- Размер платежа **фиксирован** на протяжении всего срока
- В начале доля процентов выше, доля основного долга ниже
- В конце — наоборот
- Общая переплата **больше**, чем при дифференцированном платеже

---

## Дифференцированный платёж (DIFFERENTIATED)

### Формула

```
r = annualRate / 100 / 12

principalPart = amount / n          // постоянная часть

for month in 1..n:
    interest = remainingBalance × r
    payment  = principalPart + interest
```

### Алгоритм построения графика

```
balance = amount
principalPart = amount / n

for month in 1..n:
    interest  = balance × r
    payment   = principalPart + interest
    balance  -= principalPart

    schedule += MonthlyPayment(
        month            = month,
        date             = startDate + (month - 1) months,
        payment          = payment,
        principal        = principalPart,
        interest         = interest,
        remainingBalance = max(0, balance)
    )
```

**Итог:**
- `totalPayment  = Σ payment[i]`
- `totalInterest = Σ interest[i]`
- `overpayment   = totalInterest`

### Свойства дифференцированного платежа

- Доля основного долга **постоянна**
- Сумма платежа **убывает** каждый месяц
- Первый платёж — максимальный, последний — минимальный
- Общая переплата **меньше**, чем при аннуитетном платеже
- Больший платёж в начале требует более высокого дохода заёмщика

---

## Сравнение типов платежей

Пример: кредит 1 500 000 ₽ на 24 месяца под 12% годовых

| Показатель | Аннуитет | Дифференцированный |
|-----------|---------|-------------------|
| Первый платёж | 70 492 ₽ | 77 500 ₽ |
| Последний платёж | 70 492 ₽ | 63 125 ₽ |
| Общая сумма | 1 691 808 ₽ | 1 681 250 ₽ |
| Переплата | 191 808 ₽ | 181 250 ₽ |

---

## Расчёт доли для диаграммы

```kotlin
val total = loanAmountRaw + overpaymentRaw
val loanFraction       = loanAmountRaw / total      // доля тела кредита
val overpaymentFraction = overpaymentRaw / total    // доля переплаты

// Угол дуги в градусах:
loanSweep       = loanFraction * 360f
overpaymentSweep = overpaymentFraction * 360f
```

---

## Граничные случаи

| Ситуация | Поведение |
|---------|----------|
| `annualRate = 0` | `r = 0`, каждый платёж равен `amount / n`, без процентов |
| Последний остаток < 0 | `remainingBalance = max(0, balance)` — зажимается в 0 |
| `termMonths = 0` | Расчёт не запускается (валидация в ViewModel) |
| Пустые поля ввода | Расчёт не запускается, результаты не обновляются |

---

## Точность вычислений

Все расчёты выполняются в `Double` (IEEE 754). Это допустимо для потребительского применения. При необходимости банковской точности следует использовать `BigDecimal`.

Форматирование для отображения применяет `String.format("%.2f")` или аналог перед суффиксом `₽`.
