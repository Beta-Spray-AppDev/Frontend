import android.content.Context
import androidx.core.content.edit

object UpdatePrefs {
    private const val PREFS = "updates"

    private const val KEY_SEEN_PREFIX = "seen_"                // + versionCode
    private const val KEY_LATEST_CODE = "latest_code"
    private const val KEY_LATEST_NAME = "latest_name"
    private const val KEY_LATEST_URL  = "latest_url"
    private const val KEY_LATEST_LOG  = "latest_log"

    private const val PREF = "update_prefs"
    private const val KEY_LAST_DL = "last_download_id"

    fun saveDownloadId(ctx: Context, id: Long) {
        ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .edit { putLong(KEY_LAST_DL, id) }
    }

    fun getDownloadId(ctx: Context): Long {
        return ctx.getSharedPreferences(PREF, Context.MODE_PRIVATE)
            .getLong(KEY_LAST_DL, -1L)
    }

    fun wasSeen(ctx: Context, versionCode: Int) =
        ctx.getSharedPreferences(PREFS, 0).getBoolean(KEY_SEEN_PREFIX + versionCode, false)

    fun markSeen(ctx: Context, versionCode: Int) =
        ctx.getSharedPreferences(PREFS, 0).edit()
            .putBoolean(KEY_SEEN_PREFIX + versionCode, true).apply()

    fun saveLatest(ctx: Context, code: Int, name: String, url: String, log: String?) =
        ctx.getSharedPreferences(PREFS, 0).edit()
            .putInt(KEY_LATEST_CODE, code)
            .putString(KEY_LATEST_NAME, name)
            .putString(KEY_LATEST_URL, url)
            .putString(KEY_LATEST_LOG, log).apply()

    fun readLatest(ctx: Context): Triple<Int, String, Pair<String,String?>>? {
        val sp = ctx.getSharedPreferences(PREFS, 0)
        val code = sp.getInt(KEY_LATEST_CODE, -1)
        val name = sp.getString(KEY_LATEST_NAME, null)
        val url  = sp.getString(KEY_LATEST_URL, null)
        val log  = sp.getString(KEY_LATEST_LOG, null)
        return if (code >= 0 && !name.isNullOrBlank() && !url.isNullOrBlank())
            Triple(code, name, url to log) else null
    }
}
