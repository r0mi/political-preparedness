package com.example.android.politicalpreparedness.election

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.example.android.politicalpreparedness.MyApp
import com.example.android.politicalpreparedness.R
import com.example.android.politicalpreparedness.databinding.FragmentVoterInfoBinding
import com.google.android.material.snackbar.Snackbar

class VoterInfoFragment : Fragment() {

    private lateinit var binding: FragmentVoterInfoBinding

    private val args by navArgs<VoterInfoFragmentArgs>()

    private val viewModel by viewModels<VoterInfoViewModel> {
        VoterInfoViewModelFactory(
            args.argElectionId,
            args.argDivision,
            (requireContext().applicationContext as MyApp).electionsRepository
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_voter_info, container, false)
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

        viewModel.goToUrl.observe(viewLifecycleOwner) { url ->
            openUrl(url)
        }

        return binding.root
    }

    private fun openUrl(url: String) {
        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
    }

}