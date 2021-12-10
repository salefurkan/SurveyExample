package example.methods.surveyexample.adapter;


import android.content.Context;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.RadioButton;

import java.util.ArrayList;
import java.util.List;

import example.methods.surveyexample.R;
import example.methods.surveyexample.model.Votes;

public class SurveyAdapter extends BaseAdapter {
    private final List<Votes> mBookList = new ArrayList<>();

    private final SparseBooleanArray mCheckState = new SparseBooleanArray();

    private final LayoutInflater mInflater;

    private boolean mIsMultipleMode = false;

    public SurveyAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
    }

    void addBooks(List<Votes> bookInfoList) {
        mBookList.clear();
        mBookList.addAll(bookInfoList);
        notifyDataSetChanged();
    }

    void deleteBooks(List<Votes> bookInfoList) {
        for (Votes bookInfo : bookInfoList) {
            mBookList.remove(bookInfo);
        }
        notifyDataSetChanged();
    }

    void check(int position) {
        if (mCheckState.get(position)) {
            mCheckState.delete(position);
        } else {
            mCheckState.put(position, true);
        }
        notifyDataSetChanged();
    }

    void onSelectAllMenuClicked() {
        // Current state is all elements selected, so switch the state to none selected
        if (mCheckState.size() == getCount()) {
            mCheckState.clear();
        } else {
            mCheckState.clear();
            for (int i = 0; i < getCount(); i++) {
                mCheckState.put(i, true);
            }
        }
        notifyDataSetChanged();
    }

    boolean isAllSelected() {
        return mCheckState.size() == mBookList.size();
    }

    List<Votes> getSelectedItems() {
        List<Votes> bookInfoList = new ArrayList<>();
        int selectCount = mCheckState.size();
        for (int i = 0; i < selectCount; i++) {
            bookInfoList.add(mBookList.get(mCheckState.keyAt(i)));
        }
        return bookInfoList;
    }

    boolean isMultipleMode() {
        return mIsMultipleMode;
    }

    void setMultipleMode(boolean isMultipleMode) {
        mIsMultipleMode = isMultipleMode;
        if (!isMultipleMode) {
            mCheckState.clear();
        }
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return mBookList.size();
    }

    @Override
    public Object getItem(int position) {
        return mBookList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.activity_main, null);
            viewHolder.yes = convertView.findViewById(R.id.yesAnswer);
            viewHolder.not = convertView.findViewById(R.id.notAnswer);
            viewHolder.button = convertView.findViewById(R.id.report);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        Votes surveyInfo = mBookList.get(position);
        /*viewHolder.authorView.setText(surveyInfo.get());
        viewHolder.bookNameView.setText(surveyInfo.getBookName());*/
        return convertView;
    }

    private static final class ViewHolder {
        RadioButton yes, not;
        Button button;
    }
}