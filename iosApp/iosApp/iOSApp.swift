import SwiftUI
import Shared

@main
struct iOSApp: App {
    init() {
        registerFoundationOnDeviceEngine()
        KoinKt.doInitKoin()
    }

    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}
