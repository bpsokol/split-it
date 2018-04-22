package project.cs495.splitit;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;

public class BillAdapter extends ArrayAdapter<Bill> {

    public BillAdapter(Context context, ArrayList<Bill> list) {
        super(context, 0, list);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Bill bill = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view_items, parent, false);
        }
        TextView name = (TextView) convertView.findViewById(R.id.manager_name);
        TextView email = (TextView) convertView.findViewById(R.id.manager_email);
        Button pay = (Button) convertView.findViewById(R.id.pay_bill);

        name.setText(bill.getName());
        pay.setText(bill.getAmount());
        email.setText(bill.getEmail());

        return convertView;
    }
}