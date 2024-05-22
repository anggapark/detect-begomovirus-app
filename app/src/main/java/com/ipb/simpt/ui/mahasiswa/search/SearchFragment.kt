package com.ipb.simpt.ui.mahasiswa.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ipb.simpt.R
import com.ipb.simpt.databinding.FragmentSearchBinding

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
//        val searchViewModel =
//            ViewModelProvider(this).get(SearchViewModel::class.java)
//
//        _binding = FragmentSearchBinding.inflate(inflater, container, false)
//        val root: View = binding.root
//
//        val textView: TextView = binding.textSearch
//        searchViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }
//
//        return root

        return inflater.inflate(R.layout.fragment_search, container, false)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}