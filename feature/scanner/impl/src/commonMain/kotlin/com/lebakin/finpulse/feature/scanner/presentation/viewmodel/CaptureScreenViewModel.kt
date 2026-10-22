package com.lebakin.finpulse.feature.scanner.presentation.viewmodel

import com.lebakin.finpulse.core.common.Loadable
import com.lebakin.finpulse.core.mvi.ScreenViewModel
import com.lebakin.finpulse.feature.scanner.domain.CaptureOutcome
import com.lebakin.finpulse.feature.scanner.domain.MAX_RECEIPT_BYTES
import com.lebakin.finpulse.feature.scanner.domain.ReceiptCapture
import com.lebakin.finpulse.feature.scanner.domain.ReceiptDraft
import com.lebakin.finpulse.feature.scanner.domain.ReceiptImage
import com.lebakin.finpulse.feature.scanner.domain.ReceiptEngines
import com.lebakin.finpulse.feature.scanner.domain.ReceiptOcr
import com.lebakin.finpulse.feature.scanner.domain.ReceiptParser
import com.lebakin.finpulse.feature.scanner.domain.ReceiptSamples
import com.lebakin.finpulse.feature.scanner.domain.ReceiptSession
import com.lebakin.finpulse.feature.scanner.domain.ReceiptUnreadable
import com.lebakin.finpulse.feature.scanner.presentation.mapper.toUiModel
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.CaptureAction
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.CaptureReducer
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.CaptureState
import com.lebakin.finpulse.feature.scanner.presentation.model.mvi.toUserMessage
import com.lebakin.finpulse.feature.scanner.presentation.navigation.ScannerNavigator

internal class CaptureScreenViewModel(
    private val capture: ReceiptCapture,
    private val ocr: ReceiptOcr,
    private val parser: ReceiptParser,
    private val engines: ReceiptEngines,
    private val session: ReceiptSession,
    private val navigator: ScannerNavigator,
) : ScreenViewModel<CaptureState, CaptureAction>() {
    override fun createInitialState(): CaptureState =
        CaptureReducer.initial(ReceiptSamples.all.map { it.toUiModel() })

    override fun onAction(action: CaptureAction) {
        when (action) {
            CaptureAction.TakePhoto -> read { capture.takePhoto() }
            CaptureAction.PickFromGallery -> read { capture.pickFromGallery() }
            is CaptureAction.ChooseSample -> chooseSample(action.id)
            CaptureAction.Back -> navigator.back()
            CaptureAction.DismissMessage -> reduce { CaptureReducer.run { withMessage(null) } }
            CaptureAction.DismissSheet -> reduce { CaptureReducer.run { withSheet(null) } }
        }
    }

    private fun chooseSample(id: String) {
        if (isReading()) {
            return
        }
        reduce { CaptureReducer.run { withReading(true) } }
        launchCatching(onError = { error ->
            reduce { CaptureReducer.run { withReading(false).withMessage(error.toUserMessage()) } }
        }) {
            val text = ReceiptSamples.text(id) ?: throw ReceiptUnreadable(ReceiptUnreadable.UNREADABLE)
            openDraft(parser.parse(text))
        }
    }

    private fun read(load: suspend () -> CaptureOutcome) {
        if (isReading()) {
            return
        }
        reduce { CaptureReducer.run { withReading(true) } }
        launchCatching(onError = { error ->
            reduce { CaptureReducer.run { withReading(false).withMessage(error.toUserMessage()) } }
        }) {
            when (val outcome = load()) {
                CaptureOutcome.Cancelled -> reduce { CaptureReducer.run { withReading(false) } }
                CaptureOutcome.Denied -> throw ReceiptUnreadable(ReceiptUnreadable.CAMERA_DENIED)
                CaptureOutcome.Unavailable -> throw ReceiptUnreadable(ReceiptUnreadable.CAMERA_UNAVAILABLE)
                is CaptureOutcome.Image -> readImage(outcome.image)
            }
        }
    }

    private suspend fun readImage(image: ReceiptImage) {
        if (image.bytes.isEmpty() || image.bytes.size > MAX_RECEIPT_BYTES) {
            throw ReceiptUnreadable(ReceiptUnreadable.UNREADABLE)
        }
        openDraft(engines.parse(ocr.read(image)))
    }

    private fun openDraft(draft: ReceiptDraft) {
        session.store(draft)
        reduce { CaptureReducer.run { withReading(false) } }
        navigator.openReview()
    }

    private fun isReading(): Boolean {
        val ready = currentState.content as? Loadable.Ready ?: return false
        return ready.value.reading
    }
}
