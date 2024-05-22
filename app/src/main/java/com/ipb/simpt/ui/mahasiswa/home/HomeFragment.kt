package com.ipb.simpt.ui.mahasiswa.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.ipb.simpt.databinding.FragmentHomeBinding
import com.ipb.simpt.ui.mahasiswa.add.AddDataActivity
import com.ipb.simpt.ui.mahasiswa.mydata.MyDataActivity

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // This property is only valid between onCreateView and
    // onDestroyView.

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // TODO: Show name and NIM didn't work
        // Get the data from the bundle
        val userName = arguments?.getString("userName")
        val userNim = arguments?.getString("userNim")

        // Set the data to the TextViews
        binding.tvName.text = userName
        binding.tvNim.text = userNim

        // Launch ActivityAdd
        val cvAdd: CardView = binding.cvAdd
        cvAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddDataActivity::class.java))
        }

        // Launch ActivityMyData
        val cvData: CardView = binding.cvData
        cvData.setOnClickListener {
            startActivity(Intent(requireContext(), MyDataActivity::class.java))
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}