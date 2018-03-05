package com.cmnd97.moneyvate;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by cristi-mnd on 15.02.18.
 * <p>
 * This class is used to create an adapter for the main listview
 */

public class TaskAdapter extends ArrayAdapter<Task> {

    TaskAdapter(Context context, ArrayList<Task> tasks) {
        super(context, 0, tasks);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View listItemView = convertView;


        if (listItemView == null) {
            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        Task currentItem = getItem(position);

        ((TextView) listItemView.findViewById(R.id.task_id_tv)).setText(currentItem.id);
        ((TextView) listItemView.findViewById(R.id.task_location_tv)).setText(currentItem.tag.description);
        ((TextView) listItemView.findViewById(R.id.task_deadline_tv)).setText(currentItem.deadline);
        ((TextView) listItemView.findViewById(R.id.task_status_tv)).setText(currentItem.status);
        currentItem.setDirectionsView((ImageView) listItemView.findViewById(R.id.task_directions_iv));
        setDirectionListener(currentItem);


        return listItemView;
    }
    void setDirectionListener(final Task task) {

        task.getDirectionsView().setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
            //    Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/dir/?api=1&destination=" + task.tag.loc_lat + "," + task.tag.loc_long + "&travelmode=walking"));
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("geo:" + task.tag.loc_lat + "," + task.tag.loc_long + "?q=" +  task.tag.loc_lat + "," +  task.tag.loc_long ));
                mapIntent.setPackage("com.google.android.apps.maps");
                getContext().startActivity(mapIntent);
            }
        });
    }
}
