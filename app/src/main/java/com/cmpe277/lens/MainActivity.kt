package com.cmpe277.lens


import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.google.firebase.auth.FirebaseAuth
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.method.LinkMovementMethod
import android.text.method.ScrollingMovementMethod
import android.text.style.ClickableSpan
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import com.cmpe277.lens.databinding.ActivityMainBinding
import com.firebase.ui.auth.AuthUI
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val RC_SIGN_IN = 123
    private val ODT_PERMISSION_REQUEST = 1234
    private val ODT_REQUEST_IMAGE_CAPTURE = 12345

    var outputFileUri: Uri? = null

    private lateinit var binding : ActivityMainBinding
    lateinit var imageView: ImageView
    lateinit var editText: EditText
//    lateinit var logoutBtn: Button
    lateinit var cameraBtn: Button
    lateinit var findTextBtn: Button
    lateinit var toggle: ActionBarDrawerToggle
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
        binding.navView.setNavigationItemSelectedListener {
            when(it.itemId) {
            R.id.logout_btn1 -> logout()
            }
            true
        }
        imageView = binding.imageView
        editText = binding.editText
        findTextBtn = binding.findtextBtn
//        logoutBtn = binding.logoutBtn
        cameraBtn = binding.cameraBtn

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
                spannableString.setSpan(
                    OnClickListener(
                        this,
                        spannableString.substring(start, end)
                    ), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )
                start = end + 1
                continue
            }
            end++
        }

        editText.text = spannableString
        editText.movementMethod = LinkMovementMethod.getInstance()
        editText.movementMethod = ScrollingMovementMethod.getInstance()

    }

    companion object {
        const val TAG = "MainActivity"
    }
}
