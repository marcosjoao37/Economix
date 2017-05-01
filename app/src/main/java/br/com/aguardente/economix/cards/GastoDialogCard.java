package br.com.aguardente.economix.cards;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.List;

import br.com.aguardente.economix.R;
import br.com.aguardente.economix.conf.Static;
import br.com.aguardente.economix.daos.GastoDao;
import br.com.aguardente.economix.models.Gasto;

/**
 * Created by joao on 30/04/17.
 */

public class GastoDialogCard extends RecyclerView.Adapter<GastoDialogCard.MyViewHolder> {

    private View view;
    private List<Gasto> gastoList;
    private LayoutInflater inflater;
    private int count = 0;

    public GastoDialogCard(View view, List<Gasto> gastoList) {
        this.view = view;
        this.gastoList = gastoList;
        this.inflater = LayoutInflater.from(view.getContext());
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.list_gastos_diarios_dialog, parent, false);
        MyViewHolder holder = new MyViewHolder(view);

        return holder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Gasto gasto = gastoList.get(position);

        holder.sobre.setText(gasto.getSobre());
        holder.valor.setText("R$" + gasto.getPreco());
    }

    private int count() {
        return count++;
    }

    @Override
    public int getItemCount() {
        return gastoList.size();
    }

    class MyViewHolder extends RecyclerView.ViewHolder {

        CardView cardView;
        MyViewHolder viewHolder;
        TextView sobre;
        TextView valor;
        ImageButton btnDeletar;
        public Gasto gasto;

        public MyViewHolder(View itemView) {
            super(itemView);

            viewHolder = this;
            cardView = (CardView) itemView.findViewById(R.id.cardView);
            sobre = (TextView) itemView.findViewById(R.id.sobre);
            valor = (TextView) itemView.findViewById(R.id.valor);
            btnDeletar = (ImageButton) itemView.findViewById(R.id.btnDeletarGasto);

            gasto = gastoList.get(count());

            btnDeletar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(view.getContext());
                    alertBuilder.setTitle("Confirme");
                    alertBuilder.setMessage("Você tem certeza que deseja deletar este gasto?");
                    alertBuilder.setCancelable(false);
                    alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            new GastoDao(view.getContext()).deletarGasto(Static.usuario, gasto, cardView);
                        }
                    });
                    alertBuilder.setNegativeButton("Cancelar", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // Não deletar
                        }
                    });
                    alertBuilder.show();
                }
            });
        }
    }
}
