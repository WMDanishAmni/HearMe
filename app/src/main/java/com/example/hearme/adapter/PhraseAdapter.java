// file: PhraseAdapter.java
package com.example.hearme.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.example.hearme.models.PhraseItem;

import java.util.List;

public class PhraseAdapter extends ArrayAdapter<PhraseItem> {
    public PhraseAdapter(Context context, List<PhraseItem> items) {
        super(context, 0, items);
    }

    @Override
    public int getViewTypeCount() {
        return 2; // Header and Phrase
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        PhraseItem item = getItem(position);
        LayoutInflater inflater = LayoutInflater.from(getContext());

        if (item.getType() == PhraseItem.TYPE_HEADER) {
            // Inflate header layout
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView headerText = convertView.findViewById(android.R.id.text1);
            headerText.setText(item.getText());
            headerText.setTextAppearance(getContext(), android.R.style.TextAppearance_Medium);
            headerText.setTextColor(Color.BLACK);
            // INCREASE PADDING FOR HEADER
            headerText.setPadding(50, 24, 32, 16); // left, top, right, bottom
        } else {
            // Inflate phrase layout
            if (convertView == null) {
                convertView = inflater.inflate(android.R.layout.simple_list_item_1, parent, false);
            }
            TextView phraseText = convertView.findViewById(android.R.id.text1);
            phraseText.setText(item.getText());
            // INCREASE PADDING FOR PHRASE
            phraseText.setPadding(100, 20, 32, 20); // left, top, right, bottom
        }

        return convertView;
    }
}