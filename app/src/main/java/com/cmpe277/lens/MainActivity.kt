package com.cmpe277.lens

import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import com.cmpe277.lens.databinding.ActivityMainBinding
import androidx.databinding.DataBindingUtil

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    lateinit var imageView: ImageView
    lateinit var editText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        imageView = binding.imageView
        editText = binding.editText
    }
    fun selectImage(v: View) {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(Intent.createChooser(intent, "Select Img"), 1)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1 && resultCode == androidx.appcompat.app.AppCompatActivity.RESULT_OK) {
            binding.imageView.setImageURI(data!!.data)

        }
    }

    fun startRecognizing(v: View) {
        if (imageView.drawable != null) {
            editText.setText("")
            v.isEnabled = false
            val bitmap = (imageView.drawable as BitmapDrawable).bitmap
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val detector = FirebaseVision.getInstance().onDeviceTextRecognizer

            detector.processImage(image)
                    .addOnSuccessListener { firebaseVisionText ->
                        v.isEnabled = true
                        processResultText(firebaseVisionText)
                    }
                    .addOnFailureListener {
                        v.isEnabled = true
                        editText.setText("Failed")
                    }
        } else {
            Toast.makeText(this, "Select an Image First", Toast.LENGTH_LONG).show()
        }

    }


    private fun processResultText(resultText: FirebaseVisionText) {
        if (resultText.textBlocks.size == 0) {
            editText.setText("No Text Found")
            return
        }
        for (block in resultText.textBlocks) {
            val blockText = block.text
            val blockConfidence = block.confidence
            val blockLanguages = block.recognizedLanguages
            val blockCornerPoints = block.cornerPoints
            val blockFrame = block.boundingBox
            for (line in block.lines) {
                val lineText = line.text
                val lineConfidence = line.confidence
                val lineLanguages = line.recognizedLanguages
                val lineCornerPoints = line.cornerPoints
                val lineFrame = line.boundingBox
                for (element in line.elements) {
                    val elementText = element.text
                    val elementConfidence = element.confidence
                    val elementLanguages = element.recognizedLanguages
                    val elementCornerPoints = element.cornerPoints
                    val elementFrame = element.boundingBox
                    editText.append(elementText + "\n")
                }
            }
        }
    }
}
