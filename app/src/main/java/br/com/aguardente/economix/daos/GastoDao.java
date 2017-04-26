package br.com.aguardente.economix.daos;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import br.com.aguardente.economix.models.Gasto;
import br.com.aguardente.economix.models.Usuario;

/**
 * Created by joao on 26/04/17.
 */

public class GastoDao {

    private static final String TAG = "GASTO_DAO";

    private FirebaseAuth mAuth;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    private boolean canDo = false;
    private Activity activity;
    private Toast toastOnStart;

    public GastoDao(Activity activity) {
        this.activity = activity;
        mAuth = FirebaseAuth.getInstance();
        if (mAuth != null) {
            database = FirebaseDatabase.getInstance();
        }
        if (database != null) {
            canDo = true;
        }
    }

    public void salvarGasto(Usuario usuario, Gasto gasto) {
        if (canDo) {
            onStart("Registrando gasto...");
            Log.d(TAG, "Salvando gasto...");
            myRef = database.getReference();
            String key = myRef.child("users").child(usuario.getUid()).push().getKey();
            myRef.child("users").child(usuario.getUid()).child("gastos").child(key)
                    .setValue(gasto).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if (task.isSuccessful()) {
                        onSuccess("Gasto registrado!");
                    } else {
                        onFail("Gasto não registrado!");
                    }
                }
            });
        }
    }

    public void onStart(String msg) {
        toastOnStart = Toast.makeText(activity, msg, Toast.LENGTH_SHORT);
        toastOnStart.show();
    }

    public void onSuccess(String msg) {
        toastOnStart.cancel();
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        activity.setResult(Activity.RESULT_OK);
        activity.finish();
    }

    public void onFail(String msg) {
        toastOnStart.cancel();
        Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show();
        activity.setResult(Activity.RESULT_CANCELED);
        activity.finish();
    }
}
