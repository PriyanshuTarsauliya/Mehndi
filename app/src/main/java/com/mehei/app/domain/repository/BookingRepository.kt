package com.mehei.app.domain.repository

import com.mehei.app.domain.model.Booking
import kotlinx.coroutines.flow.Flow

import com.mehei.app.domain.model.error.Result
import com.mehei.app.domain.model.error.AppError
import com.mehei.app.domain.model.BookingStatus

interface BookingRepository {
    fun observeBookings(): Flow<List<Booking>>
    fun observeBookingById(id: String): Flow<Booking?>
    suspend fun getBookings(): Result<List<Booking>, AppError>
    suspend fun getBookingById(id: String): Result<Booking, AppError>
    suspend fun createBooking(booking: Booking): Result<Unit, AppError>
    
    // Artist Flow
    fun observeArtistBookings(artistId: String): Flow<List<Booking>>
    suspend fun getArtistBookings(artistId: String): Result<List<Booking>, AppError>
    suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit, AppError>
}
