package com.mehei.app.data.remote

import retrofit2.Response
import retrofit2.http.*

data class OtpRequest(val phoneNumber: String)
data class OtpVerificationRequest(val phoneNumber: String, val otpCode: String)
data class AuthResponse(
    val token: String,
    val userId: String,
    val phoneNumber: String,
    val name: String?,
    val role: String,
    val isNewUser: Boolean,
    val profileImageUrl: String? = null
)
data class ProfileSetupRequest(val name: String, val email: String?, val role: String)
data class ProfileImageResponse(val profileImageUrl: String)
data class ArtistProfileResponse(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val category: String,
    val pricePerHand: Double,
    val bio: String,
    val available: Boolean
)
data class LocationUpdateRequest(val latitude: Double, val longitude: Double)
data class CreateBookingRequest(val artistId: String, val amount: Double)
data class BookingResponse(
    val id: String,
    val clientId: String,
    val clientName: String,
    val artistId: String,
    val artistName: String,
    val amount: Double,
    val status: String,
    val createdAt: String
)

interface MeheiApiService {
    @POST("api/auth/send-otp")
    suspend fun sendOtp(@Body request: OtpRequest): Response<Unit>

    @POST("api/auth/verify-otp")
    suspend fun verifyOtp(@Body request: OtpVerificationRequest): Response<AuthResponse>

    @POST("api/auth/setup-profile")
    suspend fun setupProfile(@Body request: ProfileSetupRequest): Response<AuthResponse>

    @Multipart
    @POST("api/profile/upload-image")
    suspend fun uploadProfileImage(@Part file: okhttp3.MultipartBody.Part): Response<ProfileImageResponse>

    @GET("api/artists")
    suspend fun getArtists(): Response<List<ArtistProfileResponse>>

    @POST("api/locations/update")
    suspend fun updateLocation(@Body request: LocationUpdateRequest): Response<Unit>

    @POST("api/bookings/create")
    suspend fun createBooking(@Body request: CreateBookingRequest): Response<BookingResponse>

    @GET("api/bookings/{userId}")
    suspend fun getBookingHistory(@Path("userId") userId: String): Response<List<BookingResponse>>

    @GET("api/favorites")
    suspend fun getFavorites(): Response<List<ArtistProfileResponse>>

    @POST("api/favorites/{artistId}")
    suspend fun addFavorite(@Path("artistId") artistId: String): Response<Unit>

    @DELETE("api/favorites/{artistId}")
    suspend fun removeFavorite(@Path("artistId") artistId: String): Response<Unit>
}
