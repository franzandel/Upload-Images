package com.example.uploadimages

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import com.example.uploadimages.data.User
import com.example.uploadimages.ui.compose.MainView
import com.example.uploadimages.ui.theme.UploadImagesTheme
import com.example.uploadimages.utils.toDataClass
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.UploadTask
import com.google.firebase.storage.ktx.component1
import com.google.firebase.storage.ktx.component2
import com.google.firebase.storage.ktx.storage
import id.zelory.compressor.Compressor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.apache.commons.io.FileUtils
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

// TODO: Find how is the way to pass clickListener to Compose View
// TODO: Get multiple photo and upload it together (DONE)
// TODO: Integrate to Firestore (DONE)
// TODO: Compress images before uploading (DONE)
class MainActivity : ComponentActivity() {

    private lateinit var onGetPhotoClick: () -> Unit
    private lateinit var onUploadPhotoClick: () -> Unit
    private lateinit var onDownloadPhotoClick: () -> Unit
    private lateinit var onClearPhotoClick: () -> Unit
    private lateinit var onSubmitClick: () -> Unit
    private lateinit var onRetrieveClick: () -> Unit
    private lateinit var activityResultLauncher: ActivityResultLauncher<String>
    private val viewModel by viewModels<MainViewModel>()
    private lateinit var downloadPath: String
    private var downloadPaths = mutableListOf<Uri>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UploadImagesTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainView(
                        onGetPhotoClick,
                        onUploadPhotoClick,
                        onDownloadPhotoClick,
                        onClearPhotoClick,
                        onSubmitClick,
                        onRetrieveClick,
                        viewModel
                    )
                }
            }
        }

        activityResultLauncher =
            registerForActivityResult(ActivityResultContracts.GetMultipleContents()) {
//            val imageStream = contentResolver.openInputStream(it)
//            val selectedImage = BitmapFactory.decodeStream(imageStream)
//            viewModel.bitmap = selectedImage
//            viewModel.imageUri = it
                viewModel.imagesUri = it
            }

        onGetPhotoClick = {
            activityResultLauncher.launch("image/*")
        }

        val storage = Firebase.storage
        val storageReference = storage.reference
        val db = Firebase.firestore

        onUploadPhotoClick = {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.uploadEnabled = false

                val uploadTasks = mutableListOf<UploadTask>()
                viewModel.imagesUri.forEach {
                    val file = createTmpFileFromUri(this@MainActivity, it, "test")
//                    val file = File(it.path.toString())
//                    val asdf = DocumentFile.fromSingleUri(this@MainActivity, it)
//                    val zxcv = File(applicationContext.getExternalFilesDir(null).toString(), "file.png")
//                    val asdf = copyStreamToFile(contentResolver.openInputStream(it)!!, zxcv)
                    val compressedImageFile =
                        Compressor.compress(this@MainActivity, file!!, Dispatchers.Main)

                    val firstImageRef = storageReference.child("images/${it.lastPathSegment}")
                    val uploadTask = firstImageRef.putFile(compressedImageFile.toUri())
//                    val uploadTask = firstImageRef.putFile(it)
                    uploadTasks.add(uploadTask)

                    val urlTask =
                        uploadTask.addOnProgressListener { (bytesTransferred, totalByteCount) ->
                            val progress = bytesTransferred / totalByteCount
                            viewModel.progress = progress.toFloat()
                        }.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            // the uri here should be save in Firestore
                            firstImageRef.downloadUrl
                        }

                    urlTask.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // the uri here should be save in Firestore
                            // e.g. https://firebasestorage.googleapis.com/v0/b/upload-images-8607a.appspot.com/o/first_image.jpg?alt=media&token=0cdb3442-222b-4cae-8e75-874e361733e4
                            downloadPath = task.result.toString()
                            Timber.d(downloadPath)
                            downloadPaths.add(Uri.parse(downloadPath))
                        } else {
                            // Handle failures
                            // ...
                        }
                        file.delete()
                    }.addOnFailureListener {
                        // Handle unsuccessful uploads
                        Timber.d(it.message.toString())
                        Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
                        file.delete()
                    }
                }

                Tasks.whenAll(uploadTasks).addOnCompleteListener {
                    viewModel.uploadEnabled = true
                    Toast.makeText(this@MainActivity, "All images uploaded", Toast.LENGTH_SHORT)
                        .show()
                }.addOnFailureListener {
                    viewModel.uploadEnabled = true
                    Timber.d(it.message.toString())
                    Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        onDownloadPhotoClick = {
            viewModel.imagesUri = downloadPaths
//            viewModel.downloadEnabled = false
            // this path must get from Firestore
//            viewModel.imageUri = Uri.parse(downloadPath)

//            val firstImageRef = storageReference.child("images/first_image.jpg")
//            val firstImageRef = storage.getReferenceFromUrl(downloadPath)
//            val ONE_MEGABYTE: Long = 1024 * 1024

//            firstImageRef.getBytes(ONE_MEGABYTE).addOnSuccessListener {
//                // Data for "images/first_image.jpg" is returned, use this as needed
//                viewModel.bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
//                viewModel.downloadEnabled = true
//            }.addOnFailureListener {
//                // Handle any errors
//                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
//                viewModel.downloadEnabled = true
//            }
        }

        onClearPhotoClick = {
//            viewModel.bitmap = ContextCompat.getDrawable(this, R.drawable.ic_launcher_background)!!.toBitmap()
//            viewModel.imageUri = Uri.parse("")
            viewModel.imagesUri = listOf()
            downloadPaths.clear()
        }

        onSubmitClick = {
            lifecycleScope.launch(Dispatchers.IO) {
                viewModel.submitEnabled = false
                viewModel.showSubmitProgress = true
                val uploadTasks = mutableListOf<UploadTask>()
                viewModel.imagesUri.forEach {
                    val file = createTmpFileFromUri(this@MainActivity, it, "test")
                    val compressedImageFile =
                        Compressor.compress(this@MainActivity, file!!, Dispatchers.Main)
                    val firstImageRef = storageReference.child("images/${it.lastPathSegment}")
                    val uploadTask = firstImageRef.putFile(compressedImageFile.toUri())
//                val uploadTask = firstImageRef.putFile(it)
                    uploadTasks.add(uploadTask)

                    val urlTask =
                        uploadTask.addOnProgressListener { (bytesTransferred, totalByteCount) ->
                            val progress = bytesTransferred / totalByteCount
                            viewModel.progress = progress.toFloat()
                        }.continueWithTask { task ->
                            if (!task.isSuccessful) {
                                task.exception?.let {
                                    throw it
                                }
                            }
                            // the uri here should be save in Firestore
                            firstImageRef.downloadUrl
                        }

                    urlTask.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            // the uri here should be save in Firestore
                            // e.g. https://firebasestorage.googleapis.com/v0/b/upload-images-8607a.appspot.com/o/first_image.jpg?alt=media&token=0cdb3442-222b-4cae-8e75-874e361733e4
                            downloadPath = task.result.toString()
                            Timber.d(downloadPath)
                            downloadPaths.add(Uri.parse(downloadPath))

                            if (downloadPaths.size == viewModel.imagesUri.size) {
                                val downloadPathsString = downloadPaths.map { paths ->
                                    paths.toString()
                                }
                                val user = hashMapOf(
                                    "title" to viewModel.onTitleChanged.text,
                                    "description" to viewModel.onDescriptionChanged.text,
                                    "images" to downloadPathsString
                                )

                                // Add a new document with a generated ID
                                db.collection("users").document("user1")
                                    .set(user)
                                    .addOnSuccessListener {
//                                    Timber.d("DocumentSnapshot added with ID: ${documentReference.id}")
                                        Toast.makeText(
                                            this@MainActivity,
                                            "Successfully submitted",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                    .addOnFailureListener { e ->
                                        Timber.d("Error adding document", e)
                                    }
                                viewModel.submitEnabled = true
                                viewModel.showSubmitProgress = false
                            }
                        } else {
                            // Handle failures
                            // ...
                        }
                        file.delete()
                    }.addOnFailureListener {
                        // Handle unsuccessful uploads
                        Timber.d(it.message.toString())
                        Toast.makeText(this@MainActivity, it.message, Toast.LENGTH_SHORT).show()
                        file.delete()
                    }
                }
            }

            // Tasks.whenAll is executed before latest downloadUrl being added, that is why i use checking trick
//            Tasks.whenAll(uploadTasks).addOnSuccessListener {
//                val downloadPathsString = downloadPaths.map {
//                    it.toString()
//                }
//                val user = hashMapOf(
//                    "title" to viewModel.onTitleChanged.text,
//                    "description" to viewModel.onDescriptionChanged.text,
//                    "images" to downloadPathsString
//                )
//
//                // Add a new document with a generated ID
//                db.collection("users")
//                    .add(user)
//                    .addOnSuccessListener { documentReference ->
//                        Timber.d("DocumentSnapshot added with ID: ${documentReference.id}")
//                    }
//                    .addOnFailureListener { e ->
//                        Timber.d("Error adding document", e)
//                    }
//                viewModel.submitEnabled = true
//                viewModel.showSubmitProgress = false
//            }.addOnFailureListener {
//                viewModel.submitEnabled = true
//                viewModel.showSubmitProgress = false
//                Timber.d(it.message.toString())
//                Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
//            }
        }

        onRetrieveClick = {
            viewModel.retrieveEnabled = false
            db.collection("users").document("user1")
                .get()
                .addOnSuccessListener { result ->
                    val user = result.data?.toDataClass<User>()
                    Timber.d("$user")
                    viewModel.imagesUri = user?.images!!.map {
                        Uri.parse(it)
                    }
                    viewModel.retrieveEnabled = true
                }
                .addOnFailureListener { exception ->
                    Timber.d("Error getting documents.", exception)
                    viewModel.retrieveEnabled = true
                }
        }
    }

    fun copyStreamToFile(inputStream: InputStream, outputFile: File) {
        inputStream.use { input ->
            val outputStream = FileOutputStream(outputFile)
            outputStream.use { output ->
                val buffer = ByteArray(4 * 1024) // buffer size
                while (true) {
                    val byteCount = input.read(buffer)
                    if (byteCount < 0) break
                    output.write(buffer, 0, byteCount)
                }
                output.flush()
            }
        }
    }

    private fun createTmpFileFromUri(context: Context, uri: Uri, fileName: String): File? {
        return try {
            val stream = context.contentResolver.openInputStream(uri)
            val file = File.createTempFile(fileName, "", context.cacheDir)
            FileUtils.copyInputStreamToFile(stream, file)
            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
