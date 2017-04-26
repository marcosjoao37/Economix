package br.com.aguardente.economix;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.roomorama.caldroid.CaldroidFragment;

import java.util.Calendar;

import br.com.aguardente.economix.conf.Static;
import br.com.aguardente.economix.models.Usuario;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MAIN_ACTIVITY";
    Activity activity;

    public static final int CADASTRAR_GASTO = 1;
    public static final int LOGIN = 2;

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private ValueEventListener dataChanges;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        activity = this;
        carregarCalendario();

        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            database = FirebaseDatabase.getInstance();
        }
        if (database != null) {
            if (dataChanges == null) {
                dataChanges = new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        String value = dataSnapshot.getValue(String.class);
                        Log.d(TAG, "Value is: " + value);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.w(TAG, "Failed to read value.", databaseError.toException());
                    }
                };
            }
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
        CaldroidFragment caldroidFragment = new CaldroidFragment();
        Bundle args = new Bundle();
        Calendar cal = Calendar.getInstance();
        args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
        args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
        caldroidFragment.setArguments(args);

        android.support.v4.app.FragmentTransaction t = getSupportFragmentManager().beginTransaction();
        t.replace(R.id.calendario, caldroidFragment);
        t.commit();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivityForResult(new Intent(activity, Login.class), LOGIN);
        } else {
            if (Static.usuario == null) {
                Toast.makeText(activity, currentUser.getDisplayName()+"", Toast.LENGTH_SHORT).show();
                Static.usuario = new Usuario(
                        currentUser.getUid(),
                        currentUser.getEmail(),
                        currentUser.getEmail() // username
                );
            }
        }
    }
}
