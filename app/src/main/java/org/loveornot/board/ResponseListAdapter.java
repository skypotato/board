package org.loveornot.board;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by hunso on 2016-10-29.
 */

class ResponseListAdapter extends BaseAdapter {
    final private Activity context;
    private ArrayList<Board> items = new ArrayList<>();

    ResponseListAdapter(Activity context) {
        this.context = context;
    }

    void addList(ArrayList<Board> boards){
        this.items = boards;
    }

    void addAll(ArrayList<Board> boards){
        items.addAll(boards);
    }

    public void addItem(Board item) {
        items.add(item);
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = context.getLayoutInflater();
        View view = inflater.inflate(R.layout.listitem_response, null, true);

        TextView name = (TextView) view.findViewById(R.id.responseData01);
        TextView content = (TextView) view.findViewById(R.id.responseData02);
        TextView date = (TextView) view.findViewById(R.id.responseData03);

        name.setText(items.get(position).getName());
        content.setText(items.get(position).getContent());
        date.setText(items.get(position).getDate());
        return view;
    }
}
