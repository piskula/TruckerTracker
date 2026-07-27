package com.momosi.trucktrack.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momosi.trucktrack.app.TruckTrackApp
import com.momosi.trucktrack.app.TruckTrackViewModel
import com.momosi.trucktrack.user.model.AuthenticationState
import org.koin.android.ext.android.inject
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.publicvalue.multiplatform.oidc.appsupport.AndroidCodeAuthFlowFactory

class TruckTrackActivity : ComponentActivity() {

    private val codeAuthFlowFactory: AndroidCodeAuthFlowFactory by inject()
    private val appViewModel: TruckTrackViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        codeAuthFlowFactory.registerActivity(this)
        enableEdgeToEdge(
            navigationBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT),
        )
        setContent {
            val view = LocalView.current
            val authState by appViewModel.authenticationState.collectAsStateWithLifecycle()
            SideEffect {
                WindowInsetsControllerCompat(window, view).isAppearanceLightStatusBars =
                    authState == AuthenticationState.Authorized
            }
            TruckTrackApp()
        }
    }
}
