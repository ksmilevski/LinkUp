package mk.ukim.finki.linkup

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import mk.ukim.finki.linkup.utils.FirebaseUtil


class SplashActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        //po 3 sekundi se kreira intent koj go otvara drugiot ekran
        //i avtomatski od ovoj ekran se prefrla na drugiot
        Handler(Looper.getMainLooper()).postDelayed({
            if(FirebaseUtil.isLoggedIn()){
                startActivity(Intent(this, MainActivity::class.java))

            }else {
                startActivity(Intent(this, LoginPhoneNumberActivity::class.java))
            }
            finish()
        }, 1000)
    }
}