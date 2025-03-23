package mk.ukim.finki.linkup

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var searchButton: ImageButton
    private lateinit var chatFragment: ChatFragment
    private lateinit var profileFragment: ProfileFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        chatFragment = ChatFragment()
        profileFragment = ProfileFragment()

        bottomNavigationView = findViewById(R.id.bottom_navigation)
        searchButton = findViewById(R.id.main_searchButton)

        searchButton.setOnClickListener {
            startActivity(Intent(this, SearchUserActivity::class.java))
        }

        //za switch pomegju razlicni fragmenti
        //vo zavisnost od koja ikona e kliknata, toj fragment da se otvori
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_chat -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frameLayout, chatFragment)
                        .commit()
                    true
                }

                R.id.menu_profile -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.main_frameLayout, profileFragment)
                        .commit()
                    true
                }

                else -> false //nema da se prevzeme akcija ako ne e selektirano nishto
            }
        }
        //koga app startuva po default e chat fragmentot
        bottomNavigationView.selectedItemId = R.id.menu_chat
    }
}

