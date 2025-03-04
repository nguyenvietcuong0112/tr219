package com.moneytracker.aimoney.moneymanager.finance.adapter;



import static com.moneytracker.aimoney.moneymanager.finance.utils.SharePreferenceUtils.getSelectedCurrencyCode;
import static com.moneytracker.aimoney.moneymanager.finance.utils.SharePreferenceUtils.saveSelectedCurrencyCode;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.RecyclerView;

import com.moneytracker.aimoney.moneymanager.finance.R;
import com.moneytracker.aimoney.moneymanager.finance.databinding.ItemCurencyUnitBinding;
import com.moneytracker.aimoney.moneymanager.finance.model.CurrencyUnitModel;

import java.util.List;


public class  CurrencyUnitAdapter extends RecyclerView.Adapter<CurrencyUnitAdapter.CurrencyUnitViewHolder> {

    private static Context context;
    private List<CurrencyUnitModel> lists;
    private IClickCurrencyUnit iClickCurrencyUnit;
    private int selectedPosition = RecyclerView.NO_POSITION;


    public interface IClickCurrencyUnit {
        void onClick(CurrencyUnitModel model);
    }

    public CurrencyUnitAdapter(Context context, List<CurrencyUnitModel> lists, IClickCurrencyUnit iClickCurrencyUnit) {
        this.context = context;
        this.lists = lists;
        this.iClickCurrencyUnit = iClickCurrencyUnit;

        String savedCode = getSelectedCurrencyCode(context);
        for (int i = 0; i < lists.size(); i++) {
            if (lists.get(i).getSymbol().equals(savedCode)) {
                selectedPosition = i;
                break;
            }
        }
    }

    @NonNull
    @Override
    public CurrencyUnitViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCurencyUnitBinding binding = ItemCurencyUnitBinding.inflate(LayoutInflater.from(context), parent, false);
        return new CurrencyUnitViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyUnitViewHolder holder, int position) {
        CurrencyUnitModel data = lists.get(position);
        holder.bind(data, position == selectedPosition);

        holder.binding.rlItem.setOnClickListener(view -> {
            int previousPosition = selectedPosition;
            selectedPosition = holder.getAdapterPosition();

            saveSelectedCurrencyCode(context, data.getSymbol());
            notifyCurrencyChanged(context);
            notifyItemChanged(previousPosition);
            notifyItemChanged(selectedPosition);

            iClickCurrencyUnit.onClick(data);
        });
    }


    private void notifyCurrencyChanged(Context context) {
        Intent intent = new Intent("CURRENCY_CHANGED");
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }
    @Override
    public int getItemCount() {
        return lists.size();
    }

    public static class CurrencyUnitViewHolder extends RecyclerView.ViewHolder {
        private final ItemCurencyUnitBinding binding;

        public CurrencyUnitViewHolder(ItemCurencyUnitBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(CurrencyUnitModel data, boolean isSelected) {
            binding.ivAvatar.setImageDrawable(ContextCompat.getDrawable(binding.getRoot().getContext(), data.getImage()));
            binding.tvName.setText(data.getLanguageName());
            binding.tvCode.setText(data.getCode());
            binding.v2.setVisibility(isSelected ? View.VISIBLE : View.GONE);
            binding.rlItem.setBackground(isSelected ? ContextCompat.getDrawable(context, R.drawable.bg_item_currency_true) : ContextCompat.getDrawable(context, R.drawable.bg_item_currency));
        }
    }
}
