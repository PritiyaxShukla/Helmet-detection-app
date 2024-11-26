package com.example.helmet_detection_1

import UploadService
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import android.os.Environment

class MainActivity : AppCompatActivity() {

    lateinit var imgView: ImageView
    lateinit var btnUpload: Button
    lateinit var btnPredict: Button
    private var imageUri: Uri? = null

    private val contract = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            imageUri = uri
            imgView.setImageURI(uri)
            Log.d("PritiyaxShukla", "Image selected: $uri")
        } else {
            Log.e("PritiyaxShukla", "No image selected")
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check if the device is running Android 11 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Check if the app has MANAGE_EXTERNAL_STORAGE permission
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)

                // Check if the Intent can be handled by the device
                if (intent.resolveActivity(packageManager) != null) {
                    startActivityForResult(intent, REQUEST_CODE)
                } else {
                    Log.e("PritiyaxShukla", "No activity found to handle the permission request")
                    Toast.makeText(this, "Unable to request storage permission", Toast.LENGTH_SHORT).show()
                }
            } else {
                setup() // Permission granted, proceed with setting up the UI
            }
        } else {
            // For devices below Android 11, you can request the old storage permissions
            setup() // Proceed without requiring MANAGE_EXTERNAL_STORAGE permission
        }
    }

    private fun setup() {
        imgView = findViewById(R.id.imageView) // Replace with the actual ID of your ImageView
        btnUpload = findViewById(R.id.button) // Replace with the actual ID of your Button for uploading
        btnPredict = findViewById(R.id.button2) // Replace with the actual ID of your Button for prediction

        // Set onClickListener for Upload button
        btnUpload.setOnClickListener {
            try {
                contract.launch("image/*")
                Log.d("PritiyaxShukla", "Image picker launched")
            } catch (e: Exception) {
                Log.e("PritiyaxShukla", "Error launching image picker: ${e.message}")
                Toast.makeText(this, "Error launching image picker: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Set onClickListener for Predict button
        btnPredict.setOnClickListener {
            uploadAndPredict()
        }
    }

    private fun uploadAndPredict() {
        val uri = imageUri
        if (uri == null) {
            Log.e("PritiyaxShukla", "No image selected")
            Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
            return
        }

        // Record start time
        val startTime = System.currentTimeMillis()

        // Save the image to a temporary file
        val fileDir = applicationContext.filesDir
        val file = File(fileDir, "image.png")
        val inputStream = contentResolver.openInputStream(uri)
        val outputStream = FileOutputStream(file)
        inputStream?.use { it.copyTo(outputStream) }

        // Prepare the file for upload
        val requestBody = file.asRequestBody("image/*".toMediaTypeOrNull())
        val part = MultipartBody.Part.createFormData("file", file.name, requestBody)

        val httpClient = OkHttpClient.Builder()
            .addInterceptor(HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            })
            .connectTimeout(1200, java.util.concurrent.TimeUnit.SECONDS)  // Set connection timeout to 2 minutes
            .writeTimeout(1200, java.util.concurrent.TimeUnit.SECONDS)    // Set write timeout to 2 minutes
            .readTimeout(1200, java.util.concurrent.TimeUnit.SECONDS)     // Set read timeout to 2 minutes
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl("https://bike-helmet-detection-4vt9.onrender.com/") // Replace with your IP address
            .addConverterFactory(GsonConverterFactory.create())
            .client(httpClient) // Use the customized OkHttpClient
            .build()
            .create(UploadService::class.java)

        // Make the network call in a coroutine
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val response = retrofit.uploadImage(part)
                // Record end time after response is received
                val endTime = System.currentTimeMillis()

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.let {
                        // Save binary data as a file
                        val predictedImageFile = File(fileDir, "predicted_image.jpg")
                        val fos = FileOutputStream(predictedImageFile)
                        fos.use { it.write(responseBody.bytes()) }

                        // Generate a unique cache key by appending timestamp
                        val predictedImageUri = predictedImageFile.toURI().toString() + "?timestamp=${System.currentTimeMillis()}"

                        // Show the predicted image using Glide
                        runOnUiThread {
                            Glide.with(this@MainActivity)
                                .load(predictedImageUri) // Use URI with timestamp
                                .skipMemoryCache(true) // Skip Glide's memory cache
                                .into(imgView)

                            // Calculate time taken and show in Toast
                            val timeTaken = endTime - startTime
                            Toast.makeText(this@MainActivity, "Time Taken: $timeTaken ms", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Log.e("PritiyaxShukla", "Error: ${response.code()} - ${response.message()}")
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "Upload failed", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                Log.e("PritiyaxShukla", "Error: ${e.message}")
                runOnUiThread {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    companion object {
        private const val REQUEST_CODE = 1001
    }
}
