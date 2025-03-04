package com.moneytracker.aimoney.moneymanager.finance.utils.language;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;

import com.moneytracker.aimoney.moneymanager.finance.R;
import com.moneytracker.aimoney.moneymanager.finance.adapter.LanguageModel;
import com.moneytracker.aimoney.moneymanager.finance.databinding.LayoutLanguageCustomBinding;

import java.util.ArrayList;

public class UILanguageCustom extends RelativeLayout implements LanguageCustomAdapter.OnItemClickListener {
    private LanguageCustomAdapter adapterEng;

    private LanguageCustomAdapter adapterPor;
    boolean isVisibleHindi = false;
    boolean isVisibleEng = false;

    boolean isVisiblePor = false;
    private LanguageCustomAdapter adapterHindi;
    private Context context;
    private final ArrayList<LanguageModel> dataEng = new ArrayList<>();

    private final ArrayList<LanguageModel> dataPor = new ArrayList<>();

    private final ArrayList<LanguageModel> dataHindi = new ArrayList<>();
    private OnItemClickListener onItemClickListener;
    private LayoutLanguageCustomBinding binding;

    private boolean isItemLanguageSelected = false;

    public UILanguageCustom(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public UILanguageCustom(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        initView();
    }

    private void initView() {

        binding = LayoutLanguageCustomBinding.inflate(LayoutInflater.from(context), this, true);
        binding.languageES.ivAvatar.setImageResource(R.drawable.flag_es);
        binding.languageES.tvTitle.setText("Spanish");

        binding.languageHindi.ivAvatar.setImageResource(R.drawable.flag_hi);
        binding.languageHindi.tvTitle.setText("Spanish");

        adapterEng = new LanguageCustomAdapter(dataEng);
        adapterEng.setOnItemClickListener(this);
        binding.rcvLanguageCollap1.setAdapter(adapterEng);

        adapterHindi = new LanguageCustomAdapter(dataHindi);
        adapterHindi.setOnItemClickListener(this);
        binding.rcvLanguageCollap2.setAdapter(adapterHindi);

        binding.languageHindi.imgCountries.setVisibility(GONE);
        binding.languageHindi.animHand.setVisibility(GONE);
        binding.languagePor.animHand.setVisibility(GONE);

        binding.languagePor.imgCountries.setImageResource(R.drawable.img_por);

        adapterPor = new LanguageCustomAdapter(dataPor);
        adapterPor.setOnItemClickListener(this);
        binding.rcvLanguageCollap3.setAdapter(adapterPor);
        binding.languageES.llNotColap.setOnClickListener(v -> {
            binding.languageES.imgSelected.setImageResource(R.drawable.ic_checked_language);
            binding.languageFR.imgSelected.setImageResource(R.drawable.ic_unchecked_language);
            adapterPor.unselectAll();
            adapterEng.unselectAll();
            adapterHindi.unselectAll();
            isItemLanguageSelected=true;
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(0,isItemLanguageSelected);
            }

            binding.languageEnglishCollapse.animHand.setVisibility(GONE);
        });
        binding.languageFR.llNotColap.setOnClickListener(v -> {
            binding.languageES.imgSelected.setImageResource(R.drawable.ic_unchecked_language);
            binding.languageFR.imgSelected.setImageResource(R.drawable.ic_checked_language);
            adapterPor.unselectAll();
            adapterEng.unselectAll();
            adapterHindi.unselectAll();
            isItemLanguageSelected=true;
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(0,isItemLanguageSelected);
            }
            binding.languageEnglishCollapse.animHand.setVisibility(GONE);
        });

        binding.languageHindi.tvTitle.setText("Hindi");
        binding.languageFR.tvTitle.setText("French");
        binding.languagePor.tvTitle.setText("Portuguese");
        binding.languageFR.ivAvatar.setImageResource(R.drawable.flag_fr);
        binding.languagePor.ivAvatar.setImageResource(R.drawable.flag_portugese);
        binding.languageHindi.itemCollap.setOnClickListener(v -> {
            isVisibleHindi = !isVisibleHindi;
            binding.rcvLanguageCollap2.setVisibility(isVisibleHindi ? View.VISIBLE : View.GONE);
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(0,isItemLanguageSelected);
            }
            binding.languageEnglishCollapse.animHand.setVisibility(GONE);
        });
        binding.languageEnglishCollapse.itemCollap.setOnClickListener(v -> {
            isVisibleEng = !isVisibleEng;
            binding.rcvLanguageCollap1.setVisibility(isVisibleEng ? View.VISIBLE : View.GONE);
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(0,isItemLanguageSelected);
            }
            binding.languageEnglishCollapse.animHand.setVisibility(GONE);

        });

        binding.languagePor.itemCollap.setOnClickListener(v -> {
            isVisiblePor = !isVisiblePor;
            binding.rcvLanguageCollap3.setVisibility(isVisiblePor ? View.VISIBLE : View.GONE);
            if (onItemClickListener != null) {
                onItemClickListener.onItemClickListener(0,isItemLanguageSelected);
            }
            binding.languageEnglishCollapse.animHand.setVisibility(GONE);
        });

    }

    public void upDateData(ArrayList<LanguageModel> dataEng1, ArrayList<LanguageModel> hindi, ArrayList<LanguageModel> dataPor1) {
        dataPor.clear();
        dataHindi.clear();
        dataEng.clear();
        if (dataPor1 != null && !dataPor1.isEmpty()) {
            dataPor.addAll(dataPor1);
        }
        if (hindi != null && !hindi.isEmpty()) {
            dataHindi.addAll(hindi);
        }
        if (dataEng1 != null && !dataEng1.isEmpty()) {
            dataEng.addAll(dataEng1);
        }
        adapterPor.notifyDataSetChanged();
        adapterHindi.notifyDataSetChanged();
        adapterEng.notifyDataSetChanged();

    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onItemNewClick(int position, LanguageModel itemTabModel) {
        isItemLanguageSelected = true;
        if (onItemClickListener != null) {
            onItemClickListener.onItemClickListener(position,isItemLanguageSelected);
        }
        binding.languageFR.imgSelected.setImageResource(R.drawable.ic_unchecked_language);
        binding.languageES.imgSelected.setImageResource(R.drawable.ic_unchecked_language);
    }

    @Override
    public void onPreviousPosition(int pos) {
        if (onItemClickListener != null) {
            onItemClickListener.onPreviousPosition(pos);
        }
    }

    public interface OnItemClickListener {
        void onItemClickListener(int position , boolean isItemLanguageSelected);

        void onPreviousPosition(int pos);
    }
}

