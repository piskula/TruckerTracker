package com.momosi.trucktrack.ui

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.momosi.trucktrack.app.TruckTrackApp
import org.koin.android.ext.android.inject
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory

class TruckTrackActivity : AppCompatActivity() {

    private val codeAuthFlowFactory: AndroidCodeAuthFlowFactory by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codeAuthFlowFactory.registerActivity(this)
        enableEdgeToEdge()
        setContent {
            TruckTrackApp()
        }
    }
}
