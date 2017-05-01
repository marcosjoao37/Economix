package br.com.aguardente.economix.cards;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import br.com.aguardente.economix.FiltrarGastos;
import br.com.aguardente.economix.R;
import br.com.aguardente.economix.conf.Static;
import br.com.aguardente.economix.daos.GastoDao;
import br.com.aguardente.economix.models.Gasto;

/**
 * Created by joao on 30/04/17.
 */

public class MesesCard extends RecyclerView.Adapter<MesesCard.MyViewHolder> {

    private FiltrarGastos activity;
    private List<String> mesesList;
    private LayoutInflater inflater;
    private int count = 0;

    public MesesCard(Activity activity, List<String> mesesList) {
        this.activity = (FiltrarGastos) activity;
        this.mesesList = mesesList;
        this.inflater = LayoutInflater.from(activity);
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_filtrar_gastos_meses, parent, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        String mes = mesesList.get(position);

        holder.txtMes.setText(mes);
    }

    private int count() {
        return count++;
    }

    @Override
    public int getItemCount() {
        return mesesList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        MyViewHolder viewHolder;
        TextView txtMes;
        public String mesSelecionado;

        public MyViewHolder(View itemView) {
            super(itemView);

            mesSelecionado = mesesList.get(count());

            viewHolder = this;
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            txtMes = (TextView) itemView.findViewById(R.id.mes);

            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    activity.carregarGastos(mesSelecionado);
                }
            });
        }
    }
}
