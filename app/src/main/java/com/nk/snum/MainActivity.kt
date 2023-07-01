package com.nk.snum

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.os.Build
import android.os.Bundle
import android.telephony.SubscriptionManager
import android.util.Log
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.GetPhoneNumberHintIntentRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.material.snackbar.Snackbar
import com.nk.snum.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.getNumber.setOnClickListener {
            val request: GetPhoneNumberHintIntentRequest = GetPhoneNumberHintIntentRequest.builder().build()
            Identity.getSignInClient(this)
                .getPhoneNumberHintIntent(request)
                .addOnSuccessListener { result: PendingIntent ->
                    try {
                        phoneNumberHintIntentResultLauncher.launch(IntentSenderRequest.Builder(result).build())
                    } catch (e: Exception) {
                        showMessage("Launching the PendingIntent failed")
                    }
                }
                .addOnFailureListener {
                    Identity.getSignInClient(this)
                        .beginSignIn(BeginSignInRequest.builder().build())
                        .addOnSuccessListener {
                            phoneNumberHintIntentResultLauncher.launch(IntentSenderRequest.Builder(it.pendingIntent).build())
                        }
                    showMessage("Phone Number Hint failed")
                }
        }

        binding.getSimNumber.setOnClickListener {
            val subscriptionManager = getSystemService(TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
            val subsInfoList = subscriptionManager.activeSubscriptionInfoList
            val number = StringBuilder()
            for (subscriptionInfo in subsInfoList) {
                val v:String
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    v= subscriptionManager.getPhoneNumber(subscriptionInfo.subscriptionId)
                    number.append(v).append(", ")
                } else {
                    v = subscriptionInfo.number
                    number.append(v).append(", ")
                }
                Log.d("Test", " Number is  $v, ${subscriptionInfo.subscriptionId}, ${subscriptionInfo.displayName}, ${subscriptionInfo.carrierName}")
            }
            binding.displayNumber.text = number.toString()
        }
    }

    private val phoneNumberHintIntentResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            try {
                val phoneNumber = Identity.getSignInClient(this).getPhoneNumberFromIntent(result.data)
                binding.displayNumber.text = phoneNumber
            } catch (e: Exception) {
                showMessage("Phone Number Hint failed")
            }
        }


    private fun showMessage(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_SHORT).show()
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}