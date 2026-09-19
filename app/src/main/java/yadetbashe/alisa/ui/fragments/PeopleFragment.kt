package yadetbashe.app.alisa.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import yadetbashe.app.alisa.NavGraphDirections
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import yadetbashe.app.alisa.R
import yadetbashe.app.alisa.data.model.Person
import yadetbashe.app.alisa.databinding.DialogPersonBinding
import yadetbashe.app.alisa.databinding.FragmentPeopleBinding
import yadetbashe.app.alisa.ui.adapter.PersonAdapter
import yadetbashe.app.alisa.viewmodel.PeopleViewModel
import yadetbashe.app.alisa.viewmodel.PersonWithBalance

@AndroidEntryPoint
class PeopleFragment : Fragment() {

    private var _binding: FragmentPeopleBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PeopleViewModel by viewModels()
    private lateinit var adapter: PersonAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPeopleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = PersonAdapter(
            onItemClicked = { item ->
                // منوی گزینه‌ها: جزئیات / ویرایش / حذف
                showOptionsDialog(item)
            },
            onItemLongClicked = { item ->
                // میان‌بر: لمس طولانی = ویرایش
                showEditDialog(item.person)
            }
        )
        binding.rvPeople.layoutManager = LinearLayoutManager(requireContext())
        binding.rvPeople.adapter = adapter

        viewModel.peopleWithBalance.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)
            binding.layoutEmpty.isVisible = list.isEmpty()
        }

        binding.fabAddPerson.setOnClickListener { showEditDialog(null) }
        binding.layoutEmpty.setOnClickListener { showEditDialog(null) }
    }

    /**
     * منوی گزینه‌های فرد: جزئیات، ویرایش، حذف
     */
    private fun showOptionsDialog(item: PersonWithBalance) {
        val options = arrayOf(
            getString(R.string.transaction_history),
            getString(R.string.edit),
            getString(R.string.delete)
        )
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(item.person.name)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> findNavController().navigate(
                        NavGraphDirections.actionGlobalPersonDetail(item.person.id)
                    )

                    1 -> showEditDialog(item.person)

                    2 -> confirmDelete(item.person)
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditDialog(person: Person?) {
        val dialogBinding = DialogPersonBinding.inflate(layoutInflater)
        person?.let {
            dialogBinding.etPersonName.setText(it.name)
            dialogBinding.etPersonPhone.setText(it.phone)
            dialogBinding.etPersonNote.setText(it.note)
        }

        val builder = MaterialAlertDialogBuilder(requireContext())
            .setTitle(
                if (person == null) getString(R.string.add_person)
                else getString(R.string.edit_person)
            )
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save) { _, _ ->
                val name = dialogBinding.etPersonName.text.toString().trim()
                if (name.isNotEmpty()) {
                    val p = (person ?: Person(name = name)).copy(
                        name = name,
                        phone = dialogBinding.etPersonPhone.text.toString().trim(),
                        note = dialogBinding.etPersonNote.text.toString().trim()
                    )
                    viewModel.savePerson(p)
                    Toast.makeText(requireContext(), R.string.person_saved, Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton(R.string.cancel, null)

        // دکمه حذف فقط در حالت ویرایش
        if (person != null) {
            builder.setNeutralButton(R.string.delete) { _, _ -> confirmDelete(person) }
        }

        builder.show()
    }

    private fun confirmDelete(person: Person) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete)
            .setMessage(R.string.delete_person_message)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deletePerson(person)
                Toast.makeText(requireContext(), R.string.person_deleted, Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
