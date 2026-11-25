import SwiftUI
import PhotosUI

struct HomeView: View {
    @EnvironmentObject var model: AppModel
    @State private var galleryItem: PhotosPickerItem?
    @State private var addCatItem: PhotosPickerItem?
    @State private var showCamera = false

    var body: some View {
        NavigationStack(path: $model.path) {
            List {
                Section {
                    OnDeviceBanner(status: model.onDeviceStatus)
                }
                Section {
                    HStack {
                        Button { showCamera = true } label: {
                            Label("Scan", systemImage: "camera.fill").frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.borderedProminent)
                        PhotosPicker(selection: $galleryItem, matching: .images) {
                            Label("Gallery", systemImage: "photo.on.rectangle").frame(maxWidth: .infinity)
                        }
                        .buttonStyle(.bordered)
                    }
                    .disabled(!model.ready)
                }
                Section {
                    if model.cats.isEmpty {
                        Text("Add your cats so PurrSense can tell them apart and track each one over time.")
                            .foregroundStyle(.secondary)
                    }
                    ForEach(model.cats) { cat in
                        NavigationLink(value: Route.cat(cat)) {
                            VStack(alignment: .leading) {
                                Text(cat.name).font(.headline)
                                Text(cat.appearance).font(.caption).foregroundStyle(.secondary)
                            }
                        }
                    }
                } header: {
                    HStack {
                        Text("My cats")
                        Spacer()
                        PhotosPicker(selection: $addCatItem, matching: .images) {
                            Label("Add cat", systemImage: "plus")
                        }
                        .disabled(!model.ready)
                    }
                }
            }
            .navigationTitle("PurrSense")
            .navigationDestination(for: Route.self) { route in
                switch route {
                case .result: ResultView()
                case .addCat: AddCatView()
                case .cat(let cat): CatDetailView(cat: cat)
                }
            }
        }
        .tint(.orange)
        .sheet(isPresented: $showCamera) {
            CameraPicker { image in Task { await model.analyze(image) } }.ignoresSafeArea()
        }
        .onChange(of: galleryItem) { _, item in load(item) { await model.analyze($0) }; galleryItem = nil }
        .onChange(of: addCatItem) { _, item in load(item) { await model.startAddCat($0) }; addCatItem = nil }
        .overlay { if let msg = model.busy { BusyOverlay(message: msg) } }
        .alert("PurrSense", isPresented: .constant(model.error != nil)) {
            Button("OK") { model.error = nil }
        } message: { Text(model.error ?? "") }
    }

    private func load(_ item: PhotosPickerItem?, then action: @escaping (UIImage) async -> Void) {
        guard let item else { return }
        Task {
            if let data = try? await item.loadTransferable(type: Data.self), let img = UIImage(data: data) {
                await action(img.normalized())
            }
        }
    }
}

struct OnDeviceBanner: View {
    let status: String
    var available: Bool { status == "AVAILABLE" }
    var body: some View {
        HStack(spacing: 12) {
            Image(systemName: available ? "iphone" : "cloud.fill").font(.title2)
                .foregroundStyle(available ? .green : .orange)
            VStack(alignment: .leading, spacing: 2) {
                Text(available ? "Apple Intelligence ready" : "Using cloud AI").font(.headline)
                Text(available
                     ? "Identification, care notes and trends run on your iPhone."
                     : "On-device model: \(status). Everything still works via the cloud.")
                    .font(.caption).foregroundStyle(.secondary)
            }
        }
    }
}

struct BusyOverlay: View {
    let message: String
    var body: some View {
        ZStack {
            Color.black.opacity(0.4).ignoresSafeArea()
            VStack(spacing: 12) { ProgressView(); Text(message) }
                .padding(24).background(.regularMaterial, in: RoundedRectangle(cornerRadius: 16))
        }
    }
}

/// Minimal UIKit camera wrapper.
struct CameraPicker: UIViewControllerRepresentable {
    var onImage: (UIImage) -> Void
    @Environment(\.dismiss) private var dismiss

    func makeUIViewController(context: Context) -> UIImagePickerController {
        let p = UIImagePickerController()
        p.sourceType = UIImagePickerController.isSourceTypeAvailable(.camera) ? .camera : .photoLibrary
        p.delegate = context.coordinator
        return p
    }
    func updateUIViewController(_ vc: UIImagePickerController, context: Context) {}
    func makeCoordinator() -> Coordinator { Coordinator(self) }

    final class Coordinator: NSObject, UIImagePickerControllerDelegate, UINavigationControllerDelegate {
        let parent: CameraPicker
        init(_ p: CameraPicker) { parent = p }
        func imagePickerController(_ picker: UIImagePickerController,
                                   didFinishPickingMediaWithInfo info: [UIImagePickerController.InfoKey: Any]) {
            if let img = info[.originalImage] as? UIImage { parent.onImage(img.normalized()) }
            parent.dismiss()
        }
        func imagePickerControllerDidCancel(_ picker: UIImagePickerController) { parent.dismiss() }
    }
}
