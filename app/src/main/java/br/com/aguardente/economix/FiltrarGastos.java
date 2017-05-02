package br.com.aguardente.economix;

import android.app.Activity;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.aguardente.economix.cards.GastoDialogCard;
import br.com.aguardente.economix.cards.GastoMesCard;
import br.com.aguardente.economix.cards.MesesCard;
import br.com.aguardente.economix.conf.Static;
import br.com.aguardente.economix.models.Gasto;

public class FiltrarGastos extends AppCompatActivity {
    Activity activity;
    private static final String TAG = "FILTRAR_GASTOS";

    List<String> meses = new ArrayList<>();
    List<Gasto> gastos = new ArrayList<>();

    private FirebaseDatabase database;
    private ChildEventListener childEventListener;

    private Date dateGastosInicio;
    private Date dateGastosFim;

    private RecyclerView recyclerViewGastoMes;
    private GastoMesCard gastoMesesCard;

    public TextView txtMes;
    private LinearLayout carregando;
    private LinearLayout nenhumDado;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filtrar_gastos);
        activity = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Economix");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        txtMes = (TextView) findViewById(R.id.mesSelecionado);
        carregando = (LinearLayout) findViewById(R.id.carregando);
        nenhumDado = (LinearLayout) findViewById(R.id.nenhumDado);

        meses.add("Janeiro");
        meses.add("Fevereiro");
        meses.add("Março");
        meses.add("Abril");
        meses.add("Maio");
        meses.add("Junho");
        meses.add("Julho");
        meses.add("Agosto");
        meses.add("Setembro");
        meses.add("Outubro");
        meses.add("Novembro");
        meses.add("Dezembro");

        System.out.println(meses.size());

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycleViewMeses);
        MesesCard mesesCard = new MesesCard(activity, meses);
        recyclerView.setAdapter(mesesCard);
        recyclerView.setLayoutManager(new LinearLayoutManager(activity,
                LinearLayoutManager.HORIZONTAL, false));

        recyclerViewGastoMes = (RecyclerView) findViewById(R.id.recycleViewGastosPorMes);
        gastoMesesCard = new GastoMesCard(activity, gastos);
        recyclerViewGastoMes.setAdapter(gastoMesesCard);
        recyclerViewGastoMes.setLayoutManager(new LinearLayoutManager(activity,
                LinearLayoutManager.VERTICAL, false));

        database = FirebaseDatabase.getInstance();

    }

    public void carregarGastos(String mes) {
        Log.d(TAG, mes);
        gastos.clear();
        carregando.setVisibility(View.VISIBLE);
        txtMes.setText(mes);
        if (mes.equalsIgnoreCase("Janeiro")) {
            carregarDados(1);
        } else if (mes.equalsIgnoreCase("Fevereiro")) {
            carregarDados(2);
        } else if (mes.equalsIgnoreCase("Março")) {
            carregarDados(3);
        } else if (mes.equalsIgnoreCase("Abril")) {
            carregarDados(4);
        } else if (mes.equalsIgnoreCase("Maio")) {
            carregarDados(5);
        } else if (mes.equalsIgnoreCase("Junho")) {
            carregarDados(6);
        } else if (mes.equalsIgnoreCase("Julho")) {
            carregarDados(7);
        } else if (mes.equalsIgnoreCase("Agosto")) {
            carregarDados(8);
        } else if (mes.equalsIgnoreCase("Setembro")) {
            carregarDados(9);
        } else if (mes.equalsIgnoreCase("Outubro")) {
            carregarDados(10);
        } else if (mes.equalsIgnoreCase("Novembro")) {
            carregarDados(11);
        } else if (mes.equalsIgnoreCase("Dezembro")) {
            carregarDados(12);
        }
    }

    private void carregarDados(int mes_num) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
        cal.clear(Calendar.MINUTE);
        cal.clear(Calendar.SECOND);
        cal.clear(Calendar.MILLISECOND);
        cal.set(cal.get(Calendar.YEAR), mes_num - 1, 1);

        // get start of the month
        cal.set(Calendar.DAY_OF_MONTH, 1);
        dateGastosInicio = cal.getTime();

        // get start of the next month
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_YEAR, -1);
        dateGastosFim = cal.getTime();

        Query query = database.getReference("users/" + Static.usuario.getUid() + "/gastos")
                .orderByChild("data").startAt(dateGastosInicio.getTime()).endAt(dateGastosFim.getTime());
        childEventListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Gasto gasto = dataSnapshot.getValue(Gasto.class);
                gastos.add(gasto);
                gastoMesesCard.notifyDataSetChanged();

                nenhumDado.setVisibility(View.GONE);
                carregando.setVisibility(View.GONE);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        query.addChildEventListener(childEventListener);

        gastoMesesCard = new GastoMesCard(activity, gastos);
        recyclerViewGastoMes.setAdapter(gastoMesesCard);

        gastoMesesCard.notifyDataSetChanged();

        new CountDownTimer(2000, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (gastos.size() > 0) {
                    onFinish();
                    cancel();
                }
            }

            @Override
            public void onFinish() {
                carregando.setVisibility(View.GONE);
                if (gastos.size() == 0) {
                    nenhumDado.setVisibility(View.VISIBLE);
                } else {
                    nenhumDado.setVisibility(View.GONE);
                }
            }
        }.start();
    }

    @Override
    public boolean onSupportNavigateUp() {
//        onBackPressed();
        finish();
        return true;
    }
}
