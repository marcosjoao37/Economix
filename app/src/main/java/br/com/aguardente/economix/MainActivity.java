package br.com.aguardente.economix;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
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
import com.google.firebase.database.ValueEventListener;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.com.aguardente.economix.conf.Static;
import br.com.aguardente.economix.models.Gasto;
import br.com.aguardente.economix.models.Usuario;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN_ACTIVITY";
    Activity activity;

    public static final int CADASTRAR_GASTO = 1;
    public static final int LOGIN = 2;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private FirebaseDatabase database;
    private Query databaseReference;
    private ChildEventListener gastosChangedListener;

    public static CaldroidFragment caldroidFragment;
    private CaldroidListener caldroidListener;

    private Map<Date, Drawable> backgroundForDateMap;
    private Map<Date, Integer> textColorForDateMap;
    GradientDrawable drawable;

    DateFormat dateFormat;

    private double totalGasto;
    private TextView txtTotalGasto;

    private boolean started;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;

        started = false;

        backgroundForDateMap = new HashMap<>();
        textColorForDateMap = new HashMap<>();

        dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        totalGasto = 0.0;
        txtTotalGasto = (TextView) findViewById(R.id.totalGasto);

        drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadii(new float[]{8, 8, 8, 8, 8, 8, 8, 8});
        drawable.setColor(Color.GREEN);

        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
        if (mAuth != null && currentUser != null) {
            if (Static.usuario == null) {
                Toast.makeText(activity, currentUser.getDisplayName() + "", Toast.LENGTH_SHORT).show();
                Static.usuario = new Usuario(
                        currentUser.getUid(),
                        currentUser.getEmail(),
                        currentUser.getEmail() // username
                );
            }

            database = FirebaseDatabase.getInstance();
            dataChanges();
            carregarCalendario();
            started = true;
        }
    }

    public void dataChanges() {
        if (database != null) {
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
        }
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
                        totalGasto = 0.0;
                        txtTotalGasto.setText(totalGasto + "");
                        List<Date> datesForBackground = new ArrayList<>(backgroundForDateMap.keySet());
                        List<Date> datesForFonts = new ArrayList<>(textColorForDateMap.keySet());

                        caldroidFragment.clearBackgroundDrawableForDates(datesForBackground);
                        caldroidFragment.clearTextColorForDates(datesForFonts);

                        // get start of the month
                        cal.set(Calendar.DAY_OF_MONTH, 1);
                        Date inicio = cal.getTime();

                        // get start of the next month
                        cal.add(Calendar.MONTH, 1);
                        cal.add(Calendar.DAY_OF_YEAR, -1);
                        Date fim = cal.getTime();

                        databaseReference = database.getReference("users/" + Static.usuario.getUid() + "/gastos")
                                .orderByChild("data").startAt(inicio.getTime()).endAt(fim.getTime());
                        dataChanges();
                        databaseReference.addChildEventListener(gastosChangedListener);
                    }
                };
            }

            caldroidFragment.setCaldroidListener(caldroidListener);
            android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            t.replace(R.id.calendario, caldroidFragment);
            t.commit();
        }
    }

    private void criarDialogoGastosDoDia(final Date date) {
        final List<String> gastoList = new ArrayList<>();

        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // none
            }
        });

        DatabaseReference ref = database.getReference("/users/" + Static.usuario.getUid() +
                "/dias/" + date.getTime());
        ref.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                Gasto gasto = dataSnapshot.getValue(Gasto.class);
                System.out.println("GASTO: " + gasto.getUid());
                gastoList.add(gasto.getSobre() + ": R$" + gasto.getPreco());
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

        builder.setTitle("Gastos do dia");

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
                    builder.setMessage("Nenhum gasto neste dia!");
                } else {
                    ArrayAdapter<String> adapter = new ArrayAdapter<>(activity,
                            android.R.layout.select_dialog_item, gastoList);
                    builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Toast.makeText(activity, "CLICOU: " + which, Toast.LENGTH_SHORT).show();
                        }
                    });
                }

                builder.show();
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
            if (Static.usuario == null) {
                Toast.makeText(activity, currentUser.getDisplayName() + "", Toast.LENGTH_SHORT).show();
                Static.usuario = new Usuario(
                        currentUser.getUid(),
                        currentUser.getEmail(),
                        currentUser.getEmail() // username
                );
            }
            if (!started) {
                database = FirebaseDatabase.getInstance();
                dataChanges();
                carregarCalendario();
                started = true;
            }
        }
    }
}
