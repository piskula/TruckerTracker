package com.momosi.trucktrack.core.common.language

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import java.util.Locale

class LanguageRepositoryImpl : LanguageRepository {

    override val requiresRestartToApply: Boolean = false

    private val _language = MutableStateFlow(currentLanguage())
    override val language: StateFlow<AppLanguage> = _language

    override fun setLanguage(language: AppLanguage) {
        _language.value = language
        AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(language.languageTag))
    }

    private fun currentLanguage(): AppLanguage {
        val appliedLocale = AppCompatDelegate.getApplicationLocales().takeUnless { it.isEmpty }?.get(0)
        val languageTag = (appliedLocale ?: Locale.getDefault()).language
        return AppLanguage.entries.firstOrNull { it.languageTag == languageTag } ?: AppLanguage.English
    }
}
