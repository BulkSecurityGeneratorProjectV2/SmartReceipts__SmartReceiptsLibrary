package co.smartreceipts.android.search.delegates

import android.graphics.Typeface
import co.smartreceipts.android.databinding.ItemTripOrDistanceCardBinding
import co.smartreceipts.android.model.Distance
import co.smartreceipts.android.sync.BackupProvidersManager
import com.hannesdorfmann.adapterdelegates4.dsl.adapterDelegateViewBinding

fun distanceAdapterDelegate(
    itemClickedListener: (Distance) -> Unit,
    backupProvidersManager: BackupProvidersManager
) =
    adapterDelegateViewBinding<Distance, Any, ItemTripOrDistanceCardBinding>({ layoutInflater, parent ->
        ItemTripOrDistanceCardBinding.inflate(layoutInflater, parent, false)
    }) {

        itemView.setOnClickListener { itemClickedListener(item) }

        bind {
            binding.textPrice.text = item.price.currencyFormattedPrice
            binding.textName.text = item.decimalFormattedDistance

            val location: String = item.location
            binding.textDetails.text = if (location.isNotEmpty()) location else item.comment
            binding.textDetails.setTypeface(null, Typeface.BOLD)

            SyncStateViewHelper.setSyncStateImage(binding.imageSyncState, item, backupProvidersManager)
        }


    }

