import SwiftUI

struct AddCatView: View {
    @EnvironmentObject var model: AppModel
    @State private var name = ""
    @State private var appearance = ""

    var body: some View {
        Form {
            if let photo = model.photo {
                Image(uiImage: photo).resizable().scaledToFill().frame(height: 200).clipped()
                    .listRowInsets(EdgeInsets())
            }
            TextField("Name", text: $name)
            Section {
                TextField("Appearance", text: $appearance, axis: .vertical)
            } header: {
                Text("Appearance (used to recognise them)")
            } footer: {
                Text("Tip: add distinctive marks, e.g. \"white left paw, notch in right ear\".")
            }
            Button("Save") { model.saveCat(name: name, appearance: appearance) }
                .disabled(name.trimmingCharacters(in: .whitespaces).isEmpty)
        }
        .navigationTitle("Add a cat")
        .onAppear { appearance = model.suggestedAppearance }
    }
}

struct CatDetailView: View {
    @EnvironmentObject var model: AppModel
    let cat: Cat

    var body: some View {
        List {
            Section("This week") {
                if let t = model.trend {
                    Text(t.0)
                    TraceRow(step: "Trend summary", brain: t.1, millis: 0)
                } else if model.history.isEmpty {
                    Text("No readings yet. Scan \(cat.name) to start tracking.").foregroundStyle(.secondary)
                } else {
                    ProgressView("Summarising…")
                }
            }
            Section("History") {
                ForEach(model.history) { r in
                    VStack(alignment: .leading) {
                        Text(Date(timeIntervalSince1970: r.createdAt / 1000), style: .date)
                            .font(.caption).foregroundStyle(.secondary)
                        Text("\(r.mood.capitalized) · pain \(r.painScore)/10"
                             + (r.deepUrgency.map { " · Deep Check: \($0.replacingOccurrences(of: "_", with: " "))" } ?? ""))
                    }
                }
            }
        }
        .navigationTitle(cat.name)
        .task { await model.loadHistory(for: cat) }
    }
}
