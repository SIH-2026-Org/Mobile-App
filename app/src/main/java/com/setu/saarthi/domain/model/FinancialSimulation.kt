package com.setu.saarthi.domain.model

/**
 * Indicative financial breakdown for a scheme, ported from
 * `financial.simulator.js`. All figures are indicative only - the
 * disclaimer in [FinancialSimulator.DISCLAIMER] must be surfaced wherever
 * this is shown.
 */
sealed interface FinancialSimulation {
    data class Unavailable(val message: String, val disclaimer: String) : FinancialSimulation

    data class Available(
        val financingType: String,
        val projectCost: Long,
        val ownContribution: Long,
        val eligibleFinancing: Long,
        val subsidyReceived: Long,
        val netLoanAmount: Long,
        val interestRateBase: Double,
        val interestRateSubsidy: Double,
        val interestRateEffective: Double,
        val tenureMonthsMax: Int,
        val tenureMonthsMin: Int,
        val moratoriumMonths: Int,
        val repaymentMonths: Int,
        val moratoriumInterest: Long,
        val totalInterest: Long,
        val totalRepayment: Long,
        val emiMonthly: Long,
        val collateralRequired: Boolean,
        val subsidyNotes: String?,
        val disclaimer: String
    ) : FinancialSimulation
}
