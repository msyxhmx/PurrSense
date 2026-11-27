package dev.twobrain.purrsense.ai

import android.content.Context
import android.graphics.Bitmap
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.imageembedder.ImageEmbedder
import kotlin.math.sqrt

/**
 * On-device image embeddings for recognising which registered cat is in a photo.
 *
 * Interim model: MediaPipe Image Embedder with MobileNet-V3 Small (Apache-2.0), a general-purpose
 * model, not one trained to tell individual cats apart. Its accuracy on look-alike cats has not
 * been measured. The research (docs/RESEARCH_HANDOFF.md) recommends replacing it with
 * DINOv2-Small + an ArcFace head trained on cat faces, which reported 85-89%.
 *
 * Runs fully on the phone; only the resulting vector is stored, never the photo.
 */
class CatEmbedder(context: Context) {

    private val embedder: ImageEmbedder = ImageEmbedder.createFromOptions(
        context,
        ImageEmbedder.ImageEmbedderOptions.builder()
            .setBaseOptions(BaseOptions.builder().setModelAssetPath(MODEL).build())
            .setRunningMode(RunningMode.IMAGE)
            .setL2Normalize(true)
            .setQuantize(false)
            .build(),
    )

    /** L2-normalised embedding of the photo, or null if the model returned nothing. */
    fun embed(photo: Bitmap): List<Float>? {
        val argb = if (photo.config == Bitmap.Config.ARGB_8888) photo else photo.copy(Bitmap.Config.ARGB_8888, false)
        val result = embedder.embed(BitmapImageBuilder(argb).build())
        return result.embeddingResult().embeddings().firstOrNull()?.floatEmbedding()?.toList()
    }

    companion object {
        private const val MODEL = "mobilenet_v3_small.tflite"

        /** Cosine similarity in [-1, 1]; 0 when the vectors can't be compared. */
        fun cosine(a: List<Float>, b: List<Float>): Double {
            if (a.isEmpty() || a.size != b.size) return 0.0
            var dot = 0.0; var na = 0.0; var nb = 0.0
            for (i in a.indices) {
                dot += a[i] * b[i]; na += a[i] * a[i]; nb += b[i] * b[i]
            }
            return if (na == 0.0 || nb == 0.0) 0.0 else dot / (sqrt(na) * sqrt(nb))
        }
    }
}
