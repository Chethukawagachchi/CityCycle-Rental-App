package com.example.citycycle;



import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class DiscountAdapter extends RecyclerView.Adapter<DiscountAdapter.DiscountViewHolder> {
    private static final String CURRENT_UTC_TIME = "2025-03-03 02:07:19";
    private static final String CURRENT_USER = "Chethukawagachchi";

    private List<Discount> discounts = new ArrayList<>();
    private final OnDiscountClickListener listener;

    public interface OnDiscountClickListener {
        void onDiscountClick(Discount discount);
    }

    public DiscountAdapter(OnDiscountClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DiscountViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_discount, parent, false);
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

    class DiscountViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvDiscountCode;
        private final TextView tvDiscountPercentage;
        private final TextView tvValidUntil;
        private final TextView tvCreatedBy;
        private final TextView tvStatus;

        DiscountViewHolder(@NonNull View itemView) {
            super(itemView);
            tvDiscountCode = itemView.findViewById(R.id.tvDiscountCode);
            tvDiscountPercentage = itemView.findViewById(R.id.tvDiscountPercentage);
            tvValidUntil = itemView.findViewById(R.id.tvValidUntil);
            tvCreatedBy = itemView.findViewById(R.id.tvCreatedBy);
            tvStatus = itemView.findViewById(R.id.tvStatus);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onDiscountClick(discounts.get(position));
                }
            });
        }

        void bind(Discount discount) {
            tvDiscountCode.setText(discount.getCode());
            tvDiscountPercentage.setText(String.format("%d%%", discount.getPercentage()));
            tvValidUntil.setText(String.format("Valid until: %s", discount.getValidUntil()));
            tvCreatedBy.setText(String.format("Created by: %s", discount.getCreatedBy()));

            // Check if discount is still valid
            boolean isValid = isDiscountValid(discount.getValidUntil());
            tvStatus.setText(isValid ? "Active" : "Expired");
            tvStatus.setTextColor(itemView.getContext().getColor(
                    isValid ? android.R.color.holo_green_dark : android.R.color.holo_red_dark));
        }

        private boolean isDiscountValid(String validUntil) {
            try {
                java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
                java.util.Date validDate = sdf.parse(validUntil);
                java.util.Date currentDate = sdf.parse(CURRENT_UTC_TIME.split(" ")[0]);
                return validDate != null && validDate.after(currentDate);
            } catch (Exception e) {
                return false;
            }
        }
    }
}