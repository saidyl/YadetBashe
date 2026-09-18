package yadetbashe.app.alisa.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import yadetbashe.app.alisa.R
import yadetbashe.app.alisa.data.repository.AppRepository
import yadetbashe.app.alisa.databinding.FragmentSettingsBinding
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

        // یادآوری‌ها
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
                val msg = if (ok) R.string.backup_success else R.string.backup_failed
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }

        // بازیابی
        binding.btnRestore.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val ok = withContext(Dispatchers.IO) {
                    repository.importBackup(requireContext())
                }
                val msg = if (ok) R.string.restore_success else R.string.restore_failed
                Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
