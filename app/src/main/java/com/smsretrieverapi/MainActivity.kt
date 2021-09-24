package com.smsretrieverapi

import android.content.Intent
import android.content.IntentFilter
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.smsretrieverapi.databinding.ActivityMainBinding
import com.smsretrieverapi.receiver.SmsReceiver
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var smsReceiver: SmsReceiver? = null
    private val otpSmsRegex = "(|^)\\d{4}"
    private val otpResponseCode = 52
    private val otpSmsTitle = "SMSAPI"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        init()
    }

    private fun init() {
        val client = SmsRetriever.getClient(this)
        client.startSmsUserConsent(otpSmsTitle)
    }

    private fun registerBroadcastReceiver() {
        smsReceiver = SmsReceiver()
        smsReceiver?.smsBroadcastReceiverListener = object :
            SmsReceiver.SmsBroadcastReceiverListener {
            override fun onSuccess(intent: Intent?) {

                startActivityForResult(intent, otpResponseCode)
            }
            override fun onFailure() {

            }
        }

        val intentFilter = IntentFilter(SmsRetriever.SMS_RETRIEVED_ACTION)
        registerReceiver(smsReceiver, intentFilter)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        try {
            if (requestCode == otpResponseCode && resultCode == RESULT_OK && data != null) {
                val message = data.getStringExtra(SmsRetriever.EXTRA_SMS_MESSAGE)
                getOtpFromMessage(message)
            }
        } catch (ex: Exception) {

        }
    }

    private fun getOtpFromMessage(message: String?) {
        val otpPattern = Pattern.compile(otpSmsRegex)
        val matcher = otpPattern.matcher(message)

        if (matcher.find()) {
            val otp = matcher.group()
            try {
                binding.otpCode1.setText(otp.substring(0,1))
                binding.otpCode2.setText(otp.substring(1,2))
                binding.otpCode3.setText(otp.substring(2,3))
                binding.otpCode4.setText(otp.substring(3,4))
            } catch (ex: Exception) {

            }
        }
    }

    override fun onStart() {
        super.onStart()
        registerBroadcastReceiver()
    }

    override fun onStop() {
        super.onStop()
        unregisterReceiver(smsReceiver)
    }
}