package com.mehei.app.ui.screens.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    state: AuthState,
    onEvent: (AuthEvent) -> Unit,
    onLoginSuccess: () -> Unit,
    onNavigateToSetup: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    modifier: Modifier = Modifier,
) {
    LaunchedEffect(state.isOtpVerified) {
        if (state.isOtpVerified) {
            // In a real app, check if user exists. If yes -> onLoginSuccess(), if no -> onNavigateToSetup()
            onNavigateToSetup() 
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                            MaterialTheme.colorScheme.background
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp)
                    .animateContentSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.weight(1f))

                // Logo / Branding
                Text(
                    text = "MEHEI",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Intimate Celebrations. Premium Artists.",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(48.dp))

                Card(
                    shape = RoundedCornerShape(24.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .padding(24.dp)
                            .animateContentSize(animationSpec = spring()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (state.isLoginWithEmail) {
                            var passwordVisible by remember { mutableStateOf(false) }

                            // Email Input
                            OutlinedTextField(
                                value = state.email,
                                onValueChange = { onEvent(AuthEvent.EmailChanged(it)) },
                                label = { Text("Email Address") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Email, contentDescription = "Email")
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !state.isLoading,
                                singleLine = true,
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Password Input
                            OutlinedTextField(
                                value = state.password,
                                onValueChange = { onEvent(AuthEvent.PasswordChanged(it)) },
                                label = { Text("Password") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Lock, contentDescription = "Password")
                                },
                                trailingIcon = {
                                    val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                                        Icon(imageVector = image, contentDescription = "Toggle password visibility")
                                    }
                                },
                                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !state.isLoading,
                                singleLine = true,
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Forgot Password Link
                            TextButton(
                                onClick = onNavigateToForgotPassword,
                                modifier = Modifier.align(Alignment.End)
                            ) {
                                Text("Forgot Password?", color = MaterialTheme.colorScheme.primary)
                            }
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            // Login Button
                            Button(
                                onClick = { onEvent(AuthEvent.LoginWithEmail) },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !state.isLoading && state.email.isNotBlank() && state.password.isNotBlank()
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = "Login",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        } else {
                            // Phone Input
                            OutlinedTextField(
                                value = state.phoneNumber,
                                onValueChange = { onEvent(AuthEvent.PhoneNumberChanged(it)) },
                                label = { Text("Phone Number") },
                                prefix = { Text("+91 ") },
                                leadingIcon = {
                                    Icon(Icons.Filled.Phone, contentDescription = "Phone")
                                },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !state.isOtpSent && !state.isLoading,
                                singleLine = true,
                            )
    
                            AnimatedVisibility(
                                visible = state.isOtpSent,
                                enter = fadeIn() + expandVertically(spring()),
                                exit = fadeOut() + shrinkVertically(spring())
                            ) {
                                Column {
                                    Spacer(modifier = Modifier.height(16.dp))
                                    OutlinedTextField(
                                        value = state.otpCode,
                                        onValueChange = { onEvent(AuthEvent.OtpCodeChanged(it)) },
                                        label = { Text("6-digit OTP") },
                                        leadingIcon = {
                                            Icon(Icons.Filled.Lock, contentDescription = "OTP")
                                        },
                                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                        modifier = Modifier.fillMaxWidth(),
                                        shape = RoundedCornerShape(16.dp),
                                        enabled = !state.isLoading,
                                        singleLine = true,
                                    )
                                }
                            }
    
                            Spacer(modifier = Modifier.height(24.dp))
    
                            // Main Action Button
                            Button(
                                onClick = {
                                    if (state.isOtpSent) onEvent(AuthEvent.VerifyOtp)
                                    else onEvent(AuthEvent.SendOtp)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                enabled = !state.isLoading && 
                                        (if (state.isOtpSent) state.otpCode.length == 6 else state.phoneNumber.length >= 10)
                            ) {
                                if (state.isLoading) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(24.dp),
                                        color = MaterialTheme.colorScheme.onPrimary,
                                        strokeWidth = 2.dp
                                    )
                                } else {
                                    Text(
                                        text = if (state.isOtpSent) "Verify & Login" else "Send OTP",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // Toggle Login Mode
                        TextButton(onClick = { onEvent(AuthEvent.ToggleLoginMode) }) {
                            Text(
                                text = if (state.isLoginWithEmail) "Login with Phone Number instead" else "Login with Email instead",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                    Text(
                        text = "OR",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    HorizontalDivider(modifier = Modifier.weight(1f), color = MaterialTheme.colorScheme.outlineVariant)
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Google Sign In Mock Button
                OutlinedButton(
                    onClick = onLoginSuccess,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Text(
                        text = "Continue with Google",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                }

                Spacer(modifier = Modifier.weight(2f))
                
                Text(
                    text = "By continuing, you agree to our Terms of Service & Privacy Policy.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
