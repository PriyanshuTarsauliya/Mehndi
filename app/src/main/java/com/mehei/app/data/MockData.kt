package com.mehei.app.data

import com.mehei.app.domain.model.*

/**
 * Mock data provider for MVP screens.
 * Replaces the data layer until Firebase/backend is connected.
 */
object MockData {

    val artists: List<Artist> = listOf(
        Artist(
            id = "artist-001",
            name = "Priya Sharma",
            rating = 4.8f,
            experienceYears = 8,
            bio = "Specializing in intricate Arabic and Rajasthani designs for intimate celebrations. Trained under master artist Kiran Sahni.",
            city = "Mumbai",
            profileImageUrl = "",
            tier = ArtistTier.MASTER,
            specialties = listOf(EventType.KARVA_CHAUTH, EventType.BABY_SHOWER, EventType.TEEJ, EventType.MEHNDI_NIGHT),
            rateCards = listOf(
                RateCard(Complexity.SIMPLE, pricePerHand = 150, hourlyRate = 500),
                RateCard(Complexity.TRADITIONAL, pricePerHand = 300, hourlyRate = 800),
                RateCard(Complexity.PORTRAIT, pricePerHand = 600, hourlyRate = 1500),
            ),
            totalReviews = 142,
            hasFlashSlots = true,
            latitude = 19.0760,   // Bandra
            longitude = 72.8777,
        ),
        Artist(
            id = "artist-002",
            name = "Anjali Mehta",
            rating = 4.6f,
            experienceYears = 3,
            bio = "Apprentice of Priya Sharma. Fresh, modern designs that blend traditional motifs with contemporary minimalism.",
            city = "Mumbai",
            profileImageUrl = "",
            tier = ArtistTier.APPRENTICE,
            parentArtistId = "artist-001",
            specialties = listOf(EventType.PARTY, EventType.FESTIVAL, EventType.BABY_SHOWER),
            rateCards = listOf(
                RateCard(Complexity.SIMPLE, pricePerHand = 100, hourlyRate = 350),
                RateCard(Complexity.TRADITIONAL, pricePerHand = 200, hourlyRate = 550),
                RateCard(Complexity.PORTRAIT, pricePerHand = 400, hourlyRate = 1000),
            ),
            totalReviews = 38,
            hasFlashSlots = true,
            latitude = 19.0178,   // Dadar
            longitude = 72.8478,
        ),
        Artist(
            id = "artist-003",
            name = "Kavita Deshmukh",
            rating = 4.9f,
            experienceYears = 12,
            bio = "Award-winning henna artist with 12+ years in bridal and celebration mehendi. Known for organic, chemical-free henna paste.",
            city = "Mumbai",
            profileImageUrl = "",
            tier = ArtistTier.MASTER,
            specialties = listOf(EventType.KARVA_CHAUTH, EventType.ENGAGEMENT, EventType.HALDI, EventType.MEHNDI_NIGHT),
            rateCards = listOf(
                RateCard(Complexity.SIMPLE, pricePerHand = 200, hourlyRate = 600),
                RateCard(Complexity.TRADITIONAL, pricePerHand = 400, hourlyRate = 1000),
                RateCard(Complexity.PORTRAIT, pricePerHand = 800, hourlyRate = 2000),
            ),
            totalReviews = 287,
            hasFlashSlots = false,
            latitude = 19.1136,   // Andheri
            longitude = 72.8697,
        ),
        Artist(
            id = "artist-004",
            name = "Ritu Patel",
            rating = 4.5f,
            experienceYears = 5,
            bio = "Corporate and party mehendi specialist. Quick, elegant designs perfect for time-boxed gatherings.",
            city = "Mumbai",
            profileImageUrl = "",
            tier = ArtistTier.ASSOCIATE,
            specialties = listOf(EventType.CORPORATE, EventType.PARTY, EventType.FESTIVAL),
            rateCards = listOf(
                RateCard(Complexity.SIMPLE, pricePerHand = 120, hourlyRate = 400),
                RateCard(Complexity.TRADITIONAL, pricePerHand = 250, hourlyRate = 700),
                RateCard(Complexity.PORTRAIT, pricePerHand = 500, hourlyRate = 1200),
            ),
            totalReviews = 64,
            hasFlashSlots = true,
            latitude = 18.9388,   // Vashi (Navi Mumbai)
            longitude = 72.8354,
        ),
        Artist(
            id = "artist-005",
            name = "Neha Gupta",
            rating = 4.7f,
            experienceYears = 6,
            bio = "Festival and Teej specialist from Jaipur. Brings authentic Rajasthani patterns to Mumbai homes.",
            city = "Mumbai",
            profileImageUrl = "",
            tier = ArtistTier.ASSOCIATE,
            specialties = listOf(EventType.TEEJ, EventType.KARVA_CHAUTH, EventType.FESTIVAL, EventType.BABY_SHOWER),
            rateCards = listOf(
                RateCard(Complexity.SIMPLE, pricePerHand = 130, hourlyRate = 450),
                RateCard(Complexity.TRADITIONAL, pricePerHand = 280, hourlyRate = 750),
                RateCard(Complexity.PORTRAIT, pricePerHand = 550, hourlyRate = 1300),
            ),
            totalReviews = 91,
            hasFlashSlots = false,
            latitude = 19.2183,   // Borivali
            longitude = 72.8567,
        ),
    )

    val flashSlots: List<FlashSlot> = listOf(
        FlashSlot(
            id = "flash-001",
            artistId = "artist-001",
            artistName = "Priya Sharma",
            date = "2026-07-08",
            startTime = "14:00",
            endTime = "17:00",
            discountPercent = 20,
            isBooked = false,
        ),
        FlashSlot(
            id = "flash-002",
            artistId = "artist-002",
            artistName = "Anjali Mehta",
            date = "2026-07-09",
            startTime = "10:00",
            endTime = "13:00",
            discountPercent = 30,
            isBooked = false,
        ),
        FlashSlot(
            id = "flash-003",
            artistId = "artist-004",
            artistName = "Ritu Patel",
            date = "2026-07-07",
            startTime = "16:00",
            endTime = "19:00",
            discountPercent = 15,
            isBooked = false,
        ),
    )

    val reviews: List<Review> = listOf(
        Review(
            id = "rev-001",
            bookingId = "book-001",
            artistId = "artist-001",
            customerName = "Meera K.",
            rating = 5.0f,
            comment = "Priya was amazing for our Karva Chauth gathering! Beautiful traditional designs for all 6 of us in under 2 hours.",
            eventType = EventType.KARVA_CHAUTH,
            date = "2026-06-20",
        ),
        Review(
            id = "rev-002",
            bookingId = "book-002",
            artistId = "artist-002",
            customerName = "Sunita R.",
            rating = 4.5f,
            comment = "Anjali did lovely minimal designs for my baby shower. Very professional and on time. Great value for the price!",
            eventType = EventType.BABY_SHOWER,
            date = "2026-06-15",
        ),
        Review(
            id = "rev-003",
            bookingId = "book-003",
            artistId = "artist-001",
            customerName = "Pooja S.",
            rating = 4.8f,
            comment = "Booked Priya through a flash slot for a Tuesday Teej celebration — 20% off and the designs were stunning.",
            eventType = EventType.TEEJ,
            date = "2026-06-10",
        ),
    )
}
