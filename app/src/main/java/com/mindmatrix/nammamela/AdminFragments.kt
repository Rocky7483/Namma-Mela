package com.mindmatrix.nammamela

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mindmatrix.nammamela.databinding.FragmentAdminDashboardBinding
import com.mindmatrix.nammamela.databinding.FragmentAdminNatakasBinding
import com.mindmatrix.nammamela.databinding.FragmentBookingsBinding
import com.mindmatrix.nammamela.databinding.FragmentUsersBinding
import kotlinx.coroutines.launch

class AdminDashboardFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentAdminDashboardBinding.inflate(inflater, container, false).also { b ->
        b.logoutButton.setOnClickListener { vm.logout() }
        viewLifecycleOwner.lifecycleScope.launch { vm.natakas.collect { b.stats.text = "${it.size} natakas • ${vm.users.value.size} users • ${vm.allBookings.value.size} bookings" } }
    }.root
}

class AdminNatakasFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentAdminNatakasBinding.inflate(inflater, container, false).also { b ->
        val adapter = NatakaAdapter({ 0 }, { n ->
            b.titleInput.setText(n.title); b.descInput.setText(n.description); b.posterInput.setText(n.posterImage); b.dateInput.setText(n.date); b.timeInput.setText(n.startTime); b.durationInput.setText(n.duration); b.venueInput.setText(n.venue); b.editingId.setText(n.id)
        }, { vm.deleteNataka(it.id) })
        b.natakaList.layoutManager = LinearLayoutManager(requireContext())
        b.natakaList.adapter = adapter
        b.saveButton.setOnClickListener {
            vm.saveNataka(b.editingId.text?.toString()?.ifBlank { null }, b.titleInput.text.toString(), b.descInput.text.toString(), b.posterInput.text.toString(), b.dateInput.text.toString(), b.timeInput.text.toString(), b.durationInput.text.toString(), b.venueInput.text.toString())
            b.editingId.text?.clear()
        }
        b.addCastButton.setOnClickListener {
            val natakaId = b.editingId.text?.toString()?.ifBlank { vm.natakas.value.firstOrNull()?.id }.orEmpty()
            if (natakaId.isNotBlank()) vm.saveCast(natakaId, b.castNameInput.text.toString(), b.castRoleInput.text.toString(), b.castPhotoInput.text.toString(), b.castBioInput.text.toString(), b.castExperienceInput.text.toString())
        }
        viewLifecycleOwner.lifecycleScope.launch { vm.natakas.collect(adapter::submitList) }
    }.root
}

class AdminUsersFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentUsersBinding.inflate(inflater, container, false).also { b ->
        val adapter = UserAdapter({ vm.suspendUser(it.id, !it.suspended) }, { vm.deleteUser(it.id) })
        b.userList.layoutManager = LinearLayoutManager(requireContext())
        b.userList.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch { vm.users.collect(adapter::submitList) }
    }.root
}

class AdminBookingsFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentBookingsBinding.inflate(inflater, container, false).also { b ->
        val adapter = BookingAdapter({ id -> vm.natakas.value.firstOrNull { it.id == id }?.title ?: id }, { vm.cancelBooking(it.id) })
        b.bookingList.layoutManager = LinearLayoutManager(requireContext())
        b.bookingList.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch { vm.allBookings.collect(adapter::submitList) }
    }.root
}
