package com.example.alex.amalgamasongs;

import android.app.Activity;
import android.text.Html;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Адаптер для преставления элементов в виде текста
 */
public class TextItemsAdapter<Item> extends ArrayAdapter<Item> {

    private Activity mActivity;
    private int mLayoutRes;

    public TextItemsAdapter(Activity activity, int layoutRes, ArrayList<Item> items) {
        super(activity, 0, items);
        mLayoutRes = layoutRes;
        mActivity = activity;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = mActivity.getLayoutInflater().inflate(mLayoutRes, parent, false);
        }

        TextView textView = (TextView) convertView.findViewById(R.id.item_text_view);
        textView.setText(Html.fromHtml(getItem(position).toString()));

        return convertView;
    }
}