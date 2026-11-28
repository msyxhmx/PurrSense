import Foundation

/// Reads the JSON a model writes, which is often slightly off-format: wrapped in prose or code
/// fences, with trailing commas, scores written as 1.0 or "1", or a single sign as a string.
enum ModelJSON {

    /// Decodes the first JSON object in `text`, or nil if there is none or it can't be read.
    static func decode<T: Decodable>(_ type: T.Type, from text: String?) -> T? {
        guard let raw = extractObject(text),
              let data = removeTrailingCommas(raw).data(using: .utf8) else { return nil }
        return try? JSONDecoder().decode(T.self, from: data)
    }

    /// The first balanced `{...}` in `text`, ignoring braces inside strings. Taking the first `{`
    /// to the last `}` breaks when the model adds a note with braces after the object.
    static func extractObject(_ text: String?) -> String? {
        guard let text, let start = text.firstIndex(of: "{") else { return nil }
        var depth = 0
        var inString = false
        var escaped = false
        var i = start
        while i < text.endIndex {
            let c = text[i]
            if inString {
                if escaped { escaped = false }
                else if c == "\\" { escaped = true }
                else if c == "\"" { inString = false }
            } else if c == "\"" {
                inString = true
            } else if c == "{" {
                depth += 1
            } else if c == "}" {
                depth -= 1
                if depth == 0 { return String(text[start...i]) }
            }
            i = text.index(after: i)
        }
        return nil
    }

    static func removeTrailingCommas(_ s: String) -> String {
        s.replacingOccurrences(of: #",\s*([}\]])"#, with: "$1", options: .regularExpression)
    }
}

/// Neither the first answer nor the retry could be read as JSON.
struct UnreadableAnswerError: LocalizedError {
    var errorDescription: String? { "The AI's answer couldn't be read. Please try the photo again." }
}

private let noneWords: Set<String> = ["", "none", "no", "n/a", "na", "null", "[]"]

extension KeyedDecodingContainer {
    /// 1, 1.0, "1" and "1.4" all read as 1; anything unreadable reads as 0.
    func lenientScore(_ key: Key) -> Int {
        if let i = try? decode(Int.self, forKey: key) { return i }
        if let d = try? decode(Double.self, forKey: key) { return Int(d.rounded()) }
        if let s = try? decode(String.self, forKey: key),
           let d = Double(s.trimmingCharacters(in: .whitespaces)) { return Int(d.rounded()) }
        return 0
    }

    /// 0.8 and "0.8" both read as 0.8.
    func lenientDouble(_ key: Key, default value: Double = 0) -> Double {
        if let d = try? decode(Double.self, forKey: key) { return d }
        if let s = try? decode(String.self, forKey: key),
           let d = Double(s.trimmingCharacters(in: .whitespaces)) { return d }
        return value
    }

    /// true, 1, "yes" and "true" read as true.
    func lenientFlag(_ key: Key) -> Bool {
        if let b = try? decode(Bool.self, forKey: key) { return b }
        if let i = try? decode(Int.self, forKey: key) { return i == 1 }
        if let s = try? decode(String.self, forKey: key) {
            return ["yes", "y", "true", "1"].contains(s.trimmingCharacters(in: .whitespaces).lowercased())
        }
        return false
    }

    /// A list of strings; a single string becomes a one-item list, and "none" becomes empty.
    func lenientList(_ key: Key) -> [String] {
        func keep(_ s: String) -> Bool { !noneWords.contains(s.trimmingCharacters(in: .whitespaces).lowercased()) }
        if let a = try? decode([String?].self, forKey: key) { return a.compactMap { $0 }.filter(keep) }
        if let s = try? decode(String.self, forKey: key), keep(s) { return [s] }
        return []
    }
}
