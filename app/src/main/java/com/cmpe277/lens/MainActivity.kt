package com.cmpe277.lens


import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.DialogInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.speech.tts.TextToSpeech
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ClickableSpan
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import com.cmpe277.lens.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.google.android.material.navigation.NavigationView
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentificationOptions
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText
import java.util.*


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 123
    private val ODT_PERMISSION_REQUEST = 1234
    private val ODT_REQUEST_IMAGE_CAPTURE = 12345

    var outputFileUri: Uri? = null
    private lateinit var tts: TextToSpeech

    private lateinit var binding : ActivityMainBinding
    lateinit var imageView: ImageView
    lateinit var editText: EditText
    lateinit var editText2: EditText
//    lateinit var logoutBtn: Button
    lateinit var cameraBtn: Button
    lateinit var findTextBtn: Button
    lateinit var toggle: ActionBarDrawerToggle
    lateinit var navView: NavigationView
    lateinit var navUserName: TextView
    lateinit var navUserEmail: TextView
    lateinit var headerView : View
    lateinit var firebaseVisionText: FirebaseVisionText
    lateinit var translateBtn : Button


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        if (auth.currentUser == null) {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish()
        } else {
            Toast.makeText(this, "Welcome " + auth.currentUser!!.email, Toast.LENGTH_SHORT).show()
        }


        binding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        toggle = ActionBarDrawerToggle(this, binding.drawerlayout, R.string.open, R.string.close)
        binding.drawerlayout.addDrawerListener(toggle)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toggle.syncState()
        navView = binding.navView
        navView.setNavigationItemSelectedListener {
            when(it.itemId) {
            R.id.logout_btn1 -> logout()
            }
            true
        }
        imageView = binding.imageView
        editText2 = binding.editText2
        editText = binding.editText
        findTextBtn = binding.findtextBtn
        translateBtn = binding.translateBtn
//        logoutBtn = binding.logoutBtn
        cameraBtn = binding.cameraBtn
        updateNavHeader()



//        logoutBtn.setOnClickListener {
//            AuthUI.getInstance().signOut(this)
//                .addOnCompleteListener() {
//                    //Toast.makeText(this,"Goodbye "+auth.currentUser!!.email, Toast.LENGTH_SHORT).show()
//                    showSignInOptions()
//                }
//
//                .addOnFailureListener { e ->
//                    Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
//                }
//        }
//        val intent = Intent(this, LoginActivity::class.java)
//        startActivity(intent)
//        finish()
//    }



        cameraBtn.setOnClickListener {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (checkSelfPermission(Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_DENIED) {
                    checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)== PackageManager.PERMISSION_DENIED

                    val permission = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    requestPermissions(permission,ODT_PERMISSION_REQUEST)

                }
                else {
                    openCamera()
                }
            }
            else {
                openCamera()
            }
        }

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(toggle.onOptionsItemSelected(item)) {
            return true
        }
        return super.onOptionsItemSelected(item)
    }



    private fun logout() {
        AuthUI.getInstance().signOut(this)
            .addOnCompleteListener() {
                startActivity(Intent(this,LoginActivity::class.java))

            }

            .addOnFailureListener { e ->
                Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()

    }

    fun updateNavHeader() {
        headerView = navView.getHeaderView(0)
        navUserName = headerView.findViewById(R.id.nav_username)
        navUserEmail = headerView.findViewById(R.id.nav_useremail)
        navUserName.setText(auth.currentUser?.displayName)
        navUserEmail.setText(auth.currentUser?.email)

    }


    private fun openCamera() {

        //val packageManager = activity!!.packageManager
        val takePhotoIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (takePhotoIntent.resolveActivity(packageManager) != null) {
            val values = ContentValues()
            values.put(MediaStore.Images.Media.TITLE, "")
            outputFileUri = contentResolver
                .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)!!
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri)
            startActivityForResult(takePhotoIntent, ODT_REQUEST_IMAGE_CAPTURE)
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray
        ) {
            //super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when(requestCode) {
            ODT_PERMISSION_REQUEST -> {
                if(grantResults.size > 0 && grantResults[0]== PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
                else {
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
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
            findTextBtn.isEnabled = true


        } else if(resultCode== Activity.RESULT_OK) {
            binding.imageView.setImageURI(outputFileUri)
            findTextBtn.isEnabled = true
        }


    }



    fun startRecognizing(v: View) {
        if(v == null) {
            Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show()
        }
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


    private class OnClickListener(val context: Context, val text: String, val tts: TextToSpeech,var editText2: EditText,var editText: EditText) :
        ClickableSpan() {

        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        override fun onClick(widget: View) {
            println("$TAG onclick called")

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
                .getInstance().getLanguageIdentification(
                    FirebaseLanguageIdentificationOptions.Builder()
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
                                    editText.visibility = EditText.GONE
                                    editText2.visibility = EditText.VISIBLE
                                    editText2.setText(translatedText);
                                    //Toast.makeText(context, translatedText, Toast.LENGTH_LONG).show()
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



        print("$TAG data block = " + resultText.text.length)
        print("$TAG data block = " + resultText.text.replace("\\s+", " "))


        var result = StringBuilder()
        var data = StringBuilder()
        var lines = resultText.textBlocks.flatMap { it.lines }
        ///////////

        lines.forEach {
            data.append(it.text)
            data.append("\n")

        }

        val languageIdentification = FirebaseNaturalLanguage
            .getInstance().getLanguageIdentification(
                FirebaseLanguageIdentificationOptions.Builder()
                    .setConfidenceThreshold(0.34f)
                    .build())

        lines.forEach {

            val text = it.text
            languageIdentification
                .identifyLanguage(text)
                .addOnSuccessListener { s ->
                    println("$TAG, text = $text , language = $s")
                    if (s == null || s == "und" || s == "en" || FirebaseTranslateLanguage.languageForLanguageCode(s) == null) {
                        result.append(text)
                        result.append("\n")
                    } else {
                        val sourceLangCode = FirebaseTranslateLanguage.languageForLanguageCode(s)!!

                        val options = FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(sourceLangCode)
                            .setTargetLanguage(FirebaseTranslateLanguage.EN)
                            .build()

                        val translator =
                            FirebaseNaturalLanguage.getInstance().getTranslator(options)

                        translator.downloadModelIfNeeded().addOnSuccessListener {
                            println("$TAG, model downloaded ")
                            translator.translate(text)
                                .addOnSuccessListener { translatedText ->
                                    println("$TAG, translated text = $translatedText")
                                    result.append(translatedText)
                                    result.append("\n")
                                }
                                .addOnFailureListener { exception ->
                                    println("$TAG, translated text failed $exception")
                                }
                        }.addOnFailureListener {
                            println("$TAG, model download exception $it")
                        }
                    }
                }
                .addOnFailureListener { e ->
                    println("$TAG, Language identification error : " + e.printStackTrace())
                }
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
                spannableString.setSpan(OnClickListener(this, spannableString.substring(start,end), tts, editText2,editText), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
                start = end + 1
                continue
            }
            end++
        }
        editText2.visibility = EditText.GONE
        editText.visibility = EditText.VISIBLE
        editText.text = spannableString
        editText.movementMethod = LinkMovementMethod.getInstance()

        translateBtn.setOnClickListener{
            editText.visibility = EditText.GONE
            editText2.visibility = EditText.VISIBLE
            editText2.setText(result)


        }



//        editText.movementMethod = ScrollingMovementMethod.getInstance()

    }

    companion object {
        const val TAG = "MainActivity"
    }
}
