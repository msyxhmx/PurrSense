package dev.twobrain.purrsense

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.core.content.FileProvider
import dev.twobrain.purrsense.ui.MainViewModel
import dev.twobrain.purrsense.ui.PurrSenseRoot
import java.io.File

class MainActivity : ComponentActivity() {

    private val vm: MainViewModel by viewModels()
    private var onPhoto: ((Bitmap) -> Unit)? = null
    private var cameraUri: Uri? = null

    private val picker = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uri?.let { deliver(it) }
    }
    private val camera = registerForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
        if (ok) cameraUri?.let { deliver(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            PurrSenseRoot(
                vm = vm,
                pickPhoto = { cb ->
                    onPhoto = cb
                    picker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                },
                takePhoto = { cb ->
                    onPhoto = cb
                    val file = File(cacheDir, "scan.jpg")
                    cameraUri = FileProvider.getUriForFile(this, "$packageName.files", file)
                    camera.launch(cameraUri!!)
                },
            )
        }
    }

    /** Decode to a software ARGB_8888 bitmap (hardware bitmaps can't be read by the model). */
    private fun deliver(uri: Uri) {
        val bmp: Bitmap = if (Build.VERSION.SDK_INT >= 28) {
            ImageDecoder.decodeBitmap(ImageDecoder.createSource(contentResolver, uri)) { d, info, _ ->
                d.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val longest = maxOf(info.size.width, info.size.height)
                if (longest > 1600) {
                    val s = 1600f / longest
                    d.setTargetSize((info.size.width * s).toInt(), (info.size.height * s).toInt())
                }
            }
        } else {
            contentResolver.openInputStream(uri).use { BitmapFactory.decodeStream(it) }
        }
        onPhoto?.invoke(bmp)
    }
}
