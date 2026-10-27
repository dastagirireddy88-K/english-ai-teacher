package com.example.englishteacher

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var input: EditText
    private lateinit var output: TextView
    private lateinit var progress: TextView

    private var tts: TextToSpeech? = null
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
            speakOutput()
        }
    }

    private fun listen() {

        if (ContextCompat.checkSelfPermission(
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

        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)

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
                "Speech recognition is not available on this phone.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 11 && resultCode == RESULT_OK) {

            val result = data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()

            if (!result.isNullOrBlank()) {
                input.setText(result)
                coach()
            }
        }
    }

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

        minutes = (minutes + 1).coerceAtMost(20)

        progress.text = "Today's lesson: $minutes/20 minutes"

        output.text = """
AI English Teacher

You said:
"$sentence"

Natural English:
Try to express the same idea in a complete, natural sentence.

Grammar:
• Check the verb tense against the time expression.
• Use Subject + Verb + Object when possible.
• Prefer natural workplace phrases instead of direct translation.

Vocabulary:

resolve — successfully fix a problem.

Example:
"We resolved the production issue."

clarify — make something clear.

Example:
"Could you clarify the requirement?"

Professional version:

"I worked on the task and resolved the issue."

Fluency score: 7/10

🔁 Repeat your answer using one new word.

Next question:

What was the main issue, and how did you resolve it?
        """.trimIndent()
    }

    private fun speakOutput() {

        val text = output.text.toString()

        if (text.isBlank()) {
            return
        }

        val engine = tts

        if (engine == null) {
            Toast.makeText(
                this,
                "Teacher voice is not ready yet.",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        engine.speak(
            text,
            TextToSpeech.QUEUE_FLUSH,
            null,
            "teacher"
        )
    }

    override fun onInit(status: Int) {

        if (status == TextToSpeech.SUCCESS) {

            val result = tts?.setLanguage(
                Locale("en", "IN")
            )

            if (result == TextToSpeech.LANG_MISSING_DATA ||
                result == TextToSpeech.LANG_NOT_SUPPORTED
            ) {
                Toast.makeText(
                    this,
                    "English voice is not available.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    override fun onDestroy() {

        tts?.stop()
        tts?.shutdown()
        tts = null

        super.onDestroy()
    }
}
