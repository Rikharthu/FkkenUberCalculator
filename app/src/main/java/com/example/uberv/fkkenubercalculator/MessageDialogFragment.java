package com.example.uberv.fkkenubercalculator;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;


public class MessageDialogFragment extends DialogFragment {
    public static final String KEY_MESSAGE = "KEY_MESSAGE";

    public MessageDialogFragment(){

    }

    public static MessageDialogFragment newInstance(String text){
        MessageDialogFragment fragment = new MessageDialogFragment();
        Bundle args = new Bundle();
        args.putString(KEY_MESSAGE,text);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        String message=getArguments().getString(KEY_MESSAGE);
        alertDialogBuilder.setMessage(message);
        alertDialogBuilder.setPositiveButton("OK",  new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        return alertDialogBuilder.create();
    }


}
