package com.momosi.trucktrack.feature.signin.impl

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.momosi.trucktrack.core.uilibrary.components.Button
import com.momosi.trucktrack.core.uilibrary.components.ButtonStyle
import com.momosi.trucktrack.core.uilibrary.components.Icon
import com.momosi.trucktrack.core.uilibrary.components.Text
import com.momosi.trucktrack.core.uilibrary.icons.TruckTrackIcons
import com.momosi.trucktrack.core.uilibrary.theme.AppTheme
import com.momosi.trucktrack.core.uilibrary.theme.TruckTrackTheme
import com.momosi.trucktrack.feature.signin.impl.resources.Res
import com.momosi.trucktrack.feature.signin.impl.resources.sign_in_app_name
import com.momosi.trucktrack.feature.signin.impl.resources.sign_in_app_subtitle
import com.momosi.trucktrack.feature.signin.impl.resources.sign_in_button
import com.momosi.trucktrack.feature.signin.impl.resources.sign_in_error_failed
import com.momosi.trucktrack.feature.signin.impl.resources.sign_in_error_no_internet
import com.momosi.trucktrack.feature.signin.impl.resources.sign_in_error_unable_to_start
import com.momosi.trucktrack.feature.signin.impl.resources.sign_in_hero
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SignInScreen(viewModel: SignInViewModel = koinViewModel(), onNavigateToMyIssues: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.event.collect { event ->
            when (event) {
                SignInEvent.NavigateToMyIssues -> onNavigateToMyIssues()
            }
        }
    }

    SignInContent(
        state = state,
        onAction = viewModel::onAction,
    )
}

@Composable
private fun SignInContent(
    state: SignInState,
    onAction: (SignInAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val errorMessage = when (state) {
        is SignInState.Error.NoInternet -> stringResource(Res.string.sign_in_error_no_internet)
        is SignInState.Error.NoActivity -> stringResource(Res.string.sign_in_error_unable_to_start)
        is SignInState.Error.Generic -> stringResource(Res.string.sign_in_error_failed)
        else -> null
    }

    Box(modifier = modifier.fillMaxSize()) {
        Image(
            painter = painterResource(Res.drawable.sign_in_hero),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            AppTheme.colors.onPrimaryFixedVariant.copy(alpha = 0.5f),
                            AppTheme.colors.onPrimaryFixedVariant.copy(alpha = 0.8f),
                        ),
                    ),
                ),
        )
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier.weight(1f),
                contentAlignment = Alignment.Center,
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = TruckTrackIcons.Truck,
                            contentDescription = null,
                            modifier = Modifier.size(40.dp),
                            tint = Color.White,
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = stringResource(Res.string.sign_in_app_name),
                        style = AppTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(Res.string.sign_in_app_subtitle),
                        style = AppTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.75f),
                    )
                }
            }
            Column(
                modifier = Modifier.padding(bottom = 48.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    text = stringResource(Res.string.sign_in_button),
                    onClick = { onAction(SignInAction.SignInClick) },
                    loading = state is SignInState.Loading,
                    style = ButtonStyle.OutlinedFixed,
                    modifier = Modifier.fillMaxWidth(),
                )
                errorMessage?.let { error ->
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = error,
                        style = AppTheme.typography.bodySmall,
                        color = AppTheme.colors.error,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun SignInIdlePreview() {
    TruckTrackTheme {
        SignInContent(
            state = SignInState.Idle,
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun SignInErrorPreview() {
    TruckTrackTheme {
        SignInContent(
            state = SignInState.Error.NoInternet,
            onAction = {},
        )
    }
}

@Preview
@Composable
private fun SignInLoadingPreview() {
    TruckTrackTheme {
        SignInContent(
            state = SignInState.Loading,
            onAction = {},
        )
    }
}
