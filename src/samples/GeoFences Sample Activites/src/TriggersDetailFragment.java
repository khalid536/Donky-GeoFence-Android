package net.donky.geo;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import net.donky.R;
import net.donky.core.network.location.GeoFence;
import net.donky.core.network.location.Trigger;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import butterknife.ButterKnife;
import butterknife.InjectView;

public class TriggersDetailFragment extends BaseFragment {

    @InjectView(R.id.trigger_listview)
    ListView getFenceListView;

    @InjectView(R.id.trigger_related_geo_count)
    TextView count;

    @InjectView(R.id.trigger_type)
    TextView type;

    @InjectView(R.id.trigger_condition)
    TextView condition;

    @InjectView(R.id.trigger_last_execution)
    TextView lastExecution;

    @InjectView(R.id.trigger_execution_count)
    TextView executionCount;

    @InjectView(R.id.trigger_execution_per_interval)
    TextView executionCountPerInterval;




    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.trigger_detail_fragment, container, false);
    }

    public static final String TRIGGER = "trigger";

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Trigger trigger = (Trigger)getArguments().getSerializable(TRIGGER);
        if (trigger == null)
            return;
        populateTrigger(trigger);
        init(trigger);
    }

    DateFormat formatter = new SimpleDateFormat("yyyy/MMM/dd HH:mm:ss");

    private void populateTrigger(Trigger trigger){
        type.setText(trigger.getTriggerType());
        if (trigger.getTriggerData().getRegions() != null)
            count.setText(Integer.toString(trigger.getTriggerData().getRegions().size()));
        condition.setText(trigger.getTriggerData().getCondition());
        Date date = new Date(trigger.getLastExecutionTime());
        lastExecution.setText(formatter.format(date));
        executionCount.setText(Integer.toString(trigger.getRestrictions().getExecutorCount()));
        executionCountPerInterval.setText(Integer.toString(trigger.getRestrictions().getMaximumExecutionsPerInterval()));
    }

    private void init(Trigger trigger){
        getFenceListView.setAdapter(new GeoFenceAdapter(getActivity(),
                new ArrayList<GeoFence>(trigger.getTriggerData().getRegions())));
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
