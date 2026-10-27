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
import java.util.Locale
class MainActivity:AppCompatActivity(),TextToSpeech.OnInitListener{
 private lateinit var input:EditText; private lateinit var output:TextView; private lateinit var progress:TextView; private lateinit var tts:TextToSpeech; private var minutes=0
 override fun onCreate(b:Bundle?){super.onCreate(b);setContentView(R.layout.activity_main);input=findViewById(R.id.input);output=findViewById(R.id.output);progress=findViewById(R.id.progress);tts=TextToSpeech(this,this);findViewById<Button>(R.id.speak).setOnClickListener{listen()};findViewById<Button>(R.id.ask).setOnClickListener{coach()};findViewById<Button>(R.id.listen).setOnClickListener{tts.speak(output.text.toString(),TextToSpeech.QUEUE_FLUSH,null,"teacher")}}
 private fun listen(){if(ContextCompat.checkSelfPermission(this,Manifest.permission.RECORD_AUDIO)!=PackageManager.PERMISSION_GRANTED){ActivityCompat.requestPermissions(this,arrayOf(Manifest.permission.RECORD_AUDIO),10);return};val i=Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);i.putExtra(RecognizerIntent.EXTRA_LANGUAGE,"en-IN");i.putExtra(RecognizerIntent.EXTRA_PROMPT,"Speak in English");startActivityForResult(i,11)}
 override fun onActivityResult(r:Int,c:Int,d:Intent?){super.onActivityResult(r,c,d);if(r==11&&c==RESULT_OK)d?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()?.let{input.setText(it);coach()}}
 private fun coach(){val s=input.text.toString().trim();if(s.isEmpty())return;minutes=(minutes+1).coerceAtMost(20);progress.text="Today's lesson: $minutes/20 minutes";output.text="""AI English Teacher

You said:
"$s"

Natural English:
Try to express the same idea in a complete, natural sentence.

Grammar:
• Check the verb tense against the time expression.
• Use Subject + Verb + Object when possible.
• Prefer natural workplace phrases instead of direct translation.

Vocabulary:
resolve — successfully fix a problem.
Example: "We resolved the production issue."

clarify — make something clear.
Example: "Could you clarify the requirement?"

Professional version:
"I worked on the task and resolved the issue."

Fluency score: 7/10

🔁 Repeat your answer using one new word.

Next question:
What was the main issue, and how did you resolve it?"""}
 override fun onInit(s:Int){if(s==TextToSpeech.SUCCESS)tts.language=Locale("en","IN")}
 override fun onDestroy(){tts.shutdown();super.onDestroy()}
}