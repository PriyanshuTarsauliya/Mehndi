package com.mehei.app.data.repository

import com.mehei.app.domain.model.Booking
import com.mehei.app.domain.model.BookingStatus
import com.mehei.app.domain.model.Complexity
import com.mehei.app.domain.model.EventType
import com.mehei.app.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import com.mehei.app.domain.model.error.Result
import com.mehei.app.domain.model.error.AppError
import kotlinx.coroutines.delay
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MockBookingRepositoryImpl @Inject constructor() : BookingRepository {

    private val initialBookings = listOf(
        Booking(
            id = "book-001",
            customerId = "cust-123",
            artistId = "artist-001",
            artistName = "Priya Sharma",
            bookingDate = "2026-10-24",
            startTime = "10:00",
            endTime = "12:00",
            numHands = 6,
            complexity = Complexity.TRADITIONAL,
            eventType = EventType.KARVA_CHAUTH,
            totalPrice = 3500,
            deposit = 1050,
            status = BookingStatus.ACCEPTED,
            customerNote = "Please bring extra organic henna cones."
        ),
        Booking(
            id = "book-002",
            customerId = "cust-123",
            artistId = "artist-002",
            artistName = "Anjali Mehta",
            bookingDate = "2026-06-15",
            startTime = "16:00",
            endTime = "18:00",
            numHands = 4,
            complexity = Complexity.SIMPLE,
            eventType = EventType.BABY_SHOWER,
            totalPrice = 2800,
            deposit = 840,
            status = BookingStatus.COMPLETED,
            customerNote = ""
        ),
        Booking(
            id = "book-003",
            customerId = "cust-123",
            artistId = "artist-004",
            artistName = "Ritu Patel",
            bookingDate = "2026-05-10",
            startTime = "11:00",
            endTime = "13:00",
            numHands = 2,
            complexity = Complexity.PORTRAIT,
            eventType = EventType.PARTY,
            totalPrice = 2200,
            deposit = 660,
            status = BookingStatus.COMPLETED,
            customerNote = ""
        ),
        Booking(
            id = "book-004",
            customerId = "cust-456",
            artistId = "artist-current", // Mocking the current artist's ID
            artistName = "You",
            bookingDate = "2026-11-15",
            startTime = "09:00",
            endTime = "14:00",
            numHands = 8,
            complexity = Complexity.PORTRAIT,
            eventType = EventType.ENGAGEMENT,
            totalPrice = 12000,
            deposit = 3600,
            status = BookingStatus.REQUESTED,
            customerNote = "Need bridal mehendi for morning event."
        )
    )

    private val _bookings = MutableStateFlow<List<Booking>>(initialBookings)

    override fun observeBookings(): Flow<List<Booking>> = _bookings

    override fun observeBookingById(id: String): Flow<Booking?> {
        return _bookings.map { list -> list.find { it.id == id } }
    }

    override suspend fun getBookings(): Result<List<Booking>, AppError> {
        delay(400)
        return Result.Success(_bookings.value)
    }

    override suspend fun getBookingById(id: String): Result<Booking, AppError> {
        delay(200)
        val booking = _bookings.value.find { it.id == id }
        return if (booking != null) {
            Result.Success(booking)
        } else {
            Result.Error(AppError.NotFoundError("Booking", id))
        }
    }

    override suspend fun createBooking(booking: Booking): Result<Unit, AppError> {
        delay(500)
        _bookings.value = listOf(booking) + _bookings.value
        return Result.Success(Unit)
    }

    override fun observeArtistBookings(artistId: String): Flow<List<Booking>> {
        return _bookings.map { list -> list.filter { it.artistId == artistId } }
    }

    override suspend fun getArtistBookings(artistId: String): Result<List<Booking>, AppError> {
        delay(300)
        val list = _bookings.value.filter { it.artistId == artistId }
        return Result.Success(list)
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit, AppError> {
        delay(300)
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
