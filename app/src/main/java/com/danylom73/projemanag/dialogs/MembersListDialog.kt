package com.danylom73.projemanag.dialogs

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.danylom73.projemanag.adapters.LabelColorListAdapter
import com.danylom73.projemanag.adapters.MemberListItemAdapter
import com.danylom73.projemanag.databinding.DialogListBinding
import com.danylom73.projemanag.models.User

class MembersListDialog(
    private val activity: AppCompatActivity,
    private val members: ArrayList<User>,
    private val title: String,
    private val onMemberSelected: (User, String) -> Unit
) : Dialog(activity) {
    private lateinit var binding: DialogListBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogListBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        setupRecyclerView()
    }

    private fun setupRecyclerView() {
        binding.tvTitle.text = title
        binding.rvList.layoutManager = LinearLayoutManager(activity)
        binding.rvList.adapter = MemberListItemAdapter(
            activity, members
        ) { pos, user, action ->
            dismiss()
            onMemberSelected(user, action)
        }
    }
}