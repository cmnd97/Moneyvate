package com.cmnd97.moneyvate;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class ProfileFragment extends Fragment {


    private OnFragmentInteractionListener mListener;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_profile, container, false);


        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    public void setUserName(String firstName, String lastName) {
        TextView tv = (TextView) (getActivity().findViewById(R.id.profile_tv));
        tv.setText(firstName + " " + lastName + " is signed in.");
    }

    interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    void setUpTask() {
        AutoCompleteTextView locView = (AutoCompleteTextView) getActivity().findViewById(R.id.locView);
        final EditText dateView = (EditText) getActivity().findViewById(R.id.dateView);
        final EditText timeView = (EditText) getActivity().findViewById(R.id.timeView);
        locView.setText("");
        dateView.setText("");
        timeView.setText("");
        ((TextView) getActivity().findViewById(R.id.task_creation_result)).setText("");


        //for date
        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener date = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear,
                                  int dayOfMonth) {

                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);

                String dateFormat = "yyyy-MM-dd";
                SimpleDateFormat sdf = new SimpleDateFormat(dateFormat, Locale.getDefault());
                dateView.setText(sdf.format(calendar.getTime()));
            }

        };

        dateView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                DatePickerDialog dp = new DatePickerDialog(getActivity(), date, calendar
                        .get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dp.getDatePicker().setMinDate(calendar.getTimeInMillis() + 1000 * 3600 * 24);
                calendar.add(Calendar.MONTH, 2);
                long oneMonth = calendar.getTimeInMillis();
                dp.getDatePicker().setMaxDate(oneMonth);
                dp.show();
            }
        });


        //for time

        timeView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                TimePickerDialog tp = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        timeView.setText(String.format("%02d:%02d:00", selectedHour, selectedMinute));
                    }
                }, hour, minute, true);
                tp.setTitle("Select Time");
                tp.show();

            }
        });

        //for location
        ArrayList<Tag> tags = ((MainActivity) getActivity()).getStoredTags();
        String[] locations = new String[tags.size()];
        for (int i = 0; i < tags.size(); i++) {
            locations[i] = tags.get(i).description;

        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_dropdown_item_1line, locations);
        locView.setAdapter(adapter);

    }


}
