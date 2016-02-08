package net.donky.geo;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.donky.R;
import net.donky.location.geofence.DonkyGeoFenceController;
import net.donky.location.geofence.model.DonkyGeoFenceDataCallback;
import net.donky.core.network.location.GeoFence;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class GeoFenceFragment extends BaseFragment {

    @InjectView(R.id.geo_fences_listview)
    ListView geoListView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.geofence_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init(){
        DonkyGeoFenceController.getInstance().getAllGeoFences(new DonkyGeoFenceDataCallback<List<GeoFence>>() {
            @Override
            public void success(List<GeoFence> data) {
                final GeoFenceAdapter adapter = new GeoFenceAdapter(getActivity(), new ArrayList<GeoFence>(data));
                geoListView.setAdapter(adapter);
                geoListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getActivity(), GeoFenceDetailsActivity.class);
                        intent.putExtra(GeoFenceDetailsActivity.GEO_TYPE, "geo_fence");
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(GeoFenceDetailFragment.GEOFENCE, adapter.getItem(position));
                        intent.putExtras(bundle);
                        startActivity(intent);

                    }
                });
            }

            @Override
            public void error(String message) {

            }
        });
    }

    private class GeoFenceAdapter extends ArrayAdapter<GeoFence>{

        public GeoFenceAdapter(Context context, ArrayList<GeoFence> geoFences){
            super(context, 0, geoFences);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.geo_fence_list_item, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }
            GeoFence geoFence = getItem(position);
            holder.name.setText(geoFence.getName());

            StringBuffer buffer = new StringBuffer(geoFence.getLabels().length);
            for (int i = 0; i < geoFence.getLabels().length; ++i) {
                buffer.append(geoFence.getLabels()[i]);
                if (i != geoFence.getLabels().length - 1)
                    buffer.append(", ");
            }
            holder.labels.setText(buffer.toString());
            holder.createdOn.setText(geoFence.getCreatedOn());
            holder.status.setText(geoFence.getStatus());
            return view;
        }
    }

    static class ViewHolder{
        @InjectView(R.id.geo_name)
        TextView name;
        @InjectView(R.id.geo_labels)
        TextView labels;
        @InjectView(R.id.geo_status)
        TextView status;
        @InjectView(R.id.geo_created_on)
        TextView createdOn;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }
}
