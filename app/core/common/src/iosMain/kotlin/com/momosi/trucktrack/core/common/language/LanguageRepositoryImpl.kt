package com.momosi.trucktrack.core.common.language

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.languageCode
import platform.Foundation.preferredLanguages

private const val LANGUAGE_KEY = "app_language"
private const val APPLE_LANGUAGES_KEY = "AppleLanguages"

class LanguageRepositoryImpl : LanguageRepository {

    override val requiresRestartToApply: Boolean = true

    private val defaults = NSUserDefaults.standardUserDefaults

    private val _language = MutableStateFlow(currentLanguage())
    override val language: StateFlow<AppLanguage> = _language

    override fun setLanguage(language: AppLanguage) {
        _language.value = language
        defaults.setObject(language.languageTag, LANGUAGE_KEY)
        defaults.setObject(listOf(language.languageTag), APPLE_LANGUAGES_KEY)
    }

    private fun currentLanguage(): AppLanguage {
        val storedTag = defaults.stringForKey(LANGUAGE_KEY)
        val languageTag = storedTag ?: NSLocale.preferredLanguages.firstOrNull()?.let { NSLocale(it as String).languageCode }
        return AppLanguage.entries.firstOrNull { it.languageTag == languageTag } ?: AppLanguage.English
    }
}
