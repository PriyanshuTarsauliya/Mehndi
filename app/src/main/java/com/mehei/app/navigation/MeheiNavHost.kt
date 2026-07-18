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
import com.mehei.app.ui.screens.explore.ExploreScreen
import com.mehei.app.ui.screens.explore.ExploreViewModel
import com.mehei.app.ui.screens.history.BookingsHistoryScreen
import com.mehei.app.ui.screens.history.BookingsHistoryViewModel

@Composable
fun MeheiNavHost(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    NavHost(
        navController = navController,
        startDestination = ExploreRoute,
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
        composable<ExploreRoute> {
            val viewModel: ExploreViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()

            ExploreScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onArtistClick = { artistId ->
                    navController.navigate(ArtistDetailRoute(artistId))
                },
                onProfileClick = {
                    navController.navigate(ProfileRoute)
                },
                onFavoritesClick = {
                    navController.navigate(FavoritesRoute)
                }
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
                        popUpTo(ExploreRoute) { inclusive = false }
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
                        popUpTo(ExploreRoute) { inclusive = false }
                    }
                },
                onBackToHome = {
                    navController.navigate(ExploreRoute) {
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
                    navController.navigate(ExploreRoute) {
                        popUpTo(LoginRoute) { inclusive = true }
                    }
                },
                onNavigateToSetup = {
                    navController.navigate(ProfileSetupRoute)
                }
            )
        }

        composable<ProfileSetupRoute> {
            val viewModel: com.mehei.app.ui.screens.auth.AuthViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            
            com.mehei.app.ui.screens.auth.ProfileSetupScreen(
                state = state,
                onEvent = viewModel::onEvent,
                onSetupComplete = {
                    navController.navigate(ExploreRoute) {
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
                onNavigateToHistory = {
                    navController.navigate(BookingsHistoryRoute)
                },
                onNavigateToSettings = {
                    navController.navigate(SettingsRoute)
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
                }
            )
        }

        composable<SettingsRoute> {
            val viewModel: com.mehei.app.ui.screens.settings.SettingsViewModel = hiltViewModel()
            val state by viewModel.state.collectAsState()
            
            com.mehei.app.ui.screens.settings.SettingsScreen(
                state = state,
                onEvent = viewModel::onEvent,
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
                    navController.navigate(ExploreRoute) {
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
    }
}
