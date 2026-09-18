package yadetbashe.app.alisa.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
                findNavController().navigate(
                    NavGraphDirections.actionGlobalPersonDetail(item.person.id)
                )
            },
            onItemLongClicked = { item -> showEditDialog(item.person) }
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

    private fun showEditDialog(person: Person?) {
        val dialogBinding = DialogPersonBinding.inflate(layoutInflater)
        person?.let {
            dialogBinding.etPersonName.setText(it.name)
            dialogBinding.etPersonPhone.setText(it.phone)
            dialogBinding.etPersonNote.setText(it.note)
        }

        MaterialAlertDialogBuilder(requireContext())
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
                }
            }
            .setNeutralButton(R.string.delete) { _, _ ->
                person?.let { viewModel.deletePerson(it) }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
