package com.cmnd97.moneyvate;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class ScanFragment extends Fragment {

    private TextView messageView;
    private ImageView circle;
    private NfcAdapter NFCAdapter;
    private OnFragmentInteractionListener mListener;

    public ScanFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.fragment_scan, container, false);

        messageView = (TextView) rootView.findViewById(R.id.message);
        NFCAdapter = NfcAdapter.getDefaultAdapter(getActivity());
        circle = (ImageView) rootView.findViewById(R.id.circle);

        BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

            @Override
            public void onReceive(Context context, Intent intent) {
                //     findViewById(R.id.nfc_tip).setVisibility(NFCAdapter.isEnabled() ? View.GONE : View.VISIBLE);
                toggleTip(NFCAdapter.isEnabled());
            }
        };
        getActivity().registerReceiver(broadcastReceiver, new IntentFilter("android.nfc.action.ADAPTER_STATE_CHANGED"));


        return rootView;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    @Override
    public void onResume() {
        super.onResume();
        toggleTip(NFCAdapter.isEnabled());
    }

    void toggleTip(boolean isNFCEnabled) {
        if (getActivity().findViewById(R.id.nfc_tip).getVisibility() == View.VISIBLE) {
            setCircleImage(isNFCEnabled ? "ON" : "OFF");
            TextView tv = (TextView) (getActivity().findViewById(R.id.approach_tag));
            tv.setText(isNFCEnabled ? "Approach a NFC tag" : "NFC is Disabled");
        }

        getActivity().findViewById(R.id.nfc_tip).setVisibility(isNFCEnabled ? View.GONE : View.VISIBLE);
        if (getActivity().findViewById(R.id.nfc_tip).getVisibility() == View.VISIBLE) {
            TextView tv = (TextView) (getActivity().findViewById(R.id.message));
            tv.setText("");
        }

    }

    void setCircleImage(String circleState) {
        switch (circleState) {
            case "?":
                circle.setImageResource(R.drawable.potato);
                break;
            case "V":
                circle.setImageResource(R.drawable.tick);
                break;
            case "X":
                circle.setImageResource(R.drawable.cross);
                break;
            case "OFF":
                circle.setImageResource(R.drawable.logo_off);
                break;
            case "ON":
                circle.setImageResource(R.drawable.logo);
                break;
        }
    }

    void setTagName(String tagName) {
        messageView.setText("Location:" + "\n" + tagName);

    }

    void setServerMessage(String serverMessage) {
        messageView.append("\n\n" + serverMessage);
    }

    void sendTagToServer(String result) {

        if (result.split("-")[0].equals("Moneyvate"))
            ((MainActivity) getActivity()).sendTag(result.split("-")[1]);
        else {
            messageView.setText("Tag not understood");
            setCircleImage("X");

        }

    }

}
