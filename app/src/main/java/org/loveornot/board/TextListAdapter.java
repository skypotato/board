package org.loveornot.board;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class TextListAdapter extends BaseAdapter {
    final private Activity context;
    ArrayList<Board> items = new ArrayList<Board>();

    public TextListAdapter(Activity context) {
        this.context = context;
    }

    public void addList(ArrayList<Board> boards){
        this.items = boards;
    }

    public void addAll(ArrayList<Board> boards){
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
        View view = inflater.inflate(R.layout.listitem, null, true);

        TextView title = (TextView) view.findViewById(R.id.dataItem01);
        TextView name = (TextView) view.findViewById(R.id.dataItem02);
        TextView hit = (TextView) view.findViewById(R.id.dataItem03);

        title.setText(items.get(position).getTitle());
        name.setText("작성자 : "+items.get(position).getId());
        hit.setText(items.get(position).getHit());
        return view;
    }
}