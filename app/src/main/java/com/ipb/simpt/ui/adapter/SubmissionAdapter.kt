package com.ipb.simpt.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.ipb.simpt.R
import com.ipb.simpt.model.DataModel

class SubmissionAdapter(
    private val submissions: List<DataModel>,
    private val onItemClick: (DataModel) -> Unit
) : RecyclerView.Adapter<SubmissionAdapter.SubmissionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubmissionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_approval_list, parent, false)
        return SubmissionViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubmissionViewHolder, position: Int) {
        val submission = submissions[position]
        holder.bind(submission)
        holder.itemView.setOnClickListener { onItemClick(submission) }
    }

    override fun getItemCount(): Int = submissions.size

    class SubmissionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_title)
        private val tvDescription: TextView = itemView.findViewById(R.id.tv_description)
        private val tvUser: TextView = itemView.findViewById(R.id.tv_user)
        private val tvStatus: TextView = itemView.findViewById(R.id.tv_user)
        fun bind(submission: DataModel) {
            tvTitle.text = submission.komoditasId // This will now be the name
            tvDescription.text = submission.deskripsi
            tvUser.text = submission.uid // This will now be the userName
            tvStatus.text = submission.status
        }
    }
}
