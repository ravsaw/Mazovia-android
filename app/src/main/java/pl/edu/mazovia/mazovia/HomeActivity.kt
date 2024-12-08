//package pl.edu.mazovia.mazovia
//
//import android.content.Intent
//import android.os.Bundle
//import android.widget.Button
//import androidx.activity.enableEdgeToEdge
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.view.ViewCompat
//import androidx.core.view.WindowInsetsCompat
//import androidx.recyclerview.widget.LinearLayoutManager
//import androidx.recyclerview.widget.RecyclerView
//import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
//
//class HomeActivity : AppCompatActivity() {
//    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
//    private lateinit var recyclerView: RecyclerView
//    private lateinit var adapter: HomeAdapter // Zastąp własnym adapterem
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_home)
//
//        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout)
//        recyclerView = findViewById(R.id.recyclerView)
//
//        val dataList = mutableListOf<HomeDataModel>()
//
//        // Konfiguracja RecyclerView
//        recyclerView.layoutManager = LinearLayoutManager(this)
//        adapter = HomeAdapter(dataList) // Inicjalizacja adaptera
//        recyclerView.adapter = adapter
//
//        // Konfiguracja Pull to Refresh
//        swipeRefreshLayout.setOnRefreshListener {
//            refreshData()
//        }
//
//        // Przycisk wylogowania
//        findViewById<Button>(R.id.logoutButton).setOnClickListener {
//            performLogout()
//        }
//    }
//
//    private fun refreshData() {
//        // Tutaj umieść logikę odświeżania danych
//        // Po zakończeniu:
//        swipeRefreshLayout.isRefreshing = false
//    }
//
//    private fun performLogout() {
//        // Wyczyść dane sesji, przejdź do LoginActivity
//        startActivity(Intent(this, LoginActivity::class.java))
//        finish()
//    }
//}