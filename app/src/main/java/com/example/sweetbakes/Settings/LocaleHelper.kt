import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.preference.PreferenceManager
import java.util.*

object LocaleHelper {
    private const val SELECTED_LANGUAGE = "Locale.Helper.Selected.Language"

    fun onAttach(context: Context): Context {
        val lang = getPersistedLanguage(context)
        return setLocale(context, lang)
    }

    fun setLocale(context: Context, language: String): Context {
        val locale = when (language) {
            "Tagalog" -> Locale("tl")
            "Waraynon" -> Locale("war")
            else -> Locale("en")
        }

        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            config.setLayoutDirection(locale)
            return context.createConfigurationContext(config)
        } else {
            @Suppress("DEPRECATION")
            config.locale = locale
            @Suppress("DEPRECATION")
            context.resources.updateConfiguration(config, context.resources.displayMetrics)
        }

        persistLanguage(context, language)
        return context
    }

    private fun persistLanguage(context: Context, language: String) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(SELECTED_LANGUAGE, language)
            .apply()
    }

    fun getPersistedLanguage(context: Context): String {
        return PreferenceManager.getDefaultSharedPreferences(context)
            .getString(SELECTED_LANGUAGE, "English") ?: "English"
    }
}