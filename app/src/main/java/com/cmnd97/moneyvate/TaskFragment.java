package com.cmnd97.moneyvate;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;

import java.util.ArrayList;

public class TaskFragment extends Fragment {

    ArrayList<Tag> tags = new ArrayList<>();
    ArrayList<Task> tasks = new ArrayList<>();
    LinearLayout taskZone;
    TaskAdapter adapter;
    private OnFragmentInteractionListener mListener;

    public TaskFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_task, container, false);
        taskZone = (LinearLayout) rootView.findViewById(R.id.task_zone);
        adapter = new TaskAdapter(getActivity(), tasks);
        inflateTasks();
        return rootView;

    }

    public View inflateTasks() {
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rootView = inflater.inflate(R.layout.task_list, null);

        ListView taskList = (ListView) rootView.findViewById(R.id.task_list_view);
        taskZone.addView(taskList);
        taskList.setAdapter(adapter);
        return rootView;
    }

    public ArrayList<Tag> getStoredTags() {
        return tags;
    }

    public void clearEntries() {
        //  tasks.clear();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public void storeLocations(String[] fromServer) {
        // tag id, description, loc_lat, loc_long
        //    0         1          2        3   =>length = 4*nr_locs - 1

        //compute how many locations we got
        int nr_locs = (fromServer.length + 1) / 4;

        for (int i = 0; i < nr_locs; i++)
            tags.add(new Tag(fromServer[4 * i], fromServer[4 * i + 1], fromServer[4 * i + 2], fromServer[4 * i + 3]));


    }

    public void storeTasks(String[] fromServer) {


        tasks.clear();
        adapter.notifyDataSetChanged();

        // task id, tag_id, status, deadline
        //    0         1        2      3     =>length =4*nr_tags -1
        int nr_tasks = (fromServer.length + 1) / 4;
        for (int i = 0; i < nr_tasks; i++) {
            for (int j = 0; j < tags.size(); j++)
                if (fromServer[4 * i + 1].equals(tags.get(j).id)) {
                    tasks.add(new Task(fromServer[4 * i], fromServer[4 * i + 2], fromServer[4 * i + 3], tags.get(j)));
                    break;
                }

        }

        if (tasks.size() == 0)
            getActivity().findViewById(R.id.no_results).setVisibility(View.VISIBLE);
        else getActivity().findViewById(R.id.no_results).setVisibility(View.GONE);

    }

}
