package com.example.sweetbakes.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "ordersitem")
data class OrdersItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(), // Unique identifier
    val title: String,
    val description: String,
    val priority: String,
    val dueDate: String,
    val reminders: String,
    val isCompleted: Boolean = false,
    val notificationScheduled: Boolean = false
) : Parcelable {


    fun priorityOrder(): Int {
        return when (priority) {
            "High" -> 1
            "Medium" -> 2
            "Low" -> 3
            else -> 4
        }
    }

    fun getReminderTimestamp(): Long {
        if (reminders.isEmpty()) return 0L

        try {
            // Match the format from BottomSheetContent
            val format = SimpleDateFormat("MMMM d, yyyy h:mm a", Locale.getDefault())
            return format.parse(reminders)?.time ?: 0L
        } catch (e: Exception) {
            e.printStackTrace()
            return 0L
        }
    }

    fun toOrdersItem(): OrdersItem = OrdersItem(
        id = id,
        title = title,
        description = description,
        priority = priority,
        dueDate = dueDate,
        reminders = reminders,
        isCompleted = isCompleted
    )

    // Stable key function that uses the ID
    fun getKey(): String = id // Just use the UUID as the key

    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: UUID.randomUUID().toString(),
        title = parcel.readString() ?: "",
        description = parcel.readString() ?: "",
        priority = parcel.readString() ?: "Low",
        dueDate = parcel.readString() ?: "",
        reminders = parcel.readString() ?: "",
        isCompleted = parcel.readByte() != 0.toByte(),
        notificationScheduled = parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(title)
        parcel.writeString(description)
        parcel.writeString(priority)
        parcel.writeString(dueDate)
        parcel.writeString(reminders)
        parcel.writeByte(if (isCompleted) 1 else 0)
        parcel.writeByte(if (notificationScheduled) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<OrdersItem> {
        override fun createFromParcel(parcel: Parcel): OrdersItem {
            return OrdersItem(parcel)
        }

        override fun newArray(size: Int): Array<OrdersItem?> {
            return arrayOfNulls(size)
        }
    }
}