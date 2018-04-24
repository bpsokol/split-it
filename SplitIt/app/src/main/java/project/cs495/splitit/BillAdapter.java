package project.cs495.splitit;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class BillAdapter extends RecyclerView.Adapter {

    List list;
    Context context;

    public BillAdapter(List list, Context context){
        this.list = list;
        this.context = context;
    }

    //Ctrl + O


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.bill_list_item, parent, false);
        return new MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        Bill bill = (Bill) list.get(position);

        ((MyViewHolder) holder).name.setText(bill.getName());
        ((MyViewHolder) holder).pay.setText(bill.getAmount());
        ((MyViewHolder) holder).email.setText(bill.getEmail());

    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        //Ctrl + O
        TextView name;
        TextView email;
        Button pay;


        public MyViewHolder(View itemView) {
            super(itemView);
            name = (TextView) itemView.findViewById(R.id.manager_name);
            email = (TextView) itemView.findViewById(R.id.manager_email);
            pay = (Button) itemView.findViewById(R.id.pay_bill);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    public void insert(Bill bill) {
        list.add(bill);
    }

    public void remove(Bill bill) {
        int position = list.indexOf(bill);
        list.remove(position);
        notifyItemRemoved(position);
    }
}