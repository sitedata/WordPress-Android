package org.wordpress.android.ui.stats.refresh

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.MenuItem
import kotlinx.android.synthetic.main.toolbar.*
import org.wordpress.android.R
import org.wordpress.android.WordPress
import org.wordpress.android.fluxc.model.SiteModel
import org.wordpress.android.ui.stats.OldStatsActivity
import org.wordpress.android.ui.stats.StatsTimeframe
import org.wordpress.android.util.LocaleManager

class StatsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.stats_list_activity)

        setSupportActionBar(toolbar)
        supportActionBar?.let {
            it.setHomeButtonEnabled(true)
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(LocaleManager.setLocale(newBase))
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            onBackPressed()
            return true
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        const val INITIAL_SELECTED_PERIOD_KEY = "INITIAL_SELECTED_PERIOD_KEY"
        @JvmStatic
        fun start(context: Context, site: SiteModel) {
            Log.d("vojta", "Starting without timeframe")
            context.startActivity(buildIntent(context, site))
        }

        fun start(context: Context, localSiteId: Int, statsTimeframe: StatsTimeframe, period: String?) {
            Log.d("vojta", "Starting with timeframe: $statsTimeframe")
            val intent = buildIntent(context, localSiteId, statsTimeframe, period)
            context.startActivity(intent)
        }

        @JvmStatic
        fun buildIntent(context: Context, site: SiteModel): Intent {
            Log.d("vojta", "Building intent without data")
            return buildIntent(context, site.id)
        }

        fun buildIntent(
            context: Context,
            localSiteId: Int,
            statsTimeframe: StatsTimeframe? = null,
            period: String? = null
        ): Intent {
            val intent = Intent(context, StatsActivity::class.java)
            intent.putExtra(WordPress.LOCAL_SITE_ID, localSiteId)
            statsTimeframe?.let { intent.putExtra(OldStatsActivity.ARG_DESIRED_TIMEFRAME, statsTimeframe) }
            period?.let { intent.putExtra(INITIAL_SELECTED_PERIOD_KEY, period) }
            Log.d("vojta", "Building intent: $statsTimeframe $period")
            return intent
        }
    }
}
