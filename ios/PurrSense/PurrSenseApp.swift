import SwiftUI
import FirebaseCore
import FirebaseAppCheck

@main
struct PurrSenseApp: App {
    @StateObject private var model = AppModel()

    init() {
        // Firebase AI Logic requires App Check. The debug provider is accepted for the hackathon:
        // copy "App Check debug token" from the Xcode console into
        // Firebase console > App Check > Apps > Manage debug tokens.
        // Must be set BEFORE FirebaseApp.configure(). Use App Attest for production.
        AppCheck.setAppCheckProviderFactory(AppCheckDebugProviderFactory())
        FirebaseApp.configure()
    }

    var body: some Scene {
        WindowGroup {
            HomeView()
                .environmentObject(model)
                .task { await model.start() }
        }
    }
}
