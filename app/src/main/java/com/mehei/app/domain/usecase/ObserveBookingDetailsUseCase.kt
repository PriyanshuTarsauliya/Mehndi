package com.mehei.app.domain.usecase

import com.mehei.app.domain.model.Booking
import com.mehei.app.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ObserveBookingDetailsUseCase @Inject constructor(
    private val bookingRepository: BookingRepository
) {
    operator fun invoke(bookingId: String): Flow<Booking?> {
        return bookingRepository.observeBookingById(bookingId)
    }
}
