package yadetbashe.app.alisa

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import yadetbashe.app.alisa.databinding.ActivityMainBinding
import yadetbashe.app.alisa.utils.NotificationHelper

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    private val requestNotificationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        binding.bottomNavView.setupWithNavController(navController)

        // FAB فقط در تب تراکنش‌ها معنا دارد
        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.fabAddTransaction.visibility =
                if (destination.id == R.id.nav_transactions) View.VISIBLE else View.GONE
        }

        binding.fabAddTransaction.setOnClickListener {
            showAddTransactionDialog()
        }

        requestNotificationPermissionIfNeeded()
    }

    private fun showAddTransactionDialog() {
        val dialog = yadetbashe.app.alisa.ui.dialogs.TransactionDialogFragment()
        dialog.show(supportFragmentManager, "TransactionDialog")
    }

    private fun requestNotificationPermissionIfNeeded() {
        if (Build.VERSION.SDK_INT >= 33 &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }
}
