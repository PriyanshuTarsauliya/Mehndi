package com.mehei.app.ui.screens.artist.bookingdetail

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import com.mehei.app.domain.model.Booking
import com.mehei.app.domain.model.BookingStatus
import com.mehei.app.domain.model.Complexity
import com.mehei.app.domain.model.EventType
import com.mehei.app.domain.model.error.AppError
import com.mehei.app.domain.model.error.Result
import com.mehei.app.domain.repository.BookingRepository
import com.mehei.app.domain.usecase.ObserveBookingDetailsUseCase
import com.mehei.app.domain.usecase.UpdateBookingStatusUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ArtistBookingDetailViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    
    private lateinit var fakeRepository: FakeBookingRepository
    private lateinit var observeBookingDetailsUseCase: ObserveBookingDetailsUseCase
    private lateinit var updateBookingStatusUseCase: UpdateBookingStatusUseCase
    private lateinit var viewModel: ArtistBookingDetailViewModel

    private val testBooking = Booking(
        id = "book-1",
        customerId = "cust-1",
        artistId = "artist-1",
        artistName = "Test Artist",
        bookingDate = "2026-01-01",
        startTime = "10:00",
        endTime = "12:00",
        numHands = 2,
        complexity = Complexity.SIMPLE,
        eventType = EventType.PARTY,
        totalPrice = 1000,
        deposit = 300,
        status = BookingStatus.PENDING,
        customerNote = ""
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeBookingRepository()
        observeBookingDetailsUseCase = ObserveBookingDetailsUseCase(fakeRepository)
        updateBookingStatusUseCase = UpdateBookingStatusUseCase(fakeRepository)

        fakeRepository.emit(listOf(testBooking))

        val savedStateHandle = SavedStateHandle(mapOf("bookingId" to "book-1"))
        viewModel = ArtistBookingDetailViewModel(
            savedStateHandle = savedStateHandle,
            observeBookingDetails = observeBookingDetailsUseCase,
            updateBookingStatus = updateBookingStatusUseCase
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `initial state emits loading then booking details`() = runTest {
        viewModel.state.test {
            val initialState = awaitItem()
            assertEquals(true, initialState.isLoading)
            
            val loadedState = awaitItem()
            assertEquals(false, loadedState.isLoading)
            assertEquals("book-1", loadedState.booking?.id)
        }
    }

    @Test
    fun `accept booking emits success message and navigates back`() = runTest {
        viewModel.effects.test {
            viewModel.acceptBooking()
            
            val messageEffect = awaitItem()
            assertEquals(ArtistBookingDetailEffect.ShowMessage("Booking Accepted!"), messageEffect)
            
            val navEffect = awaitItem()
            assertEquals(ArtistBookingDetailEffect.NavigateBack, navEffect)
        }
        
        // verify repo state changed
        assertEquals(BookingStatus.CONFIRMED, fakeRepository.bookings.value.first().status)
    }
}

class FakeBookingRepository : BookingRepository {
    val bookings = MutableStateFlow<List<Booking>>(emptyList())

    fun emit(list: List<Booking>) {
        bookings.value = list
    }

    override fun observeBookings(): Flow<List<Booking>> = bookings

    override fun observeBookingById(id: String): Flow<Booking?> {
        return bookings.map { list -> list.find { it.id == id } }
    }

    override suspend fun getBookings(): Result<List<Booking>, AppError> {
        return Result.Success(bookings.value)
    }

    override suspend fun getBookingById(id: String): Result<Booking, AppError> {
        val booking = bookings.value.find { it.id == id }
        return if (booking != null) Result.Success(booking) else Result.Error(AppError.NotFoundError("Booking", id))
    }

    override suspend fun createBooking(booking: Booking): Result<Unit, AppError> {
        bookings.value = bookings.value + booking
        return Result.Success(Unit)
    }

    override fun observeArtistBookings(artistId: String): Flow<List<Booking>> {
        return bookings.map { list -> list.filter { it.artistId == artistId } }
    }

    override suspend fun getArtistBookings(artistId: String): Result<List<Booking>, AppError> {
        return Result.Success(bookings.value.filter { it.artistId == artistId })
    }

    override suspend fun updateBookingStatus(bookingId: String, status: BookingStatus): Result<Unit, AppError> {
        val currentList = bookings.value.toMutableList()
        val index = currentList.indexOfFirst { it.id == bookingId }
        if (index != -1) {
            currentList[index] = currentList[index].copy(status = status)
            bookings.value = currentList
            return Result.Success(Unit)
        }
        return Result.Error(AppError.NotFoundError("Booking", bookingId))
    }
}
