package com.mindmatrix.nammamela

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.mindmatrix.nammamela.databinding.FragmentBookingsBinding
import com.mindmatrix.nammamela.databinding.FragmentCastDetailBinding
import com.mindmatrix.nammamela.databinding.FragmentFanWallBinding
import com.mindmatrix.nammamela.databinding.FragmentHomeBinding
import com.mindmatrix.nammamela.databinding.FragmentProfileBinding
import com.mindmatrix.nammamela.databinding.FragmentSeatsBinding
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    private lateinit var adapter: NatakaAdapter
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentHomeBinding.inflate(inflater, container, false).also { b ->
        adapter = NatakaAdapter({ id -> vm.seats.value.count { it.natakaId == id && it.status == SeatStatus.AVAILABLE } }, {
            vm.selectNataka(it.id); findNavController().navigate(R.id.seatsFragment)
        }, { vm.favorite(it.id, !it.favorite) })
        b.natakaList.layoutManager = LinearLayoutManager(requireContext())
        b.natakaList.adapter = adapter
        b.todayChip.setOnClickListener { vm.filter.value = NatakaFilter.TODAY; adapter.submitList(vm.filteredNatakas()) }
        b.tomorrowChip.setOnClickListener { vm.filter.value = NatakaFilter.TOMORROW; adapter.submitList(vm.filteredNatakas()) }
        b.weekChip.setOnClickListener { vm.filter.value = NatakaFilter.WEEK; adapter.submitList(vm.filteredNatakas()) }
        b.searchButton.setOnClickListener { vm.query.value = b.searchInput.text.toString(); adapter.submitList(vm.filteredNatakas()) }
        viewLifecycleOwner.lifecycleScope.launch { vm.natakas.collect { adapter.submitList(vm.filteredNatakas()) } }
    }.root
}

class SeatsFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    private lateinit var seatAdapter: SeatAdapter
    private val castAdapter = CastAdapter { vm.selectedCastId.value = it.id; findNavController().navigate(R.id.castDetailFragment) }
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentSeatsBinding.inflate(inflater, container, false).also { b ->
        seatAdapter = SeatAdapter({ vm.selectedSeats.value }) { vm.toggleSeat(it.code); seatAdapter.notifyDataSetChanged(); b.selectedSeats.text = vm.selectedSeats.value.joinToString() }
        b.seatGrid.layoutManager = GridLayoutManager(requireContext(), 8)
        b.seatGrid.adapter = seatAdapter
        b.castList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        b.castList.adapter = castAdapter
        b.bookButton.setOnClickListener { vm.bookSelectedSeats() }
        viewLifecycleOwner.lifecycleScope.launch { vm.seats.collect { seatAdapter.submitList(it); b.availability.text = "${it.count { s -> s.status == SeatStatus.AVAILABLE }} available" } }
        viewLifecycleOwner.lifecycleScope.launch { vm.cast.collect(castAdapter::submitList) }
    }.root
}

class BookingsFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentBookingsBinding.inflate(inflater, container, false).also { b ->
        val adapter = BookingAdapter({ id -> vm.natakas.value.firstOrNull { it.id == id }?.title ?: id }, { vm.cancelBooking(it.id) })
        b.bookingList.layoutManager = LinearLayoutManager(requireContext())
        b.bookingList.adapter = adapter
        viewLifecycleOwner.lifecycleScope.launch { vm.myBookings.collect(adapter::submitList) }
    }.root
}

class FanWallFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentFanWallBinding.inflate(inflater, container, false).also { b ->
        val adapter = ApplauseAdapter({ vm.currentUser.value?.id }, { vm.currentUser.value?.role == UserRole.ADMIN }, { vm.likeComment(it.id) }, { vm.deleteComment(it.id) })
        b.applauseList.layoutManager = LinearLayoutManager(requireContext())
        b.applauseList.adapter = adapter
        b.postButton.setOnClickListener { vm.addComment(b.messageInput.text.toString()); b.messageInput.text?.clear() }
        viewLifecycleOwner.lifecycleScope.launch { vm.comments.collect(adapter::submitList) }
    }.root
}

class ProfileFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentProfileBinding.inflate(inflater, container, false).also { b ->
        val user = vm.currentUser.value
        b.profileName.text = user?.name ?: "Guest"
        b.profileEmail.text = user?.email ?: ""
        b.role.text = user?.role?.name ?: ""
        b.logoutButton.setOnClickListener { vm.logout() }
    }.root
}

class CastDetailFragment : Fragment() {
    private val vm: MelaViewModel by activityViewModels()
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?) = FragmentCastDetailBinding.inflate(inflater, container, false).also { b ->
        val cast = vm.allCast.value.firstOrNull { it.id == vm.selectedCastId.value }
        b.name.text = cast?.fullName
        b.role.text = cast?.role
        b.bio.text = cast?.biography
        b.experience.text = cast?.experience
        b.otherNatakas.text = cast?.otherNatakas
        com.bumptech.glide.Glide.with(b.photo).load(cast?.photo).centerCrop().into(b.photo)
        b.backButton.setOnClickListener { findNavController().navigateUp() }
    }.root
}
