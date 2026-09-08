package com.setu.saarthi.domain.engine

import com.setu.saarthi.domain.model.BeneficiaryProfile
import com.setu.saarthi.domain.model.FinancialSimulation
import com.setu.saarthi.domain.model.Scheme

/**
 * Indicative EMI/subsidy calculator, ported from `financial.simulator.js`.
 * Pure math, no I/O - this is real logic, not a stand-in, but its output
 * is never authoritative: every result must carry [DISCLAIMER].
 */
interface FinancialSimulator {
    fun simulate(profile: BeneficiaryProfile, scheme: Scheme): FinancialSimulation
    fun calculateEmi(principal: Long, annualRatePercent: Double, tenureMonths: Int): Long

    companion object {
        const val DISCLAIMER =
            "Indicative financial calculation only. Actual loan amount, interest rate, tenure and EMI will be determined by the authorized financing institution based on assessment."
    }
}
