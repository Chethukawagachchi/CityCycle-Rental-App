package com.example.citycycle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ViewDiscountAdapter extends RecyclerView.Adapter<ViewDiscountAdapter.DiscountViewHolder> {
    private List<Discount> discounts = new ArrayList<>();
    private static final String CURRENT_UTC_TIME = "2025-03-03 03:59:17";

    @NonNull
    @Override
    public DiscountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_view_discount, parent, false);
        return new DiscountViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscountViewHolder holder, int position) {
        holder.bind(discounts.get(position));
    }

    @Override
    public int getItemCount() {
        return discounts.size();
    }

    public void setDiscounts(List<Discount> discounts) {
        this.discounts = discounts;
        notifyDataSetChanged();
    }

    static class DiscountViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtCode;
        private final TextView txtPercentage;
        private final TextView txtValidUntil;
        private final TextView txtDaysRemaining;

        DiscountViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCode = itemView.findViewById(R.id.txtDiscountCode);
            txtPercentage = itemView.findViewById(R.id.txtDiscountPercentage);
            txtValidUntil = itemView.findViewById(R.id.txtValidUntil);
            txtDaysRemaining = itemView.findViewById(R.id.txtDaysRemaining);
        }

        void bind(Discount discount) {
            txtCode.setText(discount.getCode());
            txtPercentage.setText(String.format("%d%% OFF", discount.getPercentage()));
            txtValidUntil.setText(String.format("Valid Until: %s", discount.getValidUntil()));
            setRemainingDays(discount.getValidUntil());
        }

        private void setRemainingDays(String validUntil) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date validDate = sdf.parse(validUntil);
                Date currentDate = sdf.parse(CURRENT_UTC_TIME.split(" ")[0]);

                if (validDate != null && currentDate != null) {
                    long diff = validDate.getTime() - currentDate.getTime();
                    long days = diff / (24 * 60 * 60 * 1000);

                    if (days > 0) {
                        txtDaysRemaining.setText(String.format("%d days remaining", days));
                        txtDaysRemaining.setTextColor(itemView.getContext().getResources()
                                .getColor(android.R.color.holo_green_dark));
                    } else {
                        txtDaysRemaining.setText("Expires today");
                        txtDaysRemaining.setTextColor(itemView.getContext().getResources()
                                .getColor(android.R.color.holo_orange_dark));
                    }
                }
            } catch (ParseException e) {
                txtDaysRemaining.setText("Date error");
            }
        }
    }
}