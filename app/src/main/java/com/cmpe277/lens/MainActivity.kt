package com.cmpe277.lens

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Bundle
import android.provider.AlarmClock
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ClickableSpan
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

    private class OnClickListener(val context: Context, val text: String) : ClickableSpan() {

        override fun onClick(widget: View) {
            val intent = Intent(context, WebviewActivity::class.java)
            intent.putExtra(AlarmClock.EXTRA_MESSAGE, text)
            context.startActivity(intent)
        }
    }

    private fun processResultText(resultText: FirebaseVisionText) {
        if (resultText.textBlocks.size == 0) {
            editText.setText("No Text Found")
            return
        }

        var data = StringBuilder()
        var lines = resultText.textBlocks.flatMap { it.lines }
        lines.forEach {
            data.append(it.text)
            data.append("\n")
        }

        val spannableString = SpannableStringBuilder(data)
        println("$TAG data = $spannableString")


        val toCharArray = data.toString().toCharArray()
        var start = 0
        var end = 0
        for (i in toCharArray.indices) {
            if (toCharArray[i] == '\n') {
                end = i
                println("$TAG spanned start  = $start, spanned end = $end")
                spannableString.setSpan(OnClickListener(this, spannableString.substring(start,end)), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                start = end + 1
                continue
            }
            end++
        }

        editText.text = spannableString
        editText.movementMethod = LinkMovementMethod.getInstance()
        editText.movementMethod = ScrollingMovementMethod.getInstance()


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
                   // editText.append(elementText + "\n")
                }
            }
        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
