package com.momosi.trucktrack.user.di

import com.momosi.trucktrack.core.common.config.AppConfig
import com.momosi.trucktrack.user.AuthManager
import com.momosi.trucktrack.user.AuthManagerImpl
import com.momosi.trucktrack.user.UserRepository
import com.momosi.trucktrack.user.UserRepositoryImpl
import com.momosi.trucktrack.user.internal.TokenVerifier
import com.momosi.trucktrack.user.internal.api.AuthApi
import org.koin.core.module.Module
import org.koin.dsl.module
import org.publicvalue.multiplatform.oidc.OpenIdConnectClient

expect fun platformUserModule(): Module

val userModule = module {
    single {
        val appConfig: AppConfig = get()
        OpenIdConnectClient(discoveryUri = "${appConfig.realmUrl}.well-known/openid-configuration") {
            clientId = appConfig.oauthClientId
            scope = "openid offline_access"
            redirectUri = appConfig.oauthRedirectUrl
            postLogoutRedirectUri = appConfig.oauthLogoutRedirectUrl
        }
    }
    single { AuthApi(get()) }
    single { TokenVerifier(get(), get(), get()) }
    single<UserRepository> { UserRepositoryImpl(get()) }
    single<AuthManager> { AuthManagerImpl(get(), get(), get(), get(), get(), get()) }
}
