package com.softeksol.paisalo.ESign.adapters;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;

import com.softeksol.paisalo.ESign.viewHolder.ESignBorrowerViewHolder;

import com.softeksol.paisalo.jlgsourcing.R;
import com.softeksol.paisalo.jlgsourcing.entities.ESignBorrower;

import java.util.List;

/**
 * Created by sachindra on 2016-10-08.
 */
public class AdapterListESignBorrower extends ArrayAdapter<ESignBorrower> {
    Context context;
    int resourecId;
    List<ESignBorrower> eSignBorrowers = null;

    public AdapterListESignBorrower(Context context, @LayoutRes int resource, @NonNull List<ESignBorrower> objects) {
        super(context, resource, objects);
        this.context = context;
        this.resourecId = resource;
        this.eSignBorrowers = objects;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        ESignBorrowerViewHolder holder;
        if (v == null) {
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            v = inflater.inflate(resourecId, parent, false);

            holder = new ESignBorrowerViewHolder();

            holder.tvBorrowerName = (TextView) v.findViewById(R.id.itemLayoutCustomerName);
            holder.tvBorrowerGuardian = (TextView) v.findViewById(R.id.itemLayoutCustomerFather);
            holder.tvCreator = (TextView) v.findViewById(R.id.itemLayoutCustomerCreator);
            holder.tvFiCode = (TextView) v.findViewById(R.id.itemLayoutCustomerFiId);
            holder.tvMobile = (TextView) v.findViewById(R.id.itemLayoutCustomerMobile);
            holder.tvAddress = (TextView) v.findViewById(R.id.itemLayoutCustomerAddress);
//            holder.tvAadhar = (TextView) v.findViewById(R.id.itemLayoutCustomer);


            v.setTag(holder);
        } else {
            holder = (ESignBorrowerViewHolder) v.getTag();
        }

        ESignBorrower eSignBorrower = eSignBorrowers.get(position);

        holder.tvBorrowerName.setText(eSignBorrower.PartyName);
        holder.tvBorrowerGuardian.setText(eSignBorrower.FatherName);
        holder.tvCreator.setText(eSignBorrower.Creator);
        holder.tvFiCode.setText(String.valueOf(eSignBorrower.FiCode));
        holder.tvMobile.setText(eSignBorrower.MobileNo);
        holder.tvAddress.setText(eSignBorrower.Address);
//        holder.tvAadhar.setText(eSignBorrower.AadharNo);

        if (eSignBorrower.ESignSucceed.equals("BLK")) {
            v.setBackgroundColor(Color.RED);
        }else {
            v.setBackgroundColor(Color.WHITE);
        }
        return v;
    }

}
