package cn.refactor.kmpautotextview;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.text.Editable;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * 作者 : andy
 * 日期 : 15/10/26 10:50
 * 邮箱 : andyxialm@gmail.com
 * 描述 : 实现KMP算法的AutoCompleteTextView, 用于字符串模糊匹配
 */
public class KMPAutoComplTextView extends AutoCompleteTextView {

    private static final int DEFAULT_HIGHLIGHT       = Color.parseColor("#FF4081");
    private static final int DEFAULT_TEXTCOLOR       = Color.parseColor("#80000000");
    private static final int DEFAULT_TEXT_PIXEL_SIZE = 14;

    private float mTextSize;
    private boolean mIsIgnoreCase;
    private KMPAdapter mAdapter;

    private ColorStateList mHighLightColor, mTextColor;
    private List<PopupTextBean> mSourceDatas, mTempDatas;
    private OnPopupItemClickListener mListener;

    public KMPAutoComplTextView(Context context) {
        this(context, null);
    }

    public KMPAutoComplTextView(Context context, AttributeSet attrs) {
        this(context, attrs, android.R.attr.autoCompleteTextViewStyle);
    }

    public KMPAutoComplTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet attrs) {
        if (attrs != null) {
            final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.KMPAutoComplTextView);
            mTextColor = a.getColorStateList(R.styleable.KMPAutoComplTextView_completionTextColor);
            mHighLightColor = a.getColorStateList(R.styleable.KMPAutoComplTextView_completionHighlightColor);
            mTextSize = a.getDimensionPixelSize(R.styleable.KMPAutoComplTextView_completionTextSize, DEFAULT_TEXT_PIXEL_SIZE);
            mIsIgnoreCase = a.getBoolean(R.styleable.KMPAutoComplTextView_completionIgnoreCase, false);
            a.recycle();
        }
        initListener();
    }

    private void initListener() {

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                onInputTextChanged(s.toString());
            }
        });

    }

    private void onInputTextChanged(String input) {
        matchResult(input);

        if (mAdapter.mList.size() == 0) {
            KMPAutoComplTextView.this.dismissDropDown();
            return;
        }
        mAdapter.notifyDataSetChanged();

        if (!KMPAutoComplTextView.this.isPopupShowing() || mAdapter.mList.size() > 0) {
            showDropDown();
        }

    }

    /**
     * 设置数据集
     *
     * @param strings
     */
    public void setDatas(final List<String> strings) {
        mAdapter = new KMPAdapter(getContext(), getResultDatas(strings));
        setAdapter(mAdapter);
    }

    public void setOnPopupItemClickListener(OnPopupItemClickListener listener) {
        mListener = listener;
        this.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (mListener == null) {
                    return;
                }
                mListener.onPopupItemClick(KMPAutoComplTextView.this.getText().toString());
            }
        });

    }

    private void matchResult(String input) {
        List<PopupTextBean> datas = mSourceDatas;
        if (TextUtils.isEmpty(input) || datas == null || datas.size() == 0) {
            return;
        }

        List<PopupTextBean> newDatas = new ArrayList<PopupTextBean>();
        List<String> newDataStrings = new ArrayList<String>();
        for (PopupTextBean resultBean : datas) {
            int matchIndex = matchString(resultBean.mTarget, input, mIsIgnoreCase);
            if (-1 != matchIndex) {
                PopupTextBean bean = new PopupTextBean(resultBean.mTarget, matchIndex, matchIndex + input.length());
                newDatas.add(bean);
                newDataStrings.add(resultBean.mTarget);
            }
        }

        mTempDatas = new ArrayList<PopupTextBean>();
        mTempDatas.clear();
        mTempDatas.addAll(newDatas);

        mAdapter.mList.clear();
        mAdapter.mList.addAll(newDataStrings);
    }


    private List<String> getResultDatas(List<String> strings) {
        if (strings == null || strings.size() == 0) {
            return null;
        }

        List<PopupTextBean> list = new ArrayList<PopupTextBean>();
        for (String target : strings) {
            list.add(new PopupTextBean(target));
        }

        mSourceDatas = new ArrayList<PopupTextBean>();
        mSourceDatas.addAll(list);
        return strings;
    }

    public void setMatchIgnoreCase(boolean ignoreCase) {
        mIsIgnoreCase = ignoreCase;
    }

    public boolean getMatchIgnoreCase() {
        return mIsIgnoreCase;
    }

    class KMPAdapter extends BaseAdapter implements Filterable {
        private List<String> mList;
        private Context mContext;
        private MyFilter mFilter;

        public KMPAdapter(Context context, List<String> list) {
            mContext = context;
            mList = new ArrayList<String>();
            mList.addAll(list);
        }

        @Override
        public int getCount() {
            return mList == null ? 0 : mList.size();
        }

        @Override
        public Object getItem(int position) {
            return mList == null ? null : mList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder = null;
            if (convertView == null) {
                holder = new ViewHolder();
                TextView tv = new TextView(mContext);
                int paddingX = DisplayUtils.dp2px(getContext(), 10.0f);
                int paddingY = DisplayUtils.dp2px(getContext(), 5.0f);
                tv.setPadding(paddingX, paddingY, paddingX, paddingY);

                holder.tv = tv;
                convertView = tv;
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            PopupTextBean bean = mTempDatas.get(position);
            SpannableString ss = new SpannableString(bean.mTarget);
            holder.tv.setTextColor(mTextColor == null ? DEFAULT_TEXTCOLOR : mTextColor.getDefaultColor());
            holder.tv.setTextSize(mTextSize == 0 ? DEFAULT_TEXT_PIXEL_SIZE : DisplayUtils.px2sp(getContext(), mTextSize));

            // Change Highlight Color
            if (-1 != bean.mStartIndex) {
                ss.setSpan(new ForegroundColorSpan(mHighLightColor == null ? DEFAULT_HIGHLIGHT : mHighLightColor.getDefaultColor()),
                        bean.mStartIndex, bean.mEndIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.tv.setText(ss);
            } else {
                holder.tv.setText(bean.mTarget);
            }

            return convertView;
        }

        @Override
        public Filter getFilter() {
            if (mFilter == null) {
                mFilter = new MyFilter();
            }
            return mFilter;
        }

        private class ViewHolder {
            TextView tv;
        }

        private class MyFilter extends Filter {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (mList == null) {
                    mList = new ArrayList<String>();
                }
                results.values = mList;
                results.count = mList.size();
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results.count > 0) {
                    notifyDataSetChanged();
                } else {
                    notifyDataSetInvalidated();
                }
            }
        }
    }

    public interface OnPopupItemClickListener {
        void onPopupItemClick(CharSequence charSequence);
    }

    /**
     * 获得字符串的next函数值
     *
     * @param mode 字符串
     * @return next函数值
     */
    private static int[] next(char[] mode) {
        int[] next = new int[mode.length];
        next[0] = -1;
        int i = 0;
        int j = -1;
        while (i < mode.length - 1) {
            if (j == -1 || mode[i] == mode[j]) {
                i ++;
                j ++;
                if (mode[i] != mode[j]) {
                    next[i] = j;
                } else {
                    next[i] = next[j];
                }
            } else {
                j = next[j];
            }
        }
        return next;
    }

    /**
     * KMP匹配字符串
     *
     * @param source       主串
     * @param modeStr      模式串
     * @param isIgnoreCase 是否忽略大小写
     * @return 若匹配成功，返回下标，否则返回-1
     */
    public int matchString(CharSequence source, CharSequence modeStr, boolean isIgnoreCase) {
        char[] modeArr = modeStr.toString().toCharArray();
        char[] sourceArr = source.toString().toCharArray();
        int[] next = next(modeArr);
        int i = 0;
        int j = 0;
        while (i <= sourceArr.length - 1 && j <= modeArr.length - 1) {
            if (isIgnoreCase) {
                if (j == -1 || sourceArr[i] == modeArr[j] || String.valueOf(sourceArr[i]).equalsIgnoreCase(String.valueOf(modeArr[j]))) {
                    i ++;
                    j ++;
                } else {
                    j = next[j];
                }
            } else {
                if (j == -1 || sourceArr[i] == modeArr[j]) {
                    i ++;
                    j ++;
                } else {
                    j = next[j];
                }
            }
        }
        if (j < modeArr.length) {
            return -1;
        } else
            return i - modeArr.length; // 返回模式串在主串中的头下标
    }

}
