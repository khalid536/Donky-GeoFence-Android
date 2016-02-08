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
import net.donky.core.network.location.Trigger;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TriggersFragment extends BaseFragment {

    @InjectView(R.id.trigger_listview)
    ListView triggerListView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.triggers_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        init();
    }

    private void init(){
        DonkyGeoFenceController.getInstance().getAllTriggers(new DonkyGeoFenceDataCallback<List<Trigger>>() {
            @Override
            public void success(List<Trigger> data) {
                final GeoFenceAdapter adapter = new GeoFenceAdapter(getActivity(), new ArrayList<Trigger>(data));
                triggerListView.setAdapter(adapter);
                triggerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        Intent intent = new Intent(getActivity(), GeoFenceDetailsActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(TriggersDetailFragment.TRIGGER, adapter.getItem(position));
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

    private class GeoFenceAdapter extends ArrayAdapter<Trigger> {

        public GeoFenceAdapter(Context context, ArrayList<Trigger> geoFences){
            super(context, 0, geoFences);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            ViewHolder holder;
            if (view != null) {
                holder = (ViewHolder) view.getTag();
            } else {
                view = LayoutInflater.from(getActivity()).inflate(R.layout.trigger_list_item, parent, false);
                holder = new ViewHolder(view);
                view.setTag(holder);
            }
            Trigger trigger = getItem(position);
            holder.type.setText(trigger.getTriggerType());
            if (trigger.getTriggerData().getRegions() != null)
                holder.count.setText(Integer.toString(trigger.getTriggerData().getRegions().size()));
            holder.condition.setText(trigger.getTriggerData().getCondition());
            holder.lastExecution.setText(getFormattedDate(trigger.getLastExecutionTime()));
            return view;
        }
    }

    static class ViewHolder{
        @InjectView(R.id.trigger_type)
        TextView type;
        @InjectView(R.id.trigger_related_geo_count)
        TextView count;
        @InjectView(R.id.trigger_condition)
        TextView condition;
        @InjectView(R.id.trigger_last_execution)
        TextView lastExecution;

        public ViewHolder(View view) {
            ButterKnife.inject(this, view);
        }
    }

    public String getFormattedDate(long time) {
        SimpleDateFormat df = new SimpleDateFormat();
        return df.format(time);
    }
}
