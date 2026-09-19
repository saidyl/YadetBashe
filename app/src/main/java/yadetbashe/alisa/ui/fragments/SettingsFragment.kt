package yadetbashe.app.alisa.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yadetbashe.app.alisa.R
import yadetbashe.app.alisa.data.repository.AppRepository
import yadetbashe.app.alisa.databinding.FragmentSettingsBinding
import yadetbashe.app.alisa.utils.JsonBackup
import yadetbashe.app.alisa.utils.PersianDate
import yadetbashe.app.alisa.utils.ReminderScheduler
import yadetbashe.app.alisa.utils.SettingsPrefs
import javax.inject.Inject

@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    @Inject
    lateinit var repository: AppRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupThemeSection()
        setupRemindersSection()
        setupBackupSection()
        setupDangerSection()
    }

    // ---------- ظاهر برنامه ----------

    private fun setupThemeSection() {
        when (SettingsPrefs.getThemeMode(requireContext())) {
            SettingsPrefs.THEME_LIGHT -> binding.rbThemeLight.isChecked = true
            SettingsPrefs.THEME_DARK -> binding.rbThemeDark.isChecked = true
            else -> binding.rbThemeSystem.isChecked = true
        }

        binding.rgTheme.setOnCheckedChangeListener { _, checkedId ->
            val mode = when (checkedId) {
                R.id.rb_theme_light -> SettingsPrefs.THEME_LIGHT
                R.id.rb_theme_dark -> SettingsPrefs.THEME_DARK
                else -> SettingsPrefs.THEME_SYSTEM
            }
            SettingsPrefs.setThemeMode(requireContext(), mode)
            // اعمال فوری تم
            SettingsPrefs.applyTheme(requireContext())
        }
    }

    // ---------- یادآوری‌ها ----------

    private fun setupRemindersSection() {
        binding.switchReminders.isChecked = SettingsPrefs.isRemindersEnabled(requireContext())
        binding.switchReminders.setOnCheckedChangeListener { _, checked ->
            SettingsPrefs.setRemindersEnabled(requireContext(), checked)
            if (checked) {
                ReminderScheduler.ensureDailyCheckScheduled(requireContext())
                Toast.makeText(requireContext(), R.string.reminders_enabled, Toast.LENGTH_SHORT).show()
            } else {
                ReminderScheduler.cancelDailyCheck(requireContext())
                Toast.makeText(requireContext(), R.string.reminders_disabled, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // ---------- پشتیبان‌گیری / بازیابی / اشتراک ----------

    private fun setupBackupSection() {
        refreshBackupInfo()

        // پشتیبان‌گیری
        binding.btnBackup.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val ok = withContext(Dispatchers.IO) {
                    try {
                        repository.exportBackup(requireContext())
                        true
                    } catch (e: Exception) {
                        false
                    }
                }
                if (ok) {
                    SettingsPrefs.setLastBackupTime(requireContext(), System.currentTimeMillis())
                    refreshBackupInfo()
                    Toast.makeText(requireContext(), R.string.backup_success, Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), R.string.backup_failed, Toast.LENGTH_SHORT).show()
                }
            }
        }

        // اشتراک‌گذاری فایل پشتیبان
        binding.btnShareBackup.setOnClickListener {
            shareBackupFile()
        }

        // بازیابی با تأیید
        binding.btnRestore.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.restore)
                .setMessage(R.string.restore_confirm)
                .setPositiveButton(R.string.ok) { _, _ -> doRestore() }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    private fun refreshBackupInfo() {
        val last = SettingsPrefs.getLastBackupTime(requireContext())
        binding.tvBackupInfo.text = if (last > 0L) {
            getString(R.string.last_backup, PersianDate.formatNumeric(last))
        } else {
            getString(R.string.no_backup_yet)
        }
    }

    private fun shareBackupFile() {
        val file = JsonBackup.backupFile(requireContext())
        if (!file.exists()) {
            Toast.makeText(requireContext(), R.string.no_backup_file, Toast.LENGTH_SHORT).show()
            return
        }
        try {
            val uri = FileProvider.getUriForFile(
                requireContext(),
                "${requireContext().packageName}.fileprovider",
                file
            )
            val sendIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            startActivity(Intent.createChooser(sendIntent, getString(R.string.share_backup)))
        } catch (e: Exception) {
            Toast.makeText(requireContext(), R.string.backup_failed, Toast.LENGTH_SHORT).show()
        }
    }

    private fun doRestore() {
        viewLifecycleOwner.lifecycleScope.launch {
            val ok = withContext(Dispatchers.IO) {
                repository.importBackup(requireContext())
            }
            val msg = if (ok) R.string.restore_success else R.string.restore_failed
            Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
        }
    }

    // ---------- پاک‌کردن همه داده‌ها ----------

    private fun setupDangerSection() {
        binding.btnClearData.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.clear_all_data)
                .setMessage(R.string.clear_all_data_message)
                .setPositiveButton(R.string.delete) { _, _ ->
                    viewLifecycleOwner.lifecycleScope.launch {
                        withContext(Dispatchers.IO) { repository.deleteAllData() }
                        Toast.makeText(requireContext(), R.string.clear_all_data_done, Toast.LENGTH_SHORT).show()
                    }
                }
                .setNegativeButton(R.string.cancel, null)
                .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
