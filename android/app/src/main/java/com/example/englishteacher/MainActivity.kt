package com.example.englishteacher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import kotlin.concurrent.thread

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var input: EditText
    private lateinit var output: TextView
    private lateinit var progress: TextView
    private lateinit var tts: TextToSpeech

    private var minutes = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        input = findViewById(R.id.input)
        output = findViewById(R.id.output)
        progress = findViewById(R.id.progress)

        tts = TextToSpeech(this, this)

        findViewById<Button>(R.id.speak).setOnClickListener {
            listen()
        }

        findViewById<Button>(R.id.ask).setOnClickListener {
            coach()
        }

        findViewById<Button>(R.id.listen).setOnClickListener {
            speakTeacher()
        }
    }

    // -----------------------------
    // MICROPHONE
    // -----------------------------

    private fun listen() {

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                10
            )

            return
        }

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_LANGUAGE,
            "en-IN"
        )

        intent.putExtra(
            RecognizerIntent.EXTRA_PROMPT,
            "Speak in English"
        )

        try {
            startActivityForResult(intent, 11)
        } catch (e: Exception) {

            Toast.makeText(
                this,
                "Speech recognition is not available.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        )

        if (
            requestCode == 11 &&
            resultCode == RESULT_OK
        ) {

            val result = data
                ?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )
                ?.firstOrNull()

            if (!result.isNullOrBlank()) {

                input.setText(result)

                coach()
            }
        }
    }

    // -----------------------------
    // AI ENGLISH TEACHER
    // -----------------------------

    private fun coach() {

        val sentence = input.text.toString().trim()

        if (sentence.isEmpty()) {

            Toast.makeText(
                this,
                "Please speak or type something first.",
                Toast.LENGTH_SHORT
            ).show()

            return
        }

        findViewById<Button>(R.id.ask).isEnabled = false
        findViewById<Button>(R.id.speak).isEnabled = false

        output.text = "🧑‍🏫 AI English Teacher\n\nAnalyzing your sentence..."

        thread {

            try {

                val apiKey = BuildConfig.GEMINI_API_KEY

                if (apiKey.isBlank()) {

                    runOnUiThread {

                        output.text =
                            "❌ Gemini API key is missing."

                        enableButtons()
                    }

                    return@thread
                }

                val prompt = """
You are my personal AI English Teacher.

I am an Indian IT professional learning spoken English and professional workplace communication.

Analyze the student's ACTUAL sentence below.

Student sentence:
"$sentence"

Give the answer using this structure:

🧑‍🏫 AI English Teacher

You said:
"$sentence"

✅ Correct English:
Give the grammatically correct sentence.

💡 Why:
Explain the grammar mistake simply.

🗣 Natural English:
Give a natural spoken-English version.

💼 Professional English:
Give a professional workplace version suitable for an IT professional.

📚 Grammar:
Explain the important grammar rule used in this sentence.

📖 Vocabulary:
Give 3 useful professional vocabulary words related to this topic.
For each word provide:
Meaning:
Example:

🔊 Pronunciation:
Give pronunciation help for one difficult word.

⭐ Fluency Score:
Give a score from 1 to 10.

🔁 Try Again:
Give one corrected sentence for me to repeat.

❓ Next Question:
Ask one natural follow-up question based on my actual sentence.

Important:
- Analyze my actual sentence.
- Do NOT give a generic response.
- Do NOT give the same vocabulary for every sentence.
- If my sentence is already correct, tell me it is correct.
- Focus on spoken English.
- Focus on professional IT communication.
- Keep explanations simple and practical.
""".trimIndent()

                val response =
                    callGemini(
                        apiKey,
                        prompt
                    )

                runOnUiThread {

                    if (response != null) {

                        minutes =
                            (minutes + 1).coerceAtMost(20)

                        progress.text =
                            "Today's lesson: $minutes/20 minutes"

                        output.text = response

                    } else {

                        output.text =
                            "❌ Gemini did not return a response."
                    }

                    enableButtons()
                }

            } catch (e: Exception) {

                runOnUiThread {

                    output.text =
                        "❌ Error connecting to Gemini.\n\n" +
                        e.message

                    enableButtons()
                }
            }
        }
    }

    // -----------------------------
    // GEMINI API
    // -----------------------------

    private fun callGemini(
        apiKey: String,
        prompt: String
    ): String? {

        val urlString =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.8-flash:generateContent"

        val url = URL(urlString)

        val connection =
            url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"

        connection.setRequestProperty(
            "Content-Type",
            "application/json"
        )

        connection.setRequestProperty(
            "x-goog-api-key",
            apiKey
        )

        connection.doOutput = true

        connection.connectTimeout = 30000
        connection.readTimeout = 60000

        val requestJson = JSONObject()

        val contents = JSONArray()

        val content = JSONObject()

        val parts = JSONArray()

        val textPart = JSONObject()

        textPart.put(
            "text",
            prompt
        )

        parts.put(textPart)

        content.put(
            "parts",
            parts
        )

        contents.put(content)

        requestJson.put(
            "contents",
            contents
        )

        connection.outputStream.use {

            it.write(
                requestJson
                    .toString()
                    .toByteArray(Charsets.UTF_8)
            )
        }

        val responseCode =
            connection.responseCode

        val stream =
            if (responseCode in 200..299) {

                connection.inputStream

            } else {

                connection.errorStream
            }

        val responseText =
            BufferedReader(
                InputStreamReader(stream)
            ).use {

                it.readText()
            }

        connection.disconnect()

        if (responseCode !in 200..299) {

            return "❌ Gemini API Error\n\n$responseText"
        }

        val json =
            JSONObject(responseText)

        val candidates =
            json.optJSONArray("candidates")

        if (
            candidates == null ||
            candidates.length() == 0
        ) {

            return "❌ Gemini returned no answer."
        }

        val firstCandidate =
            candidates.getJSONObject(0)

        val contentObject =
            firstCandidate.optJSONObject("content")

        val responseParts =
            contentObject?.optJSONArray("parts")

        if (
            responseParts == null ||
            responseParts.length() == 0
        ) {

            return "❌ Gemini returned an empty response."
        }

        return responseParts
            .getJSONObject(0)
            .optString("text")
    }

    // -----------------------------
    // TEACHER VOICE
    // -----------------------------

    private fun speakTeacher() {

        val text = output.text.toString()

        if (text.isBlank()) {
            return
        }

        tts.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "teacher"
        )
    }

    // -----------------------------
    // ENABLE BUTTONS
    // -----------------------------

    private fun enableButtons() {

        findViewById<Button>(R.id.ask).isEnabled = true
        findViewById<Button>(R.id.speak).isEnabled = true
    }

    // -----------------------------
    // TEXT TO SPEECH
    // -----------------------------

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            tts.setLanguage(
                Locale("en", "IN")
            )
        }
    }

    // -----------------------------
    // CLEANUP
    // -----------------------------

    override fun onDestroy() {

        tts.stop()
        tts.shutdown()

        super.onDestroy()
    }
}
