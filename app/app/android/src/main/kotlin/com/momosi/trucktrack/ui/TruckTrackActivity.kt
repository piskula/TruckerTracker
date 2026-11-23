package com.momosi.trucktrack.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.momosi.trucktrack.app.TruckTrackApp
import com.momosi.trucktrack.core.applinks.AppLinksManager
import org.koin.android.ext.android.inject
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory

class TruckTrackActivity : AppCompatActivity() {

    private val codeAuthFlowFactory: AndroidCodeAuthFlowFactory by inject()
    private val appLinks: AppLinksManager by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codeAuthFlowFactory.registerActivity(this)
        if (savedInstanceState == null) {
            handleAppLink(intent)
        }
        enableEdgeToEdge()
        setContent {
            TruckTrackApp()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleAppLink(intent)
    }

    private fun handleAppLink(intent: Intent) {
        if (intent.action == Intent.ACTION_VIEW) {
            intent.dataString?.let(appLinks::openUrl)
        }
    }
}
