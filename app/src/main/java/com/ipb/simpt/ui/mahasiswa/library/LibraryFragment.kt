package com.ipb.simpt.ui.mahasiswa.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ipb.simpt.R
import com.ipb.simpt.databinding.FragmentLibraryBinding

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val libraryViewModel =
//            ViewModelProvider(this).get(LibraryViewModel::class.java)
//
//        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
//        val root: View = binding.root
//
//        val textView: TextView = binding.textLibrary
//        libraryViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
//
//        return root

        return inflater.inflate(R.layout.fragment_library, container, false)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}