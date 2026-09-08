package com.setu.saarthi.di

import com.setu.saarthi.data.engine.DefaultDocumentChecklistGenerator
import com.setu.saarthi.data.engine.DefaultEligibilityEngine
import com.setu.saarthi.data.engine.DefaultFinancialSimulator
import com.setu.saarthi.data.engine.DefaultSchemeMatchingEngine
import com.setu.saarthi.data.engine.DefaultScoringEngine
import com.setu.saarthi.data.future.MockApplicationHandoffService
import com.setu.saarthi.data.future.MockLanguageRepository
import com.setu.saarthi.data.future.MockPartnerRouter
import com.setu.saarthi.data.llm.MockRequirementExtractor
import com.setu.saarthi.data.repository.NewsRepositoryImpl
import com.setu.saarthi.data.repository.ProfileRepositoryImpl
import com.setu.saarthi.data.repository.SavedSchemeRepositoryImpl
import com.setu.saarthi.data.repository.SchemeRepositoryImpl
import com.setu.saarthi.domain.engine.DocumentChecklistGenerator
import com.setu.saarthi.domain.engine.EligibilityEngine
import com.setu.saarthi.domain.engine.FinancialSimulator
import com.setu.saarthi.domain.engine.SchemeMatchingEngine
import com.setu.saarthi.domain.engine.ScoringEngine
import com.setu.saarthi.domain.future.ApplicationHandoffService
import com.setu.saarthi.domain.future.LanguageRepository
import com.setu.saarthi.domain.future.PartnerRouter
import com.setu.saarthi.domain.llm.RequirementExtractor
import com.setu.saarthi.domain.repository.NewsRepository
import com.setu.saarthi.domain.repository.ProfileRepository
import com.setu.saarthi.domain.repository.SavedSchemeRepository
import com.setu.saarthi.domain.repository.SchemeRepository
import com.setu.saarthi.domain.session.QuerySessionHolder
import com.setu.saarthi.domain.usecase.ExtractRequirementUseCase
import com.setu.saarthi.domain.usecase.GetNewsFeedUseCase
import com.setu.saarthi.domain.usecase.GetSavedSchemesUseCase
import com.setu.saarthi.domain.usecase.GetSchemeDetailUseCase
import com.setu.saarthi.domain.usecase.MatchSchemesUseCase
import com.setu.saarthi.domain.usecase.ObserveProfileUseCase
import com.setu.saarthi.domain.usecase.ObserveSavedIdsUseCase
import com.setu.saarthi.domain.usecase.SaveProfileUseCase
import com.setu.saarthi.domain.usecase.ToggleSavedSchemeUseCase
import com.setu.saarthi.ui.screens.clarify.ClarifyViewModel
import com.setu.saarthi.ui.screens.detail.SchemeDetailViewModel
import com.setu.saarthi.ui.screens.home.HomeViewModel
import com.setu.saarthi.ui.screens.profile.ProfileViewModel
import com.setu.saarthi.ui.screens.results.ResultsViewModel
import com.setu.saarthi.ui.screens.saved.SavedViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    // Repositories
    single<SchemeRepository> { SchemeRepositoryImpl() }
    single<NewsRepository> { NewsRepositoryImpl() }
    single<ProfileRepository> { ProfileRepositoryImpl(androidContext()) }
    single<SavedSchemeRepository> { SavedSchemeRepositoryImpl(androidContext()) }

    // The LLM layer
    single<RequirementExtractor> { MockRequirementExtractor() }

    // The deterministic rule engine
    single<EligibilityEngine> { DefaultEligibilityEngine() }
    single<ScoringEngine> { DefaultScoringEngine() }
    single<FinancialSimulator> { DefaultFinancialSimulator() }
    single<DocumentChecklistGenerator> { DefaultDocumentChecklistGenerator() }
    single<SchemeMatchingEngine> {
        DefaultSchemeMatchingEngine(
            schemeRepository = get(),
            eligibilityEngine = get(),
            scoringEngine = get(),
            financialSimulator = get(),
            documentChecklistGenerator = get()
        )
    }

    // Future-feature scaffolding — wired in, not surfaced in the UI yet
    single<LanguageRepository> { MockLanguageRepository() }
    single<PartnerRouter> { MockPartnerRouter() }
    single<ApplicationHandoffService> { MockApplicationHandoffService() }

    // Session bridge (Home -> Clarify -> Results -> Detail)
    single { QuerySessionHolder() }

    // Use cases
    single { ExtractRequirementUseCase(get()) }
    single { MatchSchemesUseCase(get(), get()) }
    single { GetNewsFeedUseCase(get()) }
    single { GetSchemeDetailUseCase(get()) }
    single { ToggleSavedSchemeUseCase(get()) }
    single { ObserveSavedIdsUseCase(get()) }
    single { GetSavedSchemesUseCase(get(), get()) }
    single { ObserveProfileUseCase(get()) }
    single { SaveProfileUseCase(get()) }

    // ViewModels
    viewModel { HomeViewModel(get(), get(), get(), get()) }
    viewModel { ClarifyViewModel(get(), get(), get()) }
    viewModel { ResultsViewModel(get(), get(), get()) }
    viewModel { (schemeId: String) -> SchemeDetailViewModel(schemeId, get(), get(), get(), get()) }
    viewModel { SavedViewModel(get()) }
    viewModel { ProfileViewModel(get(), get()) }
}
