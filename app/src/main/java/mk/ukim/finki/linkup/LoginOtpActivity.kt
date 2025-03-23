package mk.ukim.finki.linkup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import mk.ukim.finki.linkup.utils.AndroidUtil
import java.util.Timer
import java.util.TimerTask
import java.util.concurrent.TimeUnit

class LoginOtpActivity : AppCompatActivity() {

    private var phoneNumber: String? = null
    private var timeoutSeconds: Long = 60L
    private var verificationCode: String? = null
    private var resendingToken: PhoneAuthProvider.ForceResendingToken? = null

    private lateinit var otpInput: EditText
    private lateinit var nextBtn: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var resendOtpTextView: TextView
    val mAuth: FirebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_otp)

        otpInput = findViewById(R.id.login_otp)
        nextBtn = findViewById(R.id.login_next_btn)
        progressBar = findViewById(R.id.login_progress_bar)
        resendOtpTextView = findViewById(R.id.resend_otp_textview)

        phoneNumber = intent.getStringExtra("phone")

        //ova e isto so sendOtp(phoneNumber, false) samo e null safety da ne frli greska ako pN e null
        phoneNumber?.let { sendOtp(it, false) }

        nextBtn.setOnClickListener {
            val enteredOtp = otpInput.text.toString()
            //kreira PhoneAuthCredential objekt koj se koristi za potvrduvanje na tel broj na korisnikot koristejki otp
            //verCode e kodot sho firebase go ispratil, enteredOtp e toa nie sho sme go vnele
            val credential = PhoneAuthProvider.getCredential(verificationCode!!, enteredOtp)
            signIn(credential)
            setInProgress(true)
        }

        resendOtpTextView.setOnClickListener {
            phoneNumber?.let { sendOtp(it, true) }
        }

    }

    //funkcija koja isprakja otp - one time pass na  tel broj preku firebase auth
    fun sendOtp(phoneNumber: String, isResend: Boolean) {
        startResendTimer()
        setInProgress(true)

        val optionsBuilder = PhoneAuthOptions.newBuilder(mAuth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(timeoutSeconds, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                //avtomatska verifikacija
                override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {
                    signIn(phoneAuthCredential)
                    setInProgress(false)
                }

                //ako ima greska (pogresen br, prob so internet, firebase ako ne e aktiviran
                override fun onVerificationFailed(e: FirebaseException) {
                    AndroidUtil.showToast(applicationContext, "Otp verification failed")
                    Log.e("OTP_ERROR", "Verification failed", e)
                    setInProgress(false)
                }

                //koga kodot e praten se zacuvuva verId koj ke treba pri vnes na otp i zacuvuva FRT za podocna ako korisnikot saka da dobie nov kod
                override fun onCodeSent(
                    verificationId: String,
                    forceResentingToken: PhoneAuthProvider.ForceResendingToken
                ) {
                    super.onCodeSent(verificationId, forceResentingToken)
                    verificationCode = verificationId
                    resendingToken = forceResentingToken
                    AndroidUtil.showToast(applicationContext, "Otp sent successfully")
                    setInProgress(false)
                }
            })

        if (isResend && resendingToken != null) {
            optionsBuilder.setForceResendingToken(resendingToken!!)
        }

        val options = optionsBuilder.build()
        PhoneAuthProvider.verifyPhoneNumber(options)

    }

    //gi menuva progressBar i kopceto zavisno dali procesot e vo tek
    fun setInProgress(inProgress: Boolean) {
        if (inProgress) {
            progressBar.visibility = View.VISIBLE
            nextBtn.visibility = View.GONE
        } else {
            progressBar.visibility = View.GONE
            nextBtn.visibility = View.VISIBLE
        }
    }

    //go koristi dobieniot otp za da go najavi korisnikot vo firebase
    fun signIn(phoneAuthCredential: PhoneAuthCredential) {
        setInProgress(true)
        mAuth.signInWithCredential(phoneAuthCredential).addOnCompleteListener { task ->
            setInProgress(false)
            if (task.isSuccessful) {
                AndroidUtil.showToast(applicationContext, "intent to next act")


                //ako e uspesno odi na drug ekran
                val intent = Intent(this, LoginUsernameActivity::class.java)
                intent.putExtra("phone", phoneNumber)
                startActivity(intent)
                finish()
            }
//            else {
//                AndroidUtil.showToast(applicationContext, "OTP verification failed")
//            }
        }
    }

//    fun startResendTimer() {
//        resendOtpTextView.isEnabled = false // За да не може корисникот да кликне на почеток
//        val timer = Timer()
//        timer.scheduleAtFixedRate(object : TimerTask() {
//            override fun run() {
//                timeoutSeconds--
//                runOnUiThread {
//                    resendOtpTextView.text = "Resend OTP in $timeoutSeconds seconds"
//                }
//                if (timeoutSeconds <= 0) {
//                    timeoutSeconds = 60L
//                    timer.cancel()
//                    runOnUiThread {
//                        resendOtpTextView.isEnabled = true
//                    }
//                }
//            }
//        }, 0, 1000)
//    }


    //handler koj raboti na glavniot thread na aplikacijata
    //Looper.getMainLooper osiguruva deka kodot sto ke go izvrsuva ovoj handler ke se sluci na ui thread
    private val handler = Handler(Looper.getMainLooper())

    private val resendRunnable = object : Runnable {
        override fun run() {
            //ako e pogolemo od 0 go azurira textview i se namaluvaat sek
            if (timeoutSeconds > 0) {
                resendOtpTextView.text = "Resend OTP in $timeoutSeconds seconds"
                timeoutSeconds--
                handler.postDelayed(this, 1000)
            } else {
                //ako e 0 go ovozmozuva kopceto za resend i ja vrakja vrednosta na sek
                resendOtpTextView.isEnabled = true
                resendOtpTextView.text = "Resend OTP"
                timeoutSeconds = 60L
            }
        }
    }

    //se povikuva koga korisnikot ke pobara nov otp
    fun startResendTimer() {
        resendOtpTextView.isEnabled = false
        timeoutSeconds = 60L
        handler.post(resendRunnable)
    }

}