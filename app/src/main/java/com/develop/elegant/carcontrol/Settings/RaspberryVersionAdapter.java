package com.develop.elegant.carcontrol.Settings;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

import com.develop.elegant.carcontrol.DataSQLite.Models.RaspberryVersionTable;
import com.develop.elegant.carcontrol.R;

import java.util.ArrayList;

public class RaspberryVersionAdapter extends BaseExpandableListAdapter implements ExpandableListView.OnGroupClickListener {

    Context cntx;

    ArrayList<RaspberryVersionTable> menu_options = new ArrayList<>();

    RaspberryVersionTable selected_option;

    int group_collapse_height = -1;

    public RaspberryVersionAdapter(Context _cntx, ArrayList<RaspberryVersionTable> rasberryVersionList) {

        this.cntx = _cntx;
        this.menu_options = rasberryVersionList;

    }


    @Override
    public int getGroupCount() {
        return 1;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return menu_options.size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.selected_option;
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return menu_options.get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int parent, boolean isExpanded, View convertView, ViewGroup parentView) {
        if(convertView == null) {
            LayoutInflater inflater = (LayoutInflater) cntx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.version_row, parentView, false);
        }

        //Log.d(TAG, "----------------------> getGroupView: group_title = '" + group_title + "'" + " | selected_option = '" + selected_option + "'");

        TextView parent_expand_coin_text = (TextView) convertView.findViewById(R.id.versionTextView);
        parent_expand_coin_text.setText(selected_option.getName() + " (" + selected_option.getYear() + ")");

        return convertView;
    }

    @Override
    public View getChildView(int parent, int child, boolean lastChild, View convertView, ViewGroup parentView) {
        RaspberryVersionTable child_model = (RaspberryVersionTable) getChild(parent, child);

        if(child_model != null) {

            if (convertView == null) {
                LayoutInflater inflater = (LayoutInflater) cntx.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                convertView = inflater.inflate(R.layout.version_row, parentView, false);
            }

            TextView child_expand_coin_text = (TextView) convertView.findViewById(R.id.versionTextView);
            child_expand_coin_text.setText(child_model.getName() + " (" + child_model.getYear() + ")");
        }

        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {

        if(!parent.isGroupExpanded(groupPosition)) {
            //Log.d(TAG, "------------> expand group");

            if(group_collapse_height == -1) {
                group_collapse_height = parent.getMeasuredHeight();
            }
            expandList(parent, groupPosition);
        }
        else {
            //Log.d(TAG, "------------> collapse group");
            collapseList(parent, groupPosition);
        }

        return true;
    }

    public void collapseList(ExpandableListView parent, int groupPosition) {
        parent.getLayoutParams().height = group_collapse_height;
        parent.collapseGroup(groupPosition);
        parent.collapseGroup(groupPosition);
    }

    public void expandList(ExpandableListView parent, int groupPosition) {
        int height = 0;
        /*height += parent.getChildAt(0).getMeasuredHeight();
        height += parent.getDividerHeight()*2;
        int childrenCount = getChildrenCount(groupPosition) + groupPosition;
        if (childrenCount == 1) childrenCount = 2;

        //Log.d(TAG, "----------------> expandList: childrenCount = " + childrenCount + " | children_height = " + parent.getChildAt(0).getMeasuredHeight() + " | height = " + height*childrenCount);

        parent.getLayoutParams().height = height * childrenCount;

        parent.expandGroup(groupPosition);(*/
        View listItem = getChildView(groupPosition, 0, false, null, parent);
        listItem.measure(0, 0);
        height += listItem.getMeasuredHeight();
        height += parent.getDividerHeight()*2;
        height = height * (getChildrenCount(groupPosition)+1);
        parent.getLayoutParams().height = height;
        parent.expandGroup(groupPosition);
    }

    public void setSelectedOption(RaspberryVersionTable selected_option) {
        this.selected_option = selected_option;
    }

    public RaspberryVersionTable getSelectedOption() {
        return this.selected_option;
    }
}
