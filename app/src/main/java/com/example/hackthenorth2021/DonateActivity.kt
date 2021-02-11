package com.example.hackthenorth2021

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_donate.*
import kotlinx.android.synthetic.main.activity_main.*
import okhttp3.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class DonateActivity: AppCompatActivity() {
    val REQUEST_IMAGE_CAPTURE = 1
    lateinit var currentPhotoPath: String
    lateinit var label: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)
        setHiddenGroupVisibility(GONE)

        donateCapture.setOnClickListener{
            dispatchTakePictureIntent()
        }
        donateCapture2.setOnClickListener{
            dispatchTakePictureIntent()
        }
        donateCapture3.setOnClickListener{
            dispatchTakePictureIntent()
        }

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.navigation_bar)
        bottomNavigationView.selectedItemId = R.id.donate

        bottomNavigationView.setOnNavigationItemSelectedListener(BottomNavigationView.OnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.home -> {
                    startActivity(Intent(applicationContext, HomeActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.search -> {
                    startActivity(Intent(applicationContext, LocationActivity::class.java))
                    overridePendingTransition(0, 0)
                    return@OnNavigationItemSelectedListener true
                }
                R.id.donate -> return@OnNavigationItemSelectedListener true
            }
            true
        })

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {

//            label = getLabel(currentPhotoPath)
//
//            donateEditLabel.text = Editable.Factory.getInstance().newEditable(label)
            setPic()
            setHiddenGroupVisibility(VISIBLE)
        }
    }

    private fun setHiddenGroupVisibility(visibility: Int) {
        donateThumbnail.visibility = visibility
        donateLabelLabel.visibility = visibility
        donateSuggestedLabel.visibility = visibility
        donateDescriptionLabel.visibility = visibility
        donateEditLabel.visibility = visibility
        donateEditDescription.visibility = visibility
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {


        // Create an image file name
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: throw IOException("Could not get public storage dir")

        return File.createTempFile(
            "JPEG_${timeStamp}_", /* prefix */
            ".jpg", /* suffix */
            storageDir /* directory */
        ).apply {
            // Save a file: path for use with ACTION_VIEW intents
            currentPhotoPath = absolutePath
        }

    }

    private fun setPic() {
        // Get the dimensions of the View
        val targetW: Int = 300
        val targetH: Int = 300

        val bmOptions = BitmapFactory.Options().apply {
            // Get the dimensions of the bitmap
            inJustDecodeBounds = true

            val photoW: Int = outWidth
            val photoH: Int = outHeight

            // Determine how much to scale down the image
            val scaleFactor: Int = Math.max(1, Math.min(photoW / targetW, photoH / targetH))

            // Decode the image file into a Bitmap sized to fill the View
            inJustDecodeBounds = false
            inSampleSize = scaleFactor
            inPurgeable = true
        }
        BitmapFactory.decodeFile(currentPhotoPath, bmOptions)?.also { bitmap ->
            donateThumbnail.setImageBitmap(bitmap)
        }
    }


    private fun getLabel(filepath: String): String {
        val url = "https://api.htngiftbox.online/label"

        val client = OkHttpClient().newBuilder()
            .build()
        val mediaType: MediaType? = MediaType.parse("text/plain")
        val body: RequestBody = MultipartBody.Builder().setType(MultipartBody.FORM)
            .addFormDataPart(
                "image", filepath,
                RequestBody.create(
                    MediaType.parse("application/octet-stream"),
                    File(filepath)
                )
            )
            .addFormDataPart("label", "bicycle")
            .build()
        val request: Request? = Request.Builder()
            .url(url)
            .method("POST", body)
            .build()
        val response: Response? = client.newCall(request).execute()

        return response.toString()
    }


    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            // Ensure that there's a camera activity to handle the intent
            takePictureIntent.resolveActivity(packageManager)?.also {
                // Create the File where the photo should go
                val photoFile: File? = try {
                    createImageFile()
                } catch (ex: IOException) {
                    // Error occurred while creating the File

                    null
                }
                // Continue only if the File was successfully created
                photoFile?.also {
                    val photoURI: Uri = FileProvider.getUriForFile(
                        this,
                        "com.example.hackthenorth2021.fileProvider",
                        it
                    )
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
                }
            }
        }

    }

}