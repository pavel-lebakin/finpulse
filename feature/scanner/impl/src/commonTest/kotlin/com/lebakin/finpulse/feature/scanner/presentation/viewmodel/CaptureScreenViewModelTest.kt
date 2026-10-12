package com.lebakin.finpulse.feature.scanner.presentation.viewmodel

import com.lebakin.finpulse.feature.scanner.domain.CaptureOutcome
import com.lebakin.finpulse.feature.scanner.domain.ReceiptCapture
import com.lebakin.finpulse.feature.scanner.domain.ReceiptClock
import com.lebakin.finpulse.feature.scanner.domain.ReceiptImage
import com.lebakin.finpulse.feature.scanner.domain.ReceiptOcr
import com.lebakin.finpulse.feature.scanner.domain.ReceiptParser
import com.lebakin.finpulse.feature.scanner.domain.ReceiptSamples
import com.lebakin.finpulse.feature.scanner.domain.ReceiptSession
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.CaptureAction
import com.lebakin.finpulse.feature.scanner.presentation.navigation.ScannerNavigator
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class CaptureScreenViewModelTest {
    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun sampleOpensTheReviewWithTheDraft() {
        val session = ReceiptSession()
        val navigator = RecordingNavigator()
        val model = model(session, navigator, FakeReceiptCapture(CaptureOutcome.Cancelled), FakeReceiptOcr(""))
        model.onAction(CaptureAction.ChooseSample("groceries"))
        assertEquals(listOf("review"), navigator.calls)
        assertEquals("Market", session.current()?.merchant)
        assertNull(model.state.value.message)
    }

    @Test
    fun cancelledPhotoStaysOnCapture() {
        val navigator = RecordingNavigator()
        val capture = FakeReceiptCapture(CaptureOutcome.Cancelled)
        val model = model(ReceiptSession(), navigator, capture, FakeReceiptOcr("unused"))
        model.onAction(CaptureAction.TakePhoto)
        assertEquals(1, capture.photos)
        assertEquals(emptyList(), navigator.calls)
        assertNull(model.state.value.message)
    }

    @Test
    fun deniedCameraShowsThePermissionMessage() {
        val model = model(
            ReceiptSession(),
            RecordingNavigator(),
            FakeReceiptCapture(CaptureOutcome.Denied),
            FakeReceiptOcr("unused"),
        )
        model.onAction(CaptureAction.TakePhoto)
        assertEquals("Camera permission is required.", model.state.value.message?.text)
    }

    @Test
    fun recognizedPhotoUsesTheParser() {
        val session = ReceiptSession()
        val navigator = RecordingNavigator()
        val image = ReceiptImage(byteArrayOf(1))
        val model = model(
            session,
            navigator,
            FakeReceiptCapture(CaptureOutcome.Image(image)),
            FakeReceiptOcr(ReceiptSamples.text("cafe").orEmpty()),
        )
        model.onAction(CaptureAction.PickFromGallery)
        assertEquals("North Cafe", session.current()?.merchant)
        assertEquals(listOf("review"), navigator.calls)
    }
}

private fun model(
    session: ReceiptSession,
    navigator: ScannerNavigator,
    capture: ReceiptCapture,
    ocr: ReceiptOcr,
) = CaptureScreenViewModel(
    capture = capture,
    ocr = ocr,
    parser = ReceiptParser(CaptureClock),
    session = session,
    navigator = navigator,
)

private object CaptureClock : ReceiptClock {
    override fun today(): LocalDate = LocalDate(2026, 10, 29)
}

private class FakeReceiptCapture(
    private val outcome: CaptureOutcome,
) : ReceiptCapture {
    var photos: Int = 0

    override suspend fun takePhoto(): CaptureOutcome {
        photos += 1
        return outcome
    }

    override suspend fun pickFromGallery(): CaptureOutcome = outcome
}

private class FakeReceiptOcr(
    private val text: String,
) : ReceiptOcr {
    override suspend fun read(image: ReceiptImage): String = text
}

private class RecordingNavigator : ScannerNavigator {
    val calls = mutableListOf<String>()

    override fun openCapture() {
        calls += "capture"
    }

    override fun openReview() {
        calls += "review"
    }

    override fun back() {
        calls += "back"
    }
}
