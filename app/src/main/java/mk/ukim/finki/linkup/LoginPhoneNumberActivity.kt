package mk.ukim.finki.linkup

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.hbb20.CountryCodePicker

class LoginPhoneNumberActivity : AppCompatActivity() {

    private lateinit var phoneInput: EditText
    private lateinit var countryCodePicker: CountryCodePicker
    private lateinit var sendOtpBtn: Button
    private lateinit var progressBar: ProgressBar

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_phone_number)


        phoneInput = findViewById(R.id.login_mobile_number)
        countryCodePicker = findViewById(R.id.login_countrycode)
        sendOtpBtn = findViewById(R.id.send_otp_btn)
        progressBar = findViewById(R.id.login_progress_bar)

        progressBar.visibility = View.GONE; //da ne se gleda progressBar-ot

        //za da se povrze phoneInput so countryCodePicker
        countryCodePicker.registerCarrierNumberEditText(phoneInput) //prefiksot na drzavata avtomatski se dodava, a korisnikot go vnesuva brojot
        sendOtpBtn.setOnClickListener{
            if (!countryCodePicker.isValidFullNumber) {
                phoneInput.error = getString(R.string.error_phone_invalid)
                return@setOnClickListener //ako ne e validen brojot, intentot ne se kreira, zavrshuva tuka
            }

            //intent za da se premine na drug ekran i vo extras da se dodade tel broj
            val intent = Intent(this, LoginOtpActivity::class.java)
            intent.putExtra("phone", countryCodePicker.fullNumberWithPlus)
            startActivity(intent);
        }


//        val rootView = findViewById<LinearLayout>(R.id.root_layout) // Осигурај се дека ID-то постои во XML
//        rootView?.let {
//            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
//                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//                insets
//            }
//        }

    }
}