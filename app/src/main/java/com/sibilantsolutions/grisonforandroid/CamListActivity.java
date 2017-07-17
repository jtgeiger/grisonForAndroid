package com.sibilantsolutions.grisonforandroid;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.DimenRes;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sibilantsolutions.grisonforandroid.data.repository.SharedPreferencesCamDefRepositoryImpl;
import com.sibilantsolutions.grisonforandroid.domain.model.CamDef;
import com.sibilantsolutions.grisonforandroid.domain.usecase.GetAllCamDefsUseCase;
import com.sibilantsolutions.grisonforandroid.presenter.CamListContract;
import com.sibilantsolutions.grisonforandroid.presenter.CamListPresenter;

import java.util.List;

public class CamListActivity extends AppCompatActivity implements CamListContract.View {

    private CamListPresenter presenter;
    private RecyclerView recyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cam_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        presenter = new CamListPresenter(this, new GetAllCamDefsUseCase(new SharedPreferencesCamDefRepositoryImpl(this)));

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CamListActivity.this, AddCamActivity.class));
            }
        });

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);

        final int SPAN = 4;
        recyclerView.setLayoutManager(new GridLayoutManager(this, SPAN));

        recyclerView.addItemDecoration(new SpacingItemDecor(this, R.dimen.grid_item_offset, SPAN));
    }

    @Override
    protected void onStart() {
        super.onStart();
        presenter.getAllCamDefs();
    }

    @Override
    public void showAllCamDefs(List<CamDef> camDefs) {
        recyclerView.swapAdapter(new MyAdapter(camDefs), false);
    }

    @Override
    public void showError() {
        Snackbar.make(recyclerView, "Trouble getting cams", Snackbar.LENGTH_SHORT).show();
    }

    private static class MyAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private final List<CamDef> camDefs;

        public MyAdapter(List<CamDef> camDefs) {
            this.camDefs = camDefs;
            setHasStableIds(true);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            final View view = LayoutInflater.from(parent.getContext()).inflate(R.layout
                    .main_cam_tile, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.bind(camDefs.get(position));
        }

        @Override
        public int getItemCount() {
            return camDefs.size();
        }

        @Override
        public long getItemId(int position) {
            return camDefs.get(position).getId();
        }
    }

    private static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView mCamNameText;

        public MyViewHolder(View itemView) {
            super(itemView);

            mCamNameText = (TextView) itemView.findViewById(R.id.cam_name_text);
        }

        public void bind(CamDef camDef) {
            mCamNameText.setText(camDef.getName());
        }
    }

    private static class SpacingItemDecor extends RecyclerView.ItemDecoration {

        private final int itemOffsetPx;
        private final int span;

        SpacingItemDecor(@NonNull Context context, @DimenRes int itemOffsetId, int span) {
            itemOffsetPx = context.getResources().getDimensionPixelSize(itemOffsetId);
            this.span = span;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView
                .State state) {
            final int childAdapterPosition = parent.getChildAdapterPosition(view);

            if (span > 1) {
                int column = childAdapterPosition % span;
                outRect.top = 0;
                outRect.bottom = itemOffsetPx;
                outRect.left = 0;
                if (column > 0) {
                    outRect.left = itemOffsetPx;
                }
                outRect.right = 0;
            } else {
                outRect.top = 0;
                outRect.bottom = itemOffsetPx;
                outRect.left = 0;
                outRect.right = 0;
            }
        }
    }
}
