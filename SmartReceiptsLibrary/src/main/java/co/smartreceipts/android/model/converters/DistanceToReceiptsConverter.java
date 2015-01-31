package co.smartreceipts.android.model.converters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.smartreceipts.android.R;
import co.smartreceipts.android.model.Distance;
import co.smartreceipts.android.model.Receipt;
import co.smartreceipts.android.model.WBCurrency;
import co.smartreceipts.android.model.factory.PriceBuilderFactory;
import co.smartreceipts.android.model.factory.ReceiptBuilderFactory;
import co.smartreceipts.android.persistence.Preferences;

/**
 * An implementation of the {@link co.smartreceipts.android.model.converters.ModelConverter} contract, which
 * allows us to print {@link co.smartreceipts.android.model.Distance} values in a receipt table. Distances
 * will be summed up based of a given day.
 *
 * @author williambaumann
 */
public class DistanceToReceiptsConverter implements ModelConverter<Distance, Receipt> {

    private final Context mContext;
    private final String mDateSeparator;

    /**
     * Convenience constructor for this class.
     *
     * @param context - the current application {@link android.content.Context}
     * @param preferences - the user's {@link co.smartreceipts.android.persistence.Preferences}
     */
    public DistanceToReceiptsConverter(@NonNull Context context, @NonNull Preferences preferences) {
        this(context, preferences.getDateSeparator());
    }

    /**
     * Default constructor for this class.
     *
     * @param context - the current application {@link android.content.Context}
     * @param dateSeparator - the user's preferred date separator (e.g. "/")
     */
    public DistanceToReceiptsConverter(@NonNull Context context, @NonNull String dateSeparator) {
        mContext = context.getApplicationContext();
        mDateSeparator = dateSeparator;
    }


    @Override
    @NonNull
    public List<Receipt> convert(@NonNull List<Distance> distances) {
        final int size = distances.size();
        final HashMap<String, List<Distance>> distancesPerDay = new HashMap<String, List<Distance>>();
        // First, let's separate our distances to find what occurs each day
        for (int i = 0; i < size; i++) {
            final Distance distance = distances.get(i);
            final String formattedDate = distance.getFormattedDate(mContext, mDateSeparator);
            if (distancesPerDay.containsKey(formattedDate)) {
                distancesPerDay.get(formattedDate).add(distance);
            }
            else {
                final List<Distance> distanceList = new ArrayList<Distance>();
                distanceList.add(distance);
                distancesPerDay.put(formattedDate, distanceList);
            }
        }

        final List<Receipt> receipts = new ArrayList<Receipt>(distancesPerDay.keySet().size());
        for (Map.Entry<String, List<Distance>> entry : distancesPerDay.entrySet()) {
            if (!entry.getValue().isEmpty()) {
                receipts.add(generateReceipt(entry.getKey(), entry.getValue()));
            }
        }
        return receipts;
    }

    @NonNull
    private Receipt generateReceipt(@NonNull String formattedDay, @NonNull List<Distance> distancesThisDay) {
        if (distancesThisDay.isEmpty()) {
            throw new IllegalArgumentException("distancesThisDay must not be empty");
        }

        // Set up default values for everything
        final Distance distance0 = distancesThisDay.get(0);
        final ReceiptBuilderFactory factory = new ReceiptBuilderFactory(-1); // Randomize the id
        final ArrayList<String> names = new ArrayList<String>();
        for (int i = 0; i < distancesThisDay.size(); i++) {
            final Distance distance = distancesThisDay.get(i);
            if (!names.contains(distance.getLocation())) {
                names.add(distance.getLocation());
            }
        }
        factory.setName(TextUtils.join("; ", names));
        factory.setDate(distance0.getDate());
        factory.setImage(null);
        factory.setIsExpenseable(true);
        factory.setTimeZone(distance0.getTimeZone());
        factory.setCategory(mContext.getString(R.string.distance));
        factory.setCurrency(distance0.getPrice().getCurrency());
        factory.setPrice(new PriceBuilderFactory().setPriceables(distancesThisDay).build());

        return factory.build();
    }
}
