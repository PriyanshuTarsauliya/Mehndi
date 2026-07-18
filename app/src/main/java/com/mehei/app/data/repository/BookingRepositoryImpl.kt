package com.mehei.app.data.repository

import com.mehei.app.data.local.TokenManager
import com.mehei.app.data.remote.CreateBookingRequest
import com.mehei.app.data.remote.MeheiApiService
import com.mehei.app.domain.model.Booking
import com.mehei.app.domain.model.BookingStatus
import com.mehei.app.domain.model.Complexity
import com.mehei.app.domain.model.EventType
import com.mehei.app.domain.repository.BookingRepository
import com.mehei.app.domain.model.error.Result
import com.mehei.app.domain.model.error.AppError
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val apiService: MeheiApiService,
    private val tokenManager: TokenManager
) : BookingRepository {

    private val _bookings = MutableStateFlow<List<Booking>>(emptyList())

    override fun observeBookings(): Flow<List<Booking>> = _bookings

    override fun observeBookingById(id: String): Flow<Booking?> {
        return _bookings.map { list -> list.find { it.id == id } }
    }

    override suspend fun getBookings(): Result<List<Booking>, AppError> {
        val userId = tokenManager.getUserId() ?: return Result.Error(AppError.UnauthorizedError("User not logged in"))
        return try {
            val response = apiService.getBookingHistory(userId)
            if (response.isSuccessful) {
                val list = response.body()?.map { dto ->
                    Booking(
                        id = dto.id,
                        customerId = dto.clientId,
                        artistId = dto.artistId,
                        artistName = dto.artistName,
                        bookingDate = dto.createdAt.take(10),
                        startTime = "12:00",
                        endTime = "14:00",
                        numHands = 2,
                        complexity = Complexity.TRADITIONAL,
                        eventType = EventType.PARTY,
                        totalPrice = dto.amount.toInt(),
                        deposit = (dto.amount * 0.3).toInt(),
                        status = BookingStatus.valueOf(dto.status),
                        customerNote = ""
                    )
                } ?: emptyList()
                _bookings.value = list
                Result.Success(list)
            } else {
                Result.Error(AppError.NetworkError("Failed to fetch bookings: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(AppError.NetworkError(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getBookingById(id: String): Result<Booking, AppError> {
        val booking = _bookings.value.find { it.id == id }
        return if (booking != null) {
            Result.Success(booking)
        } else {
            Result.Error(AppError.NotFoundError("Booking", id))
        }
    }

    override suspend fun createBooking(booking: Booking): Result<Unit, AppError> {
        return try {
            val response = apiService.createBooking(
                CreateBookingRequest(booking.artistId, booking.totalPrice.toDouble())
            )
            if (response.isSuccessful) {
                getBookings()
                Result.Success(Unit)
            } else {
                Result.Error(AppError.NetworkError("Failed to create booking: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.Error(AppError.NetworkError(e.message ?: "Unknown error"))
        }
    }

    override fun observeArtistBookings(artistId: String): Flow<List<Booking>> {
        return _bookings.map { list -> list.filter { it.artistId == artistId } }
    }

    override suspend fun getArtistBookings(artistId: String): Result<List<Booking>, AppError> {
        val list = _bookings.value.filter { it.artistId == artistId }
        return Result.Success(list)
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit, AppError> {
        val currentList = _bookings.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookingId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(status = status)
            _bookings.value = currentList
            return Result.Success(Unit)
        }
        return Result.Error(AppError.NotFoundError("Booking", bookingId))
    }
}
