package com.ipb.simpt.ui.filter

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.ipb.simpt.R
import com.ipb.simpt.databinding.FragmentFilterBinding
import com.ipb.simpt.model.FilterModel
import com.ipb.simpt.utils.CategoryHandler
import java.util.Calendar

class FilterFragment : DialogFragment() {

    private lateinit var viewModel: FilterViewModel
    private lateinit var categoryHandler: CategoryHandler
    private lateinit var binding: FragmentFilterBinding

    private var selectedKomoditasId: String? = null
    private var selectedPenyakitId: String? = null
    private var selectedPathogenId: String? = null
    private var selectedGejalaId: String? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFilterBinding.inflate(inflater, container, false)
        categoryHandler = CategoryHandler(requireContext(), binding.progressBar)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(requireActivity()).get(FilterViewModel::class.java)
        setupAction()

        // Retrieve and apply the passed filter data
        arguments?.getParcelable<FilterModel>("currentFilters")?.let { filters ->
            applyFiltersToUI(filters)
        }
    }

    private fun setupAction() {
        binding.apply {
            setupEndIcons() // Setup default icons

            etKomoditas.setOnClickListener {
                categoryHandler.showKomoditasDialog { selectedId, selectedKomoditas ->
                    selectedKomoditasId = selectedId
                    etKomoditas.setText(selectedKomoditas)
                    tilKomoditas.setEndIconDrawable(R.drawable.ic_close)
                    enableResetButton()
                }
            }

            tilKomoditas.setEndIconOnClickListener {
                selectedKomoditasId = null
                etKomoditas.text?.clear()
                tilKomoditas.setEndIconDrawable(R.drawable.ic_down_gray)
                checkAndDisableResetButton()

            }

            etPenyakit.setOnClickListener {
                categoryHandler.showPenyakitDialog { selectedId, selectedPenyakit ->
                    selectedPenyakitId = selectedId
                    etPenyakit.setText(selectedPenyakit)
                    tilPenyakit.setEndIconDrawable(R.drawable.ic_close)
                    enableResetButton()
                }
            }

            tilPenyakit.setEndIconOnClickListener {
                selectedPenyakitId = null
                etPenyakit.text?.clear()
                tilPenyakit.setEndIconDrawable(R.drawable.ic_down_gray)
                checkAndDisableResetButton()

            }

            etKategoriPathogen.setOnClickListener {
                categoryHandler.showKategoriPathogenDialog { selectedKategoriPathogen ->
                    etKategoriPathogen.setText(selectedKategoriPathogen)
                    etPathogen.text?.clear() // Reset the Pathogen field
                    selectedPathogenId = null // Clear the selected Pathogen ID
                    tilPathogen.visibility = View.VISIBLE
                    tvPathogenTitle.visibility = View.VISIBLE
                    tilKategoriPathogen.setEndIconDrawable(R.drawable.ic_close)
                    enableResetButton()
                }
            }

            tilKategoriPathogen.setEndIconOnClickListener {
                etKategoriPathogen.text?.clear()
                etPathogen.text?.clear()
                selectedPathogenId = null
                tilPathogen.visibility = View.GONE
                tvPathogenTitle.visibility = View.GONE
                tilKategoriPathogen.setEndIconDrawable(R.drawable.ic_down_gray)
                checkAndDisableResetButton()

            }

            etPathogen.setOnClickListener {
                val selectedKategoriPathogen = etKategoriPathogen.text.toString()
                if (selectedKategoriPathogen.isNotEmpty()) {
                    categoryHandler.showPathogenDialog(selectedKategoriPathogen) { selectedId, selectedPathogen ->
                        selectedPathogenId = selectedId
                        etPathogen.setText(selectedPathogen)
                        tilPathogen.setEndIconDrawable(R.drawable.ic_close)
                        enableResetButton()
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please select a Kategori Pathogen first",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            tilPathogen.setEndIconOnClickListener {
                selectedPathogenId = null
                etPathogen.text?.clear()
                tilPathogen.setEndIconDrawable(R.drawable.ic_down_gray)
                checkAndDisableResetButton()

            }

            etGejala.setOnClickListener {
                categoryHandler.showGejalaDialog { selectedId, selectedGejala ->
                    selectedGejalaId = selectedId
                    etGejala.setText(selectedGejala)
                    tilGejala.setEndIconDrawable(R.drawable.ic_close)
                    enableResetButton()
                }
            }

            tilGejala.setEndIconOnClickListener {
                selectedGejalaId = null
                etGejala.text?.clear()
                tilGejala.setEndIconDrawable(R.drawable.ic_down_gray)
                checkAndDisableResetButton()

            }

            edDate.setOnClickListener {
                showDatePickerDialog()
                tilDate.setEndIconDrawable(R.drawable.ic_close)
                enableResetButton()
            }

            tilDate.setEndIconOnClickListener {
                edDate.text?.clear()
                tilDate.setEndIconDrawable(R.drawable.ic_down_gray)
                checkAndDisableResetButton()
            }

            btnApply.setOnClickListener {
                applyFilters()
            }

            btnReset.setOnClickListener {
                resetFilters()
            }

            ivClose.setOnClickListener {
                dismiss()
            }
        }
    }

    private fun setupEndIcons() {
        binding.apply {
            tilKomoditas.setEndIconDrawable(R.drawable.ic_down_gray)
            tilPenyakit.setEndIconDrawable(R.drawable.ic_down_gray)
            tilKategoriPathogen.setEndIconDrawable(R.drawable.ic_down_gray)
            tilPathogen.setEndIconDrawable(R.drawable.ic_down_gray)
            tilGejala.setEndIconDrawable(R.drawable.ic_down_gray)
            tilDate.setEndIconDrawable(R.drawable.ic_down_gray)
        }
    }

    private fun enableResetButton() {
        binding.btnReset.isEnabled = true
    }

    private fun checkAndDisableResetButton() {
        if (selectedKomoditasId == null &&
            selectedPenyakitId == null &&
            selectedPathogenId == null &&
            selectedGejalaId == null &&
            binding.etKategoriPathogen.text.isNullOrEmpty() &&
            binding.edDate.text.isNullOrEmpty()
        ) {
            binding.btnReset.isEnabled = false
        }
    }


    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val formattedDate =
                    String.format("%02d/%02d/%04d", selectedDay, selectedMonth + 1, selectedYear)
                binding.edDate.setText(formattedDate)
            },
            year,
            month,
            day
        )

        datePickerDialog.show()
    }

    private fun applyFilters() {
        val filterModel = FilterModel(
            komoditasId = selectedKomoditasId,
            komoditas = binding.etKomoditas.text.toString(),
            penyakitId = selectedPenyakitId,
            penyakit = binding.etPenyakit.text.toString(),
            kategoriPathogen = binding.etKategoriPathogen.text.toString(),
            pathogenId = selectedPathogenId,
            pathogen = binding.etPathogen.text.toString(),
            gejalaId = selectedGejalaId,
            gejala = binding.etGejala.text.toString(),
            date = binding.edDate.text.toString()
        )
        viewModel.setFilters(filterModel)
        dismiss()
    }

    private fun resetFilters() {
        selectedKomoditasId = null
        selectedPenyakitId = null
        selectedPathogenId = null
        selectedGejalaId = null

        binding.apply {
            etKomoditas.text?.clear()
            etPenyakit.text?.clear()
            etKategoriPathogen.text?.clear()
            etPathogen.text?.clear()
            etGejala.text?.clear()
            edDate.text?.clear()
            setupEndIcons()
        }

        binding.tilPathogen.visibility = View.GONE
        binding.tvPathogenTitle.visibility = View.GONE
        binding.btnReset.isEnabled = false
    }

    private fun applyFiltersToUI(filters: FilterModel) {
        binding.apply {
            etKomoditas.setText(filters.komoditas)
            etPenyakit.setText(filters.penyakit)
            etKategoriPathogen.setText(filters.kategoriPathogen)
            etPathogen.setText(filters.pathogen)
            etGejala.setText(filters.gejala)
            edDate.setText(filters.date)

            tilPathogen.visibility =
                if (filters.kategoriPathogen?.isNotEmpty() == true) View.VISIBLE else View.GONE
            tvPathogenTitle.visibility =
                if (filters.kategoriPathogen?.isNotEmpty() == true) View.VISIBLE else View.GONE
        }
    }
}
