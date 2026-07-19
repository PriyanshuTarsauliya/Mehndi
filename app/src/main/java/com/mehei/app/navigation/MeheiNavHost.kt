package com.mehei.app.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import com.mehei.app.ui.screens.artistdetail.ArtistDetailScreen
import com.mehei.app.ui.screens.artistdetail.ArtistDetailViewModel
import com.mehei.app.ui.screens.booking.BookingCalcEffect
import com.mehei.app.ui.screens.booking.BookingCalcScreen
import com.mehei.app.ui.screens.booking.BookingCalcViewModel
import com.mehei.app.ui.screens.checkout.CheckoutScreen
import com.mehei.app.ui.screens.checkout.CheckoutViewModel
import com.mehei.app.ui.screens.confirm.BookingConfirmScreen

import com.mehei.app.ui.screens.favorites.FavoritesScreen
import com.mehei.app.ui.screens.favorites.FavoritesViewModel
import com.mehei.app.ui.screens.flashslots.FlashSlotsScreen
import com.mehei.app.ui.screens.flashslots.FlashSlotsViewModel
import com.mehei.app.ui.screens.history.BookingsHistoryScreen
import com.mehei.app.ui.screens.history.BookingsHistoryViewModel
import com.mehei.app.ui.screens.request.LiveTrackingScreen
import com.mehei.app.ui.screens.request.MatchingScreen
import com.mehei.app.ui.screens.request.RequestEffect
import com.mehei.app.ui.screens.request.RequestScreen
import com.mehei.app.ui.screens.request.RequestViewModel
import com.mehei.app.ui.screens.payments.PaymentHistoryScreen
import com.mehei.app.ui.screens.payments.PaymentHistoryViewModel
import com.mehei.app.ui.screens.payments.RefundPolicyScreen

@Composable
fun MeheiNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
    startDestination: Any = RequestRoute,
) {
    NavHost(
        navController = navController,
        startDestination = startDestination,
        modifier = modifier,
        enterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { it / 8 }
            )
        },
        exitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { -it / 8 }
            )
        },
        popEnterTransition = {
            fadeIn(animationSpec = tween(300)) + slideInHorizontally(
                animationSpec = tween(300),
                initialOffsetX = { -it / 8 }
            )
        },
        popExitTransition = {
            fadeOut(animationSpec = tween(300)) + slideOutHorizontally(
                animationSpec = tween(300),
                targetOffsetX = { it / 8 }
            )
        }
    ) {
        composable<RequestRoute> {
            val viewModel: RequestViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(viewModel.effects) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is RequestEffect.NavigateToMatching -> {
                            navController.navigate(MatchingRoute(effect.requestId))
                        }
                        is RequestEffect.ShowError -> {}
                    }
                }
            }

            RequestScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<MatchingRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<MatchingRoute>()
            MatchingScreen(
                requestId = route.requestId,
                onMatchFound = { bookingId ->
                    navController.navigate(LiveTrackingRoute(bookingId)) {
                        popUpTo(RequestRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<LiveTrackingRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<LiveTrackingRoute>()
            LiveTrackingScreen(
                bookingId = route.bookingId,
                onBackClick = {
                    navController.navigate(RequestRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onChatClick = { bookingId, customerName ->
                    navController.navigate(ChatRoute(bookingId, customerName))
                }
            )
        }



        composable<FlashSlotsRoute> {
            val viewModel: FlashSlotsViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            FlashSlotsScreen(
                state = state,
                onArtistClick = { artistId ->
                    navController.navigate(ArtistDetailRoute(artistId))
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<ArtistDetailRoute> {
            val viewModel: ArtistDetailViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            ArtistDetailScreen(
                state = state,
                onBookClick = { artistId ->
                    navController.navigate(BookingCalculatorRoute(artistId))
                },
                onFavoriteClick = { viewModel.toggleFavorite() },
                onBackClick = { navController.popBackStack() },
            )
        }

        composable<BookingCalculatorRoute> {
            val viewModel: BookingCalcViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            LaunchedEffect(viewModel.effects) {
                viewModel.effects.collect { effect ->
                    when (effect) {
                        is BookingCalcEffect.NavigateToCheckout -> {
                            navController.navigate(CheckoutRoute(effect.artistId, effect.depositAmount))
                        }
                    }
                }
            }

            BookingCalcScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onBackClick = { navController.popBackStack() },
            )
        }

        composable<CheckoutRoute> {
            val viewModel: CheckoutViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            CheckoutScreen(
                state = state,
                effects = viewModel.effects,
                onEvent = viewModel::onEvent,
                onPaymentSuccess = { bookingId ->
                    navController.navigate(BookingConfirmRoute(bookingId)) {
                        popUpTo(RequestRoute) { inclusive = false }
                    }
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<BookingConfirmRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<BookingConfirmRoute>()
            BookingConfirmScreen(
                bookingId = route.bookingId,
                onViewBookings = {
                    navController.navigate(BookingsHistoryRoute) {
                        popUpTo(RequestRoute) { inclusive = false }
                    }
                },
                onBackToHome = {
                    navController.navigate(RequestRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }

        composable<LoginRoute> {
            val viewModel: com.mehei.app.ui.screens.auth.AuthViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            
            com.mehei.app.ui.screens.auth.LoginScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onLoginSuccess = {
                    navController.navigate(RequestRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateToSetup = {
                    navController.navigate(ProfileSetupRoute)
                },
                onNavigateToForgotPassword = {
                    navController.navigate(ForgotPasswordRoute)
                }
            )
        }

        composable<ForgotPasswordRoute> {
            com.mehei.app.ui.screens.auth.ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<ProfileSetupRoute> {
            val viewModel: com.mehei.app.ui.screens.auth.AuthViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            
            com.mehei.app.ui.screens.auth.ProfileSetupScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onSetupComplete = {
                    navController.navigate(RequestRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                }
            )
        }

        composable<ProfileRoute> {
            val viewModel: com.mehei.app.ui.screens.profile.ProfileViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            com.mehei.app.ui.screens.profile.ProfileScreen(
                state = state,
                onNavigateToPersonalDetails = {
                    navController.navigate(PersonalDetailsRoute)
                },
                onNavigateToHistory = {
                    navController.navigate(BookingsHistoryRoute)
                },
                onNavigateToPaymentMethods = {
                    navController.navigate(PaymentMethodsRoute)
                },
                onNavigateToPayments = {
                    navController.navigate(PaymentHistoryRoute)
                },
                onNavigateToRefundPolicy = {
                    navController.navigate(RefundPolicyRoute)
                },
                onNavigateToSettings = {
                    navController.navigate(SettingsRoute)
                },
                onNavigateToHelpSupport = {
                    navController.navigate(HelpSupportRoute)
                },
                onLogout = {
                    navController.navigate(LoginRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onSwitchToArtistMode = {
                    navController.navigate(ArtistDashboardRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onUploadImage = { part ->
                    viewModel.uploadProfileImage(part)
                }
            )
        }

        composable<PersonalDetailsRoute> {
            val viewModel: com.mehei.app.ui.screens.profile.ProfileViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            
            com.mehei.app.ui.screens.profile.PersonalDetailsScreen(
                state = state,
                onNameChange = viewModel::updateName,
                onEmailChange = viewModel::updateEmail,
                onPhoneChange = viewModel::updatePhone,
                onSaveChanges = {
                    viewModel.saveChanges()
                    navController.navigateUp()
                },
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<PaymentMethodsRoute> {
            com.mehei.app.ui.screens.profile.PaymentMethodsScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<HelpSupportRoute> {
            com.mehei.app.ui.screens.profile.HelpSupportScreen(
                onBackClick = { navController.navigateUp() }
            )
        }

        composable<FavoritesRoute> {
            val viewModel: FavoritesViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            FavoritesScreen(
                state = state,
                onArtistClick = { artistId ->
                    navController.navigate(ArtistDetailRoute(artistId))
                },
                onRemoveFavorite = { artistId ->
                    viewModel.removeFavorite(artistId)
                },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<SettingsRoute> {
            val viewModel: com.mehei.app.ui.screens.settings.SettingsViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            
            com.mehei.app.ui.screens.settings.SettingsScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onNavigateToPrivacyPolicy = { navController.navigate(PrivacyPolicyRoute) },
                onNavigateToTermsOfService = { navController.navigate(TermsOfServiceRoute) },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<PrivacyPolicyRoute> {
            com.mehei.app.ui.screens.settings.PrivacyPolicyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<TermsOfServiceRoute> {
            com.mehei.app.ui.screens.settings.TermsOfServiceScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<FavoritesRoute> {
            val viewModel: com.mehei.app.ui.screens.favorites.FavoritesViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            
            com.mehei.app.ui.screens.favorites.FavoritesScreen(
                state = state,
                onArtistClick = { artistId ->
                    navController.navigate(ArtistDetailRoute(artistId))
                },
                onRemoveFavorite = viewModel::removeFavorite,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<ArtistDashboardRoute> {
            com.mehei.app.ui.screens.artist.dashboard.ArtistDashboardScreen(
                onBookingClick = { bookingId ->
                    navController.navigate(ArtistBookingDetailRoute(bookingId))
                },
                onSwitchToCustomer = {
                    navController.navigate(RequestRoute) {
                        popUpTo(0) { inclusive = true }
                    }
                },
                onPortfolioClick = {
                    navController.navigate(ArtistPortfolioRoute)
                }
            )
        }

        composable<BookingsHistoryRoute> {
            val viewModel: BookingsHistoryViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            BookingsHistoryScreen(
                state = state,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<ArtistBookingDetailRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ArtistBookingDetailRoute>()
            com.mehei.app.ui.screens.artist.bookingdetail.ArtistBookingDetailScreen(
                onBackClick = { navController.popBackStack() },
                onChatClick = { bookingId, customerName ->
                    navController.navigate(ChatRoute(bookingId, customerName))
                }
            )
        }

        composable<ArtistNavigationRoute> { backStackEntry ->
            val route = backStackEntry.toRoute<ArtistNavigationRoute>()
            com.mehei.app.ui.screens.artist.navigation.ArtistNavigationScreen(
                bookingId = route.bookingId,
                onBackClick = { navController.popBackStack() },
                onArrivedClick = { /* TODO */ },
                onStartServiceClick = { /* TODO */ }
            )
        }

        composable<ChatRoute> {
            com.mehei.app.ui.screens.chat.ChatScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<ArtistPortfolioRoute> {
            com.mehei.app.ui.screens.artist.portfolio.ArtistPortfolioScreen(
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<PaymentHistoryRoute> {
            val viewModel: PaymentHistoryViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            PaymentHistoryScreen(
                state = state,
                onRequestRefund = { /* TODO: initiate refund flow */ },
                onBackClick = { navController.popBackStack() }
            )
        }

        composable<RefundPolicyRoute> {
            RefundPolicyScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}
