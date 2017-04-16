package com.sibilantsolutions.grisonforandroid;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CamListFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable
            Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_cam_list, container, false);
        final RecyclerView recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 2));
        List<String> names = new ArrayList<>(Arrays.asList("hi", "bye", "foo", "bar", "baz"));
        recyclerView.setAdapter(new MyAdapter(names));
        return view;
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {

        private final List<String> names;

        MyAdapter(List<String> names) {
            this.names = names;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                    .main_cam_tile, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.bind(names.get(position));
        }

        @Override
        public int getItemCount() {
            return names.size();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView mCamNameText;

        MyViewHolder(View itemView) {
            super(itemView);

            mCamNameText = (TextView) itemView.findViewById(R.id.cam_name_text);
        }

        void bind(String name) {
            mCamNameText.setText(name);
        }

    }
}
