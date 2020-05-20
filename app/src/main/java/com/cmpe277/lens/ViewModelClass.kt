package com.cmpe277.lens


import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.ml.naturallanguage.FirebaseNaturalLanguage
import com.google.firebase.ml.naturallanguage.languageid.FirebaseLanguageIdentificationOptions
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslateLanguage
import com.google.firebase.ml.naturallanguage.translate.FirebaseTranslatorOptions
import com.google.firebase.ml.vision.text.FirebaseVisionText

class ViewModelClass : ViewModel() {

    lateinit var finalText : StringBuilder
    var translateText = MutableLiveData<String>()
    var wholetranslate  = MutableLiveData<String>()
    var translationResultSet = Array<String>(100) { "" }

    lateinit var firebaseVisionText: FirebaseVisionText

    init{
        translateText.value = ""
    }

    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    fun identifyLanguage(inputText: String) {


        val languageIdentification = FirebaseNaturalLanguage
            .getInstance().getLanguageIdentification(
                FirebaseLanguageIdentificationOptions.Builder()
                    .setConfidenceThreshold(0.34f)
                    .build());

        languageIdentification
            .identifyLanguage(inputText)
            .addOnSuccessListener { s ->

                val sourceLangCode = FirebaseTranslateLanguage.languageForLanguageCode(s)!!

                val options = FirebaseTranslatorOptions.Builder()
                    .setSourceLanguage(sourceLangCode)
                    .setTargetLanguage(FirebaseTranslateLanguage.EN)
                    .build()

                val translator = FirebaseNaturalLanguage.getInstance().getTranslator(options)

                translator.downloadModelIfNeeded().addOnSuccessListener {
                    translator.translate(inputText)
                        .addOnSuccessListener { translatedText ->
                            translateText.value = translatedText
                        }
                        .addOnFailureListener { exception ->

                        }
                }
                    .addOnFailureListener {
                    }
            }
            .addOnFailureListener { e ->
                translateText.value = "error"
            }
    }




    fun processResultText(resultText: FirebaseVisionText) {
        if (resultText.textBlocks.size == 0) {
            return
        }
        translationResultSet = Array<String>(100) { "" }
        var result = StringBuilder()
        var data = StringBuilder()
        var lines = resultText.textBlocks.flatMap { it.lines }


        lines.forEach {
            data.append(it.text)
            data.append("\n")

        }

        val languageIdentification = FirebaseNaturalLanguage
            .getInstance().getLanguageIdentification(
                FirebaseLanguageIdentificationOptions.Builder()
                    .setConfidenceThreshold(0.34f)
                    .build())

        lines.forEachIndexed { index, line ->

            val text = line.text
            languageIdentification
                .identifyLanguage(text)
                .addOnSuccessListener { s ->
                    if (s == null || s == "und" || s == "en" || FirebaseTranslateLanguage.languageForLanguageCode(s) == null) {
                        translationResultSet[index] = text
//                        result.append("\n")
                    } else {
                        val sourceLangCode = FirebaseTranslateLanguage.languageForLanguageCode(s)!!

                        val options = FirebaseTranslatorOptions.Builder()
                            .setSourceLanguage(sourceLangCode)
                            .setTargetLanguage(FirebaseTranslateLanguage.EN)
                            .build()

                        val translator =
                            FirebaseNaturalLanguage.getInstance().getTranslator(options)

                        translator.downloadModelIfNeeded().addOnSuccessListener {
                            translator.translate(text)
                                .addOnSuccessListener { translatedText ->
                                    translationResultSet[index] = translatedText
                                }
                                .addOnFailureListener { exception ->
                                    println("onFail $exception")
                                }
                        }.addOnFailureListener {exception ->
                            println("onFail $exception")

                        }
                    }
                }
                .addOnFailureListener { exception ->
                    println("onFail $exception")

                }
        }



        finalText = data


    }

    fun onShowTranslate() {
        println("onShowTranslate : onclick : $translationResultSet")
        var result = StringBuilder()
        translationResultSet.forEach {
            result.append(it)
            result.append("\n")
        }
        wholetranslate.value = result.toString()
    }

    fun refresh() {
        wholetranslate.value = ""

    }

}
