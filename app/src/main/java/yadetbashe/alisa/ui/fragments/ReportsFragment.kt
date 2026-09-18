package yadetbashe.app.alisa.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import dagger.hilt.android.AndroidEntryPoint
import yadetbashe.app.alisa.R
import yadetbashe.app.alisa.databinding.FragmentReportsBinding
import yadetbashe.app.alisa.utils.PersianDate
import yadetbashe.app.alisa.viewmodel.ReportsViewModel
import java.text.DecimalFormat

@AndroidEntryPoint
class ReportsFragment : Fragment() {

    private var _binding: FragmentReportsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ReportsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentReportsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnPrevMonth.setOnClickListener { viewModel.previousMonth() }
        binding.btnNextMonth.setOnClickListener { viewModel.nextMonth() }

        setupChart()

        viewModel.summary.observe(viewLifecycleOwner) { s ->
            binding.tvMonthTitle.text = PersianDate.formatMonthYear(s.monthStart)
            val fmt = DecimalFormat("#,###")
            binding.tvMonthDebts.text = getString(
                R.string.month_debts_summary, PersianDate.toPersianDigits(fmt.format(s.debts))
            )
            binding.tvMonthCredits.text = getString(
                R.string.month_credits_summary, PersianDate.toPersianDigits(fmt.format(s.credits))
            )

            // نمودار: دو ستون بدهی و طلب
            val entries = listOf(
                BarEntry(0f, s.debts.toFloat()),
                BarEntry(1f, s.credits.toFloat())
            )
            val dataSet = BarDataSet(entries, "").apply {
                setColors(
                    ContextCompat.getColor(requireContext(), R.color.debt_color),
                    ContextCompat.getColor(requireContext(), R.color.credit_color)
                )
                setDrawValues(false)
            }
            binding.chartMonthly.data = BarData(dataSet).apply { barWidth = 0.4f }
            binding.chartMonthly.invalidate()
        }
    }

    private fun setupChart() {
        binding.chartMonthly.apply {
            description.isEnabled = false
            legend.isEnabled = false
            axisRight.isEnabled = false
            setScaleEnabled(false)
            setPinchZoom(false)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawGridLines(false)
                granularity = 1f
                valueFormatter = IndexAxisValueFormatter(
                    listOf(
                        getString(R.string.debt_label),
                        getString(R.string.credit_label)
                    )
                )
            }
            axisLeft.axisMinimum = 0f
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
