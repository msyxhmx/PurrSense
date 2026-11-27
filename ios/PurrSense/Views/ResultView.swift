import SwiftUI

struct ResultView: View {
    @EnvironmentObject var model: AppModel

    var body: some View {
        if let a = model.analysis {
            let g = a.grimace
            let alert = a.painScore >= a.alertThreshold || !a.quick.signs.isEmpty
            ScrollView {
                VStack(alignment: .leading, spacing: 12) {
                    if let photo = model.photo {
                        Image(uiImage: photo).resizable().scaledToFill()
                            .frame(height: 260).frame(maxWidth: .infinity).clipped()
                            .clipShape(RoundedRectangle(cornerRadius: 16))
                    }
                    Text(a.matchedCat?.name ?? "Unknown cat").font(.largeTitle.bold())
                    Text("Mood: \(a.quick.mood.capitalized) · \(Int(a.quick.moodConfidence * 100))% sure").font(.title3)

                    GrimaceCard(grimace: g, painScore: a.painScore, offset: a.painOffset, threshold: a.alertThreshold, alert: alert)

                    if !a.quick.signs.isEmpty {
                        Text("Visible signs: \(a.quick.signs.joined(separator: ", "))").foregroundStyle(.red)
                    }
                    Text(a.careNote)

                    if let d = a.deep {
                        VStack(alignment: .leading, spacing: 4) {
                            Text("Deep Check · \(d.urgency.replacingOccurrences(of: "_", with: " "))").bold()
                            Text(d.summary)
                            ForEach(d.findings, id: \.self) { Text("• \($0)").font(.caption) }
                        }
                        .padding().frame(maxWidth: .infinity, alignment: .leading)
                        .background(Color.red.opacity(0.08), in: RoundedRectangle(cornerRadius: 12))
                    }

                    Text("TwoBrain trace").font(.headline).padding(.top, 8)
                    ForEach(a.trace) { TraceRow(step: $0.step, brain: $0.brain, millis: $0.millis) }

                    Text("PurrSense is a screening aid, not a veterinary diagnosis. If you're worried, call your vet.")
                        .font(.caption).foregroundStyle(.secondary).padding(.top, 8)
                }
                .padding()
            }
            .navigationTitle("Reading")
            .navigationBarTitleDisplayMode(.inline)
        }
    }
}

struct GrimaceCard: View {
    let grimace: Grimace
    let painScore: Int
    let offset: Int
    let threshold: Int
    let alert: Bool

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text("Pain score \(painScore)/10").bold()
            if offset > 0 {
                Text("Grimace \(grimace.total)/10 + \(offset) safety margin: AI models tend to under-score cat pain.")
                    .font(.caption).foregroundStyle(.secondary)
            }
            Text(alert ? "At or above \(threshold): possible pain, consider contacting your vet."
                       : "Below \(threshold): no strong pain signs in this photo.")
                .font(.caption)
            ForEach([("Ears", grimace.ears), ("Eyes", grimace.eyes), ("Muzzle", grimace.muzzle),
                     ("Whiskers", grimace.whiskers), ("Head", grimace.head)], id: \.0) { label, v in
                HStack {
                    Text(label).font(.caption).frame(width: 70, alignment: .leading)
                    ProgressView(value: Double(v), total: 2)
                    Text("\(v)/2").font(.caption)
                }
            }
        }
        .padding()
        .background((alert ? Color.red : Color.green).opacity(0.1), in: RoundedRectangle(cornerRadius: 12))
    }
}

struct TraceRow: View {
    let step: String
    let brain: Brain
    let millis: Int

    var body: some View {
        HStack {
            Text(label).font(.caption2.bold()).foregroundStyle(.white)
                .padding(.horizontal, 6).padding(.vertical, 2)
                .background(color, in: RoundedRectangle(cornerRadius: 6))
            Text(step).font(.caption)
            Spacer()
            if millis > 0 { Text("\(millis) ms").font(.caption).foregroundStyle(.secondary) }
        }
    }
    private var label: String { brain == .onDevice ? "On-device" : brain == .cloud ? "Cloud" : "Rule" }
    private var color: Color { brain == .onDevice ? .green : brain == .cloud ? .blue : .gray }
}
