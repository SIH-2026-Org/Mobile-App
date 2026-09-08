package com.setu.saarthi.data.engine

import com.setu.saarthi.domain.engine.FinancialSimulator
import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.Financing
import com.setu.saarthi.domain.model.FinancialSimulation
import com.setu.saarthi.domain.model.FinancingType
import com.setu.saarthi.domain.model.Scheme
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Standard reducing-balance EMI calculator with moratorium handling, ported
 * from the reference engine's `financial.simulator.js`. All outputs are
 * indicative only - see [FinancialSimulator.DISCLAIMER].
 */
class DefaultFinancialSimulator : FinancialSimulator {

    private companion object {
        val TYPE_LABELS = mapOf(
            FinancingType.LOAN to "Loan",
            FinancingType.SUBSIDY to "Subsidy",
            FinancingType.GRANT to "Grant",
            FinancingType.COMPOSITE to "Loan + Subsidy",
            FinancingType.GUARANTEE to "Credit Guarantee",
            FinancingType.INSURANCE to "Insurance",
            FinancingType.PENSION to "Pension",
            FinancingType.SCHOLARSHIP to "Scholarship",
            FinancingType.TRAINING to "Training Support"
        )
    }

    override fun calculateEmi(principal: Long, annualRatePercent: Double, tenureMonths: Int): Long {
        if (principal <= 0 || tenureMonths <= 0) return 0
        if (annualRatePercent == 0.0) return (principal.toDouble() / tenureMonths).roundToLong()

        val r = annualRatePercent / 100.0 / 12.0
        val n = tenureMonths
        val emi = principal * r * (1 + r).pow(n) / ((1 + r).pow(n) - 1)
        return emi.roundToLong()
    }

    private data class FinancingBreakdown(val eligibleFinancing: Long, val ownContribution: Long, val subsidyReceived: Long)

    private fun computeFinancingBreakdown(cost: Long, financing: Financing): FinancingBreakdown {
        if (cost == 0L) return FinancingBreakdown(0, 0, 0)

        val ownContrib = (cost * financing.ownContributionPct / 100.0).roundToLong()
        val loanNeeded = cost - ownContrib

        var eligibleFinancing = loanNeeded
        val max = financing.maxAmount
        if (max != null && eligibleFinancing > max) eligibleFinancing = max

        val subsidyReceived = when {
            financing.subsidyAmount != null -> minOf(financing.subsidyAmount, eligibleFinancing)
            financing.subsidyPct != null -> (eligibleFinancing * financing.subsidyPct / 100.0).roundToLong()
            else -> 0L
        }

        return FinancingBreakdown(eligibleFinancing, ownContrib, subsidyReceived)
    }

    override fun simulate(profile: BeneficiaryProfile, scheme: Scheme): FinancialSimulation {
        val financing = scheme.financing
        val cost = profile.projectCost ?: profile.loanRequired ?: 0L

        if (cost == 0L) {
            return FinancialSimulation.Unavailable(
                message = "Project cost not provided. Share your estimated project cost to see financial projections.",
                disclaimer = FinancialSimulator.DISCLAIMER
            )
        }

        val (eligibleFinancing, ownContribution, subsidyReceived) = computeFinancingBreakdown(cost, financing)
        val principal = maxOf(0L, eligibleFinancing - subsidyReceived)

        val effectiveRate = financing.interestRate.effectiveRate.takeIf { it > 0 } ?: financing.interestRate.base

        val tenureMax = financing.tenureMonths.max
        val tenureMin = financing.tenureMonths.min
        val selectedTenure = tenureMax

        val moratoriumMonths = financing.moratoriumMonths
        val moratoriumInterest = if (moratoriumMonths > 0) {
            (principal * (effectiveRate / 100.0) * (moratoriumMonths / 12.0)).roundToLong()
        } else 0L

        val principalAfterMoratorium = principal + moratoriumInterest
        val repaymentMonths = maxOf(1, selectedTenure - moratoriumMonths)

        val emi = calculateEmi(principalAfterMoratorium, effectiveRate, repaymentMonths)
        val totalRepayment = emi * repaymentMonths
        val totalInterest = maxOf(0L, totalRepayment - principalAfterMoratorium + moratoriumInterest)

        return FinancialSimulation.Available(
            financingType = TYPE_LABELS[financing.type] ?: financing.type.name,
            projectCost = cost,
            ownContribution = ownContribution,
            eligibleFinancing = eligibleFinancing,
            subsidyReceived = subsidyReceived,
            netLoanAmount = principal,
            interestRateBase = financing.interestRate.base,
            interestRateSubsidy = financing.interestRate.subsidyRate ?: 0.0,
            interestRateEffective = effectiveRate,
            tenureMonthsMax = selectedTenure,
            tenureMonthsMin = tenureMin,
            moratoriumMonths = moratoriumMonths,
            repaymentMonths = repaymentMonths,
            moratoriumInterest = moratoriumInterest,
            totalInterest = totalInterest,
            totalRepayment = totalRepayment,
            emiMonthly = emi,
            collateralRequired = financing.collateralRequired,
            subsidyNotes = financing.subsidyNotes,
            disclaimer = FinancialSimulator.DISCLAIMER
        )
    }
}
