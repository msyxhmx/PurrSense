import UIKit
import Vision

/// On-device pre-check with Apple Vision (no network, no model download).
///
/// Apple's on-device Foundation Model is text-only, so on iOS the image itself has to be read in the
/// cloud. Vision runs first so that: (1) photos without a cat never leave the phone, and
/// (2) only a crop around the cat is uploaded, not the whole scene (people, home interior).
enum CatVision {

    struct Result {
        let isCat: Bool
        let crop: UIImage
        let confidence: Float
    }

    static func detectCat(in image: UIImage) async throws -> Result {
        guard let cg = image.normalized().cgImage else { return Result(isCat: false, crop: image, confidence: 0) }
        let request = VNRecognizeAnimalsRequest()
        let handler = VNImageRequestHandler(cgImage: cg, options: [:])
        try handler.perform([request])

        let cats = (request.results ?? []).compactMap { obs -> (VNRecognizedObjectObservation, Float)? in
            guard let label = obs.labels.first(where: { $0.identifier == VNAnimalIdentifier.cat.rawValue }) else { return nil }
            return (obs, label.confidence)
        }
        guard let (best, conf) = cats.max(by: { $0.1 < $1.1 }), conf > 0.3 else {
            return Result(isCat: false, crop: image, confidence: 0)
        }
        return Result(isCat: true, crop: crop(cg, to: best.boundingBox) ?? image, confidence: conf)
    }

    /// On-device image embedding (Apple Vision feature print) used to recognise which registered cat
    /// is in the photo. Interim: a general-purpose embedding, not one trained to tell individual cats
    /// apart, and its accuracy on look-alike cats has not been measured. The research
    /// (docs/RESEARCH_HANDOFF.md) recommends DINOv2-Small + ArcFace trained on cat faces (85-89%).
    /// Only this vector is stored, never the photo.
    static func featurePrint(of image: UIImage) throws -> [Float] {
        guard let cg = image.normalized().cgImage else { return [] }
        let request = VNGenerateImageFeaturePrintRequest()
        try VNImageRequestHandler(cgImage: cg, options: [:]).perform([request])
        guard let obs = request.results?.first, obs.elementType == .float else { return [] }
        return obs.data.withUnsafeBytes { Array($0.bindMemory(to: Float.self).prefix(obs.elementCount)) }
    }

    /// Cosine similarity in [-1, 1]; 0 when the vectors can't be compared.
    static func cosine(_ a: [Float], _ b: [Float]) -> Double {
        guard !a.isEmpty, a.count == b.count else { return 0 }
        var dot = 0.0, na = 0.0, nb = 0.0
        for i in a.indices {
            dot += Double(a[i] * b[i]); na += Double(a[i] * a[i]); nb += Double(b[i] * b[i])
        }
        return na == 0 || nb == 0 ? 0 : dot / (na.squareRoot() * nb.squareRoot())
    }

    /// Vision bounding boxes are normalized with origin bottom-left; add 15% padding.
    private static func crop(_ cg: CGImage, to box: CGRect) -> UIImage? {
        let w = CGFloat(cg.width), h = CGFloat(cg.height)
        var r = CGRect(x: box.minX * w, y: (1 - box.maxY) * h, width: box.width * w, height: box.height * h)
        r = r.insetBy(dx: -r.width * 0.15, dy: -r.height * 0.15).intersection(CGRect(x: 0, y: 0, width: w, height: h))
        return cg.cropping(to: r).map { UIImage(cgImage: $0) }
    }
}

extension UIImage {
    /// Bake EXIF orientation into pixels so Vision and cropping agree.
    func normalized() -> UIImage {
        guard imageOrientation != .up else { return self }
        let format = UIGraphicsImageRendererFormat()
        format.scale = scale
        return UIGraphicsImageRenderer(size: size, format: format).image { _ in draw(in: CGRect(origin: .zero, size: size)) }
    }

    func scaled(maxSide: CGFloat) -> UIImage {
        let longest = max(size.width, size.height)
        guard longest > maxSide else { return self }
        let s = maxSide / longest
        let newSize = CGSize(width: size.width * s, height: size.height * s)
        let format = UIGraphicsImageRendererFormat()
        format.scale = 1 // pixels == points, so the model gets exactly maxSide px
        return UIGraphicsImageRenderer(size: newSize, format: format).image { _ in draw(in: CGRect(origin: .zero, size: newSize)) }
    }
}
