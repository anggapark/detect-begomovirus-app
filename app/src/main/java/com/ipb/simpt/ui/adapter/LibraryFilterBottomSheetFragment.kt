package com.ipb.simpt.ui.adapter

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.ipb.simpt.R

class LibraryFilterBottomSheetFragment :  BottomSheetDialogFragment() {

    interface FilterListener {
        fun onFilterApplied(status: String)
    }

    private var listener: FilterListener? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)
        listener = context as? FilterListener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.dialog_filter, container, false)
        val btnPending = view.findViewById<Button>(R.id.btn_pending)
        val btnApproved = view.findViewById<Button>(R.id.btn_approved)
        val btnRejected = view.findViewById<Button>(R.id.btn_rejected)

        btnPending.setOnClickListener { listener?.onFilterApplied("pending") }
        btnApproved.setOnClickListener { listener?.onFilterApplied("approved") }
        btnRejected.setOnClickListener { listener?.onFilterApplied("rejected") }

        return view
    }
}
