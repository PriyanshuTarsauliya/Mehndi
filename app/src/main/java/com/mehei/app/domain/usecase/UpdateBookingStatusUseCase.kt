package com.mehei.app.domain.usecase

import com.mehei.app.domain.model.BookingStatus
import com.mehei.app.domain.model.error.AppError
import com.mehei.app.domain.model.error.Result
import com.mehei.app.domain.repository.BookingRepository
import javax.inject.Inject

class UpdateBookingStatusUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    suspend operator fun invoke(bookingId: String, status: BookingStatus): Result<Unit, AppError> {
        return bookingRepository.updateBookingStatus(bookingId, status)
    }
}
