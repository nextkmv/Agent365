package ru.its_365.agent365.activitys.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.HashSet;
import java.util.Set;

import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;
import io.realm.RealmObject;
import ru.its_365.agent365.R;


public class RealmListViewAdapter extends RealmBaseAdapter<RealmObject> implements ListAdapter {

    private static class ViewHolder {
        TextView content;
    }

    private View.OnClickListener _listener = null;

    private boolean inDeletionMode = false;
    private Set<String> countersToDelete = new HashSet<String>();

    public RealmListViewAdapter(OrderedRealmCollection<RealmObject> realmResults, View.OnClickListener listener) {
        super(realmResults);
        _listener = listener;
    }

    public RealmListViewAdapter(OrderedRealmCollection<RealmObject> realmResults) {
        super(realmResults);
    }

    void enableDeletionMode(boolean enabled) {
        inDeletionMode = enabled;
        if (!enabled) {
            countersToDelete.clear();
        }
        notifyDataSetChanged();
    }

    Set<String> getCountersToDelete() {
        return countersToDelete;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            convertView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.fragment_customer, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.content = (TextView) convertView.findViewById(R.id.title);
            if(_listener != null){
                viewHolder.content.setOnClickListener(_listener);
            }
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (adapterData != null) {
            final RealmObject item = adapterData.get(position);
            viewHolder.content.setText(item.toString());
            viewHolder.content.setTag(item);
        }
        return convertView;
    }
}

