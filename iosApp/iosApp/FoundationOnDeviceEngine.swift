import Foundation
import Shared

#if canImport(FoundationModels)
import FoundationModels

@available(iOS 26.0, *)
final class FoundationOnDeviceEngine: NSObject, OnDeviceEngine {
    func ready(completionHandler: @escaping (KotlinBoolean?, Error?) -> Void) {
        let ready = SystemLanguageModel.default.isAvailable
        completionHandler(KotlinBoolean(bool: ready), nil)
    }

    func complete(prompt: String, completionHandler: @escaping (String?, Error?) -> Void) {
        Task {
            do {
                let session = LanguageModelSession()
                let response = try await session.respond(to: prompt)
                completionHandler(response.content, nil)
            } catch {
                completionHandler(nil, error)
            }
        }
    }
}
#endif

func registerFoundationOnDeviceEngine() {
    #if canImport(FoundationModels)
    if #available(iOS 26.0, *) {
        OnDeviceEngineRegistryKt.registerOnDeviceEngine(engine: FoundationOnDeviceEngine())
    }
    #endif
}
