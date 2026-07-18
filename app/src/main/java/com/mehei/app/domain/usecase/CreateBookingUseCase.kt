package com.mehei.app.domain.usecase

import com.mehei.app.domain.model.Booking
import com.mehei.app.domain.model.error.AppError
import com.mehei.app.domain.model.error.Result
import com.mehei.app.domain.repository.BookingRepository
import javax.inject.Inject

class CreateBookingUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(booking: Booking): Result<Unit, AppError> {
        if (booking.numHands <= 0) {
            return com.mehei.app.domain.model.error.Result.Error(AppError.ValidationError("Must book for at least 1 hand"))
        }
        if (booking.totalPrice <= 0) {
            return com.mehei.app.domain.model.error.Result.Error(AppError.ValidationError("Total price must be positive"))
        }
        return bookingRepository.createBooking(booking)
    }
}
