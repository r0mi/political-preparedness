package com.example.android.politicalpreparedness.election

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.android.politicalpreparedness.MyApp
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentElectionBinding
import com.example.android.politicalpreparedness.election.adapter.ElectionListAdapter
import com.example.android.politicalpreparedness.election.adapter.ElectionListener
import com.google.android.material.snackbar.Snackbar

class ElectionsFragment : Fragment() {

    private lateinit var binding: FragmentElectionBinding

    private val viewModel by viewModels<ElectionsViewModel> {
        ElectionsViewModelFactory(
            (requireContext().applicationContext as MyApp).electionsRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_election, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        viewModel.showErrorMessage.observe(viewLifecycleOwner) { (msg, resId) ->
            if (resId != null && msg != null) {
                Snackbar.make(this.requireView(), getString(resId, msg), Snackbar.LENGTH_LONG)
                    .show()
            } else if (resId != null) {
                Snackbar.make(this.requireView(), resId, Snackbar.LENGTH_LONG).show()
            } else if (msg != null) {
                Snackbar.make(this.requireView(), msg, Snackbar.LENGTH_LONG).show()
            }
        }

        viewModel.navigateTo.observe(viewLifecycleOwner) { navDirections ->
            findNavController().navigate(navDirections)
        }

        val electionsListener = ElectionListener {
            viewModel.electionClicked(it)
        }
        binding.upcomingElectionsRecyclerView.setup(ElectionListAdapter(electionsListener))
        binding.savedElectionsRecyclerView.setup(ElectionListAdapter(electionsListener))

        return binding.root
    }
}

private fun RecyclerView.setup(electionListAdapter: ElectionListAdapter) {
    this.apply {
        layoutManager = LinearLayoutManager(this.context)
        this.adapter = electionListAdapter
    }
}
