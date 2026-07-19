package com.mehei.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.mehei.app.navigation.BottomNavItem
import com.mehei.app.navigation.BookingsHistoryRoute
import com.mehei.app.navigation.RequestRoute
import com.mehei.app.navigation.LoginRoute
import com.mehei.app.navigation.MeheiNavHost
import com.mehei.app.navigation.ProfileRoute
import com.mehei.app.ui.components.MeheiBottomBar
import com.mehei.app.ui.theme.MeheiTheme
import com.mehei.app.data.local.TokenManager
import com.mehei.app.payment.PaymentEventManager
import com.mehei.app.payment.PaymentResult
import com.razorpay.Checkout
import com.razorpay.PaymentData
import com.razorpay.PaymentResultWithDataListener
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity(), PaymentResultWithDataListener {

    @Inject
    lateinit var paymentEventManager: PaymentEventManager

    @Inject
    lateinit var tokenManager: TokenManager

    // Top-level routes where the bottom bar should be visible
    private val topLevelRoutes = setOf(
        RequestRoute::class.qualifiedName,
        BookingsHistoryRoute::class.qualifiedName,
        ProfileRoute::class.qualifiedName,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Preload Razorpay checkout for faster rendering
        Checkout.preload(applicationContext)
        
        enableEdgeToEdge()
        setContent {
            MeheiTheme {
                val startDestination = if (tokenManager.getToken() != null) RequestRoute else LoginRoute
                val navController = rememberNavController()
                val navBackStackEntry by navController.currentBackStackEntryAsState()
                
                val currentRoute by remember(navBackStackEntry) {
                    derivedStateOf {
                        navBackStackEntry?.destination?.route
                    }
                }
                
                val showBottomBar by remember(currentRoute) {
                    derivedStateOf {
                        currentRoute in topLevelRoutes
                    }
                }

                Scaffold(
                    bottomBar = {
                        AnimatedVisibility(
                            visible = showBottomBar,
                            enter = slideInVertically { it },
                            exit = slideOutVertically { it },
                        ) {
                            MeheiBottomBar(
                                currentRoute = currentRoute,
                                onItemClick = { item ->
                                    navController.navigate(item.route) {
                                        // Pop up to the start destination to avoid
                                        // building up a large stack of destinations
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                }
                            )
                        }
                    }
                ) { innerPadding ->
                    MeheiNavHost(
                        navController = navController,
                        startDestination = startDestination,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                    )
                }
            }
        }
    }

    override fun onPaymentSuccess(paymentId: String?, paymentData: PaymentData?) {
        val result = PaymentResult.Success(
            paymentId = paymentId ?: "",
            signature = paymentData?.signature,
            orderId = paymentData?.orderId
        )
        paymentEventManager.emitResult(result)
    }

    override fun onPaymentError(code: Int, response: String?, paymentData: PaymentData?) {
        val result = PaymentResult.Error(
            code = code,
            description = response ?: "Unknown error"
        )
        paymentEventManager.emitResult(result)
    }
}
