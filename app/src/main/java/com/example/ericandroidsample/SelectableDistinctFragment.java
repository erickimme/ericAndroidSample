package com.example.ericandroidsample;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;

/**
 * A fragment displaying an option to set the channel as Distinct.
 */
public class SelectableDistinctFragment extends Fragment {
    private CheckBox mCheckBox;
    private DistinctSelectedListener mListener;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_selectable_distinct, container, false);

        ((CreateGroupChannelActivity) getActivity()).setState(CreateGroupChannelActivity.STATE_SELECT_DISTINCT);

        mListener = (CreateGroupChannelActivity) getActivity();

        mCheckBox = (CheckBox) rootView.findViewById(R.id.checkbox_select_distinct);
        mCheckBox.setChecked(true);
        mCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mListener.onDistinctSelected(isChecked);
            }
        });

        return rootView;
    }

    interface DistinctSelectedListener {
        void onDistinctSelected(boolean distinct);
    }

    static SelectableDistinctFragment newInstance() {
        return new SelectableDistinctFragment();
    }



}

