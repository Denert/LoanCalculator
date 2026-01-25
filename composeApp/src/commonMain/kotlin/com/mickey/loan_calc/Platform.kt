package com.mickey.loan_calc

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform