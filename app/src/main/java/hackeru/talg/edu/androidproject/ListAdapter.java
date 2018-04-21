package hackeru.talg.edu.androidproject;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Tal on 01-Apr-18.
 */

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {
    private String[] smsScheduleList;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView datetime;
        public ViewHolder(View v) {
            super(v);
            datetime = (TextView) v.findViewById(R.id.tvListItemDateTime);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public ListAdapter(String[] list) {
        this.smsScheduleList = list;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                     int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item, parent, false);

        return new ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String text = smsScheduleList[position];
        ListItem item = new ListItem(text);
        holder.datetime.setText(item.getDatetime());
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        int i;
        for (i = 0; i < smsScheduleList.length; i++) {
            if (smsScheduleList[i] == "") {
                return i;
            }
        }
        return i;
    }
}
