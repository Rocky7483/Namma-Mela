package com.mindmatrix.nammamela

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.mindmatrix.nammamela.databinding.ItemApplauseBinding
import com.mindmatrix.nammamela.databinding.ItemBookingBinding
import com.mindmatrix.nammamela.databinding.ItemCastBinding
import com.mindmatrix.nammamela.databinding.ItemNatakaBinding
import com.mindmatrix.nammamela.databinding.ItemSeatBinding
import com.mindmatrix.nammamela.databinding.ItemUserBinding

class NatakaAdapter(private val available: (String) -> Int, private val onBook: (NatakaEntity) -> Unit, private val onFav: (NatakaEntity) -> Unit) : ListAdapter<NatakaEntity, NatakaAdapter.VH>(diff()) {
    class VH(val b: ItemNatakaBinding) : RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemNatakaBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(h: VH, p: Int) {
        val item = getItem(p)
        h.b.title.text = item.title
        h.b.meta.text = "${item.date} • ${item.startTime} • ${item.duration}"
        h.b.description.text = item.description
        h.b.venue.text = item.venue
        h.b.available.text = "${available(item.id)} seats available"
        h.b.favorite.text = if (item.favorite) "Saved" else "Favorite"
        Glide.with(h.b.poster).load(item.posterImage).centerCrop().into(h.b.poster)
        h.b.bookButton.setOnClickListener { onBook(item) }
        h.b.favorite.setOnClickListener { onFav(item) }
    }
}

class CastAdapter(private val onClick: (CastEntity) -> Unit) : ListAdapter<CastEntity, CastAdapter.VH>(diff()) {
    class VH(val b: ItemCastBinding) : RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemCastBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(h: VH, p: Int) {
        val item = getItem(p)
        h.b.castName.text = item.fullName
        h.b.castRole.text = item.role
        Glide.with(h.b.castImage).load(item.photo).centerCrop().into(h.b.castImage)
        h.b.root.setOnClickListener { onClick(item) }
    }
}

class SeatAdapter(private val selected: () -> Set<String>, private val onSeat: (SeatEntity) -> Unit) : ListAdapter<SeatEntity, SeatAdapter.VH>(diff()) {
    class VH(val b: ItemSeatBinding) : RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemSeatBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(h: VH, p: Int) {
        val item = getItem(p)
        h.b.seatCode.text = item.code
        val bg = when {
            selected().contains(item.code) -> R.drawable.bg_seat_selected
            item.status == SeatStatus.BOOKED -> R.drawable.bg_seat_reserved
            else -> R.drawable.bg_seat_available
        }
        h.b.root.background = ContextCompat.getDrawable(h.itemView.context, bg)
        h.b.root.alpha = if (item.status == SeatStatus.BOOKED) 0.75f else 1f
        h.b.root.setOnClickListener { if (item.status != SeatStatus.BOOKED) onSeat(item) }
    }
}

class BookingAdapter(private val natakaName: (String) -> String, private val onCancel: (BookingEntity) -> Unit) : ListAdapter<BookingEntity, BookingAdapter.VH>(diff()) {
    class VH(val b: ItemBookingBinding) : RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(h: VH, p: Int) {
        val item = getItem(p)
        h.b.bookingTitle.text = natakaName(item.natakaId)
        h.b.bookingSeats.text = "Seats: ${item.seatCodes}"
        h.b.bookingStatus.text = item.status.name
        h.b.cancelButton.isEnabled = item.status == BookingStatus.ACTIVE
        h.b.cancelButton.setOnClickListener { onCancel(item) }
    }
}

class ApplauseAdapter(private val ownUser: () -> String?, private val admin: () -> Boolean, private val onLike: (FanCommentEntity) -> Unit, private val onDelete: (FanCommentEntity) -> Unit) : ListAdapter<FanCommentEntity, ApplauseAdapter.VH>(diff()) {
    class VH(val b: ItemApplauseBinding) : RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemApplauseBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(h: VH, p: Int) {
        val item = getItem(p)
        h.b.fanName.text = item.userName
        h.b.message.text = item.message
        h.b.time.text = java.text.DateFormat.getDateTimeInstance().format(java.util.Date(item.createdAt))
        h.b.likeButton.text = "Like (${item.likes})"
        h.b.deleteButton.isEnabled = admin() || item.userId == ownUser()
        h.b.likeButton.setOnClickListener { onLike(item) }
        h.b.deleteButton.setOnClickListener { onDelete(item) }
    }
}

class UserAdapter(private val onSuspend: (UserEntity) -> Unit, private val onDelete: (UserEntity) -> Unit) : ListAdapter<UserEntity, UserAdapter.VH>(diff()) {
    class VH(val b: ItemUserBinding) : RecyclerView.ViewHolder(b.root)
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = VH(ItemUserBinding.inflate(LayoutInflater.from(parent.context), parent, false))
    override fun onBindViewHolder(h: VH, p: Int) {
        val item = getItem(p)
        h.b.userName.text = item.name
        h.b.userEmail.text = "${item.email} • ${item.role.name}"
        h.b.suspendButton.text = if (item.suspended) "Unsuspend" else "Suspend"
        h.b.suspendButton.setOnClickListener { onSuspend(item) }
        h.b.deleteButton.setOnClickListener { onDelete(item) }
    }
}

private inline fun <reified T : Any> diff() = object : DiffUtil.ItemCallback<T>() {
    override fun areItemsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
    override fun areContentsTheSame(oldItem: T, newItem: T): Boolean = oldItem == newItem
}
