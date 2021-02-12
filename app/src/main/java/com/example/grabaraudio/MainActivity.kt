package com.example.grabaraudio

import android.Manifest
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.media.MediaRecorder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.IOException

private val LOG_TAG = "AudioRecordTest"
private val REQUEST_GRABAR=1

class MainActivity : AppCompatActivity() {
    var fileName: String = ""
    var recorder: MediaRecorder? = null
    var player: MediaPlayer? = null

    lateinit var bGraba: Button
    lateinit var bPara: Button
    lateinit var bReproduce: Button

    // Requesting permission to RECORD_AUDIO
    var permissionToRecordAccepted = false
    var permissions: Array<String> = arrayOf(Manifest.permission.RECORD_AUDIO)
    private val AUDIO_RECORDER_FOLDER = "AudioRecorder"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bGraba = findViewById<View>(R.id.main_bGraba) as Button
        bReproduce = findViewById<View>(R.id.main_bReproduce) as Button
        bPara = findViewById<View>(R.id.main_bPara) as Button

        // Record to the external cache directory for visibility
        //fileName = "${externalCacheDir?.absolutePath}/audiorecordtest.3gp"
        fileName=getFilename()

        ActivityCompat.requestPermissions(this, permissions, REQUEST_GRABAR)

        if(permissionToRecordAccepted){
            bGraba.setOnClickListener {grabar() }
            bPara.setOnClickListener { parar() }
            bReproduce.setOnClickListener { reproducir() }
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        permissionToRecordAccepted = if (requestCode == REQUEST_GRABAR) {
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        } else {
            false
        }
        if (!permissionToRecordAccepted) Toast.makeText(this,"No puedes grabar si no aceptas los permisos",Toast.LENGTH_LONG).show()
    }


    private fun grabar() {
        recorder = MediaRecorder()
        recorder?.setAudioSource(MediaRecorder.AudioSource.MIC)
        recorder?.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        recorder?.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB)
        recorder?.setOutputFile(fileName)
        //recorder?.setOnErrorListener(errorListener)
        //recorder?.setOnInfoListener(infoListener)
        try {
            recorder?.prepare()
            recorder?.start()
            bGraba.isEnabled = false
            bPara.isEnabled = true
            bReproduce.isEnabled=false
        } catch (e: IllegalStateException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun getFilename(): String {
        val filepath = this.getExternalFilesDir(null)?.getAbsolutePath();
        val file = File(filepath, AUDIO_RECORDER_FOLDER)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath + "/" + System.currentTimeMillis() + ".mp4"
    }

    private fun parar() {
        if (recorder!=null) {
            recorder?.stop()
            recorder?.reset()
            recorder?.release()
            recorder = null
            bPara.isEnabled = false
            bGraba.isEnabled = true
            bReproduce.isEnabled = true
        }
    }


    private fun reproducir() {
        if (!player?.isPlaying!!) {
            try {
                player = MediaPlayer()
                player?.setDataSource(fileName)
                player?.prepare()
                player?.setOnCompletionListener { bReproduce.text = "Reproduce" }
                player?.start()
                bReproduce.text = "Pausa"
            } catch (e: IOException) {
                Log.e(LOG_TAG, "prepare() failed")
            }
        } else {
            player?.pause()
            bReproduce.text = "Continua"
        }
    }

}