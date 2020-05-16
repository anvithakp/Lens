package com.cmpe277.lens

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.provider.AlarmClock
import android.speech.tts.TextToSpeech
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ClickableSpan
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.cmpe277.lens.databinding.ActivityMainBinding
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentificationOptions
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import java.util.*


class MainActivity : AppCompatActivity() {
    private lateinit var tts: TextToSpeech
    private lateinit var binding : ActivityMainBinding
    lateinit var imageView: ImageView
    lateinit var editText: EditText



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_main)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)

        imageView = binding.imageView
        editText = binding.editText

        var listener = TextToSpeech.OnInitListener {
            if (it == TextToSpeech.SUCCESS) {
                println("$TAG: Text to speech engine started successfully.")
                tts.language = Locale.US
            } else {
                println("$TAG: Error starting the text to speech engine.")
            }

        }

    tts = TextToSpeech(this.applicationContext, listener)

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

    private class OnClickListener(val context: Context, val text: String, val tts: TextToSpeech) :
        ClickableSpan() {

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onClick(widget: View) {
            println("$TAG onclick called")
            Toast.makeText(context, "option 0", Toast.LENGTH_SHORT).show()

            val builder = AlertDialog.Builder(context)
            builder.setTitle("Choose an option : ")
            val options = arrayOf("Search Web", "Speak out loud", "Translate")
            builder.setItems(options, DialogInterface.OnClickListener { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(context, WebviewActivity::class.java)
                        intent.putExtra(AlarmClock.EXTRA_MESSAGE, text)
                        context.startActivity(intent)
                    }
                    1 -> tts.speak(text, TextToSpeech.QUEUE_ADD, null, "DEFAULT")
                    2 -> identifyLanguage(text)

                }

            })
            val dialog = builder.create()
            dialog.show()


        }

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        private fun identifyLanguage(inputText: String) {
            val languageIdentification = FirebaseNaturalLanguage
                .getInstance().getLanguageIdentification(FirebaseLanguageIdentificationOptions.Builder()
                    .setConfidenceThreshold(0.34f)
                    .build());

            languageIdentification
                .identifyLanguage(inputText)
                .addOnSuccessListener { s ->
                    println("$TAG, text = $inputText , language = $s")
                    val sourceLangCode = FirebaseTranslateLanguage.languageForLanguageCode(s)!!

                    val options = FirebaseTranslatorOptions.Builder()
                        .setSourceLanguage(sourceLangCode)
                        .setTargetLanguage(FirebaseTranslateLanguage.EN)
                        .build()

                    val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)

                        translator.downloadModelIfNeeded().addOnSuccessListener {
                            println("$TAG, model downloaded ")
                            translator.translate(inputText)
                                .addOnSuccessListener { translatedText ->
                                    println("$TAG, translated text = $translatedText")
                                    Toast.makeText(context, translatedText, Toast.LENGTH_LONG).show()
                                    tts.speak(translatedText, TextToSpeech.QUEUE_ADD, null, "DEFAULT")
                                }
                                .addOnFailureListener { exception ->
                                    println("$TAG, translated text failed $exception")
                                }
                        }.addOnFailureListener {
                            println("$TAG, model download exception $it")
                        }

                }
                .addOnFailureListener { e ->
                                println("$TAG, Language identification error : " + e.printStackTrace())
                                Toast.makeText(context, "error", Toast.LENGTH_SHORT).show()
                            }
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
                spannableString.setSpan(OnClickListener(this, spannableString.substring(start,end), tts), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                start = end + 1
                continue
            }
            end++
        }

        editText.text = spannableString
        editText.movementMethod = LinkMovementMethod.getInstance()
//        editText.movementMethod = ScrollingMovementMethod.getInstance()


//        for (block in resultText.textBlocks) {
//            val blockText = block.text
//            val blockConfidence = block.confidence
//            val blockLanguages = block.recognizedLanguages
//            val blockCornerPoints = block.cornerPoints
//            val blockFrame = block.boundingBox
//            for (line in block.lines) {
//                val lineText = line.text
//                val lineConfidence = line.confidence
//                val lineLanguages = line.recognizedLanguages
//                val lineCornerPoints = line.cornerPoints
//                val lineFrame = line.boundingBox
//                for (element in line.elements) {
//                    val elementText = element.text
//                    val elementConfidence = element.confidence
//                    val elementLanguages = element.recognizedLanguages
//                    val elementCornerPoints = element.cornerPoints
//                    val elementFrame = element.boundingBox
//                   // editText.append(elementText + "\n")
//                }
//            }
//        }
    }

    companion object {
        const val TAG = "MainActivity"
    }
}
