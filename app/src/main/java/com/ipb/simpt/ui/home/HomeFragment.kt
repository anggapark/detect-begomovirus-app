package com.ipb.simpt.ui.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.ipb.simpt.databinding.FragmentHomeBinding
import com.ipb.simpt.ui.add.AddActivity
import com.ipb.simpt.ui.mydata.MyDataActivity

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

        // Launch ActivityAdd
        val cvAdd: CardView = binding.cvAdd
        cvAdd.setOnClickListener {
            startActivity(Intent(requireContext(), AddActivity::class.java))
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