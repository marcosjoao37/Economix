package br.com.aguardente.economix;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.aguardente.economix.cards.GastoCard;
import br.com.aguardente.economix.conf.Static;
import br.com.aguardente.economix.models.Gasto;
import br.com.aguardente.economix.models.Usuario;

public class NavigationActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = "MAIN_ACTIVITY";
    Activity activity;

    public static final int CADASTRAR_GASTO = 1;
    public static final int LOGIN = 2;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private FirebaseDatabase database;
    private ChildEventListener gastosChangedListener;

    public static CaldroidFragment caldroidFragment;
    private CaldroidListener caldroidListener;

    private Map<Date, Drawable> backgroundForDateMap;
    private Map<Date, Integer> textColorForDateMap;
    private List<Gasto> gastoList;
    GradientDrawable drawable;

    DateFormat dateFormat;

    private double totalGasto;
    private TextView txtTotalGasto;

    private TextView nomeUsuario;
    private TextView emailUsuario;

    private boolean started;

    private Date dateGastosInicio;
    private Date dateGastosFim;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_navigation);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("Economix");

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header = navigationView.getHeaderView(0);

        started = false;
        backgroundForDateMap = new HashMap<>();
        textColorForDateMap = new HashMap<>();
        gastoList = new ArrayList<>();
        dateFormat = new SimpleDateFormat("dd/MM/yyyy");
        totalGasto = 0.0;
        txtTotalGasto = (TextView) findViewById(R.id.totalGasto);
        nomeUsuario = (TextView) header.findViewById(R.id.nome);
        emailUsuario = (TextView) header.findViewById(R.id.email);

        drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadii(new float[]{8, 8, 8, 8, 8, 8, 8, 8});
        drawable.setColor(Color.GREEN);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (mAuth != null && currentUser != null) {
            nomeUsuario.setText(currentUser.getDisplayName());
            emailUsuario.setText(currentUser.getEmail());
            if (Static.usuario == null) {
                Toast.makeText(activity, currentUser.getDisplayName() + "", Toast.LENGTH_SHORT).show();
                Static.usuario = new Usuario(
                        currentUser.getUid(),
                        currentUser.getEmail(),
                        currentUser.getDisplayName() // username
                );
            }

            database = FirebaseDatabase.getInstance();
            carregarCalendario();
            started = true;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.navigation, menu);
//        return true;
//    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_listar_todos_gastos) {
            Toast.makeText(activity, "Em breve...", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_compartilhar) {
            Toast.makeText(activity, "Em breve...", Toast.LENGTH_SHORT).show();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void carregarCalendario() {
        if (caldroidFragment == null) {
            caldroidFragment = new CaldroidFragment();
            Bundle args = new Bundle();
            Calendar cal = Calendar.getInstance();
            args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
            args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
            caldroidFragment.setArguments(args);

            if (caldroidListener == null) {
                caldroidListener = new CaldroidListener() {
                    @Override
                    public void onSelectDate(Date date, View view) {
                        criarDialogoGastosDoDia(date);
                    }

                    @Override
                    public void onChangeMonth(int month, int year) {
                        super.onChangeMonth(month, year);
                        // get today and clear time of day
                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
                        cal.clear(Calendar.MINUTE);
                        cal.clear(Calendar.SECOND);
                        cal.clear(Calendar.MILLISECOND);
                        cal.set(year, month - 1, 1);

                        // get start of the month
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        dateGastosInicio = cal.getTime();

                        // get start of the next month
                        cal.add(Calendar.MONTH, 1);
                        cal.add(Calendar.DAY_OF_YEAR, -1);
                        dateGastosFim = cal.getTime();

                        totalGasto = 0.0;
                        txtTotalGasto.setText(totalGasto + "");

                        dataChanges();
                    }
                };
            }

            caldroidFragment.setCaldroidListener(caldroidListener);
            android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            t.replace(R.id.calendario, caldroidFragment);
            t.commit();
        }
    }

    public void dataChanges() {
        if (database != null) {
            Query query = database.getReference("users/" + Static.usuario.getUid() + "/gastos")
                    .orderByChild("data").startAt(dateGastosInicio.getTime()).endAt(dateGastosFim.getTime());

            List<Date> clearBackground = new ArrayList<>(backgroundForDateMap.keySet());
            List<Date> clearFonts = new ArrayList<>(textColorForDateMap.keySet());

            caldroidFragment.clearBackgroundDrawableForDates(clearBackground);
            caldroidFragment.clearTextColorForDates(clearFonts);

            backgroundForDateMap = new HashMap<>();
            textColorForDateMap = new HashMap<>();

            totalGasto = 0.0;

            gastosChangedListener = new ChildEventListener() {
                @Override
                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                    Gasto gasto = dataSnapshot.getValue(Gasto.class);
                    Date data = new Date(gasto.getData());
                    Log.d(TAG, "ADICIONADO: " + gasto.getSobre() + " " + gasto.getPreco()
                            + " " + gasto.getUid() + " " + dateFormat.format(data));

                    backgroundForDateMap.put(data, drawable);
                    textColorForDateMap.put(data, android.R.color.white);
                    caldroidFragment.setBackgroundDrawableForDates(backgroundForDateMap);
                    caldroidFragment.setTextColorForDates(textColorForDateMap);
                    caldroidFragment.refreshView();

                    totalGasto = totalGasto + gasto.getPreco();
                    txtTotalGasto.setText("R$ " + totalGasto);
                }

                @Override
                public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                    Gasto gasto = dataSnapshot.getValue(Gasto.class);
                    Log.d(TAG, "MODIFICADO: " + gasto.getSobre() + " " + gasto.getPreco()
                            + " " + dataSnapshot.getKey());
//                        List<Date> dates = new ArrayList<>();
//                        dates.add(new Date(gasto.getData()));
//                        caldroidFragment.clearBackgroundDrawableForDates(dates);
//                        caldroidFragment.clearTextColorForDates(dates);
//                        caldroidFragment.refreshView();
                }

                @Override
                public void onChildRemoved(DataSnapshot dataSnapshot) {
                    Gasto gasto = dataSnapshot.getValue(Gasto.class);
                    Log.d(TAG, "DELETADO: " + gasto.getSobre() + " " + gasto.getPreco());
                }

                @Override
                public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                    Gasto gasto = dataSnapshot.getValue(Gasto.class);
                    Log.d(TAG, "MOVIDO: " + gasto.getSobre() + " " + gasto.getPreco());

                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            };

            query.addChildEventListener(gastosChangedListener);
        }
    }

    private void atualizarValorGasto() {

    }

    public void gastei(View view) {
        startActivityForResult(new Intent(activity, CadastrarGasto.class), CADASTRAR_GASTO);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CADASTRAR_GASTO) {
            if (resultCode == RESULT_OK) {
                Log.d(TAG, "Gasto registrado");
//                Toast.makeText(activity, "Gasto registrado!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d(TAG, "Gasto não registrado");
//                Toast.makeText(activity, "Gasto não registrado!", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == LOGIN) {
            if (resultCode == RESULT_CANCELED) {
                Toast.makeText(activity, "Não foi possível logar!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void criarDialogoGastosDoDia(final Date date) {
        final List<Gasto> gastoList = new ArrayList<>();

        final AlertDialog.Builder alertBuilder = new AlertDialog.Builder(activity);
        alertBuilder.setTitle("Gastos do dia");
        alertBuilder.setCancelable(false);
        alertBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                dataChanges();
            }
        });

        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.alert_dialog_gastos_diarios, null);
        alertBuilder.setView(dialogView);

        DatabaseReference ref = database.getReference("/users/" + Static.usuario.getUid() +
                "/dias/" + date.getTime());
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Gasto gasto = dataSnapshot.getValue(Gasto.class);
                System.out.println("GASTO: " + gasto.getUid());
                gastoList.add(gasto);
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {
                System.out.println("changed");

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {
                System.out.println("removed");

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {
                System.out.println("moved");

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("error");
            }
        });

        new CountDownTimer(500, 100) {
            @Override
            public void onTick(long millisUntilFinished) {
                if (gastoList.size() > 0) {
                    onFinish();
                    cancel();
                }
            }

            @Override
            public void onFinish() {
                if (gastoList.size() == 0) {
                    alertBuilder.setMessage("Nenhum gasto neste dia!");
                } else {
                    RecyclerView recyclerView = (RecyclerView) dialogView.findViewById(R.id.recycleViewGastosDiarios);
                    GastoCard gastoCard = new GastoCard(dialogView, gastoList);
                    recyclerView.setAdapter(gastoCard);
                    recyclerView.setLayoutManager(new LinearLayoutManager(dialogView.getContext(),
                            LinearLayoutManager.VERTICAL, false));
//                    ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
//                            android.R.layout.select_dialog_item, gastoList);
//                    alertBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
//                        @Override
//                        public void onClick(DialogInterface dialog, int which) {
//                            Toast.makeText(activity, "CLICOU: " + which, Toast.LENGTH_SHORT).show();
//                        }
//                    });
                }

                alertBuilder.show();
            }
        }.start();
    }

    @Override
    protected void onStart() {
        super.onStart();

        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivityForResult(new Intent(activity, Login.class), LOGIN);
        } else {
            nomeUsuario.setText(currentUser.getDisplayName());
            emailUsuario.setText(currentUser.getEmail());
            if (Static.usuario == null) {
                Toast.makeText(activity, currentUser.getDisplayName() + "", Toast.LENGTH_SHORT).show();
                Static.usuario = new Usuario(
                        currentUser.getUid(),
                        currentUser.getEmail(),
                        currentUser.getDisplayName() // username
                );
            }
            if (!started) {
                database = FirebaseDatabase.getInstance();
                carregarCalendario();
                started = true;
            }
        }
    }
}
