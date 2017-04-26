package br.com.aguardente.economix;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import br.com.aguardente.economix.conf.Static;
import br.com.aguardente.economix.daos.GastoDao;
import br.com.aguardente.economix.models.Gasto;

public class CadastrarGasto extends AppCompatActivity {

    Activity activity;

    DateFormat dateFormat;

    EditText editTextPreco;
    EditText editTextSobre;
    EditText editTextQuando;

    List<EditText> editTextList;

    private DatePickerDialog.OnDateSetListener datePickerListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastrar_gasto);
        activity = this;

        dateFormat = new SimpleDateFormat("dd/MM/yyyy");

        editTextPreco = (EditText) findViewById(R.id.preco);
        editTextSobre = (EditText) findViewById(R.id.sobre);
        editTextQuando = (EditText) findViewById(R.id.quando);

        editTextList = new ArrayList<>();
        editTextList.add(editTextPreco);
        editTextList.add(editTextQuando);
        editTextList.add(editTextSobre);

        datePickerListener = new DatePickerDialog.OnDateSetListener() {

            // when dialog box is closed, below method will be called.
            public void onDateSet(DatePicker view, int selectedYear, int selectedMonth, int selectedDay) {
                try {
                    editTextQuando.setText(selectedDay + "/" + selectedMonth + "/" + selectedYear);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
    }

    public void escolherDiaHoje(View view) {
        Calendar calendar = Calendar.getInstance();
        String diaHoje = dateFormat.format(calendar.getTime());
        editTextQuando.setText(diaHoje);
    }

    public void escolherDia(View view) {
        Toast.makeText(activity, "Escolher dia no calendário...", Toast.LENGTH_SHORT).show();
        Calendar cal = Calendar.getInstance();

        DatePickerDialog datePickerDialog = new DatePickerDialog(activity,
                R.style.AppTheme,
                datePickerListener,
                cal.get(Calendar.YEAR),
                cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH));

        datePickerDialog.setCancelable(false);
        datePickerDialog.setTitle("Selecione uma data");
        datePickerDialog.show();
    }

    public void salvar(View view) {
        if (checkCanFinalize(editTextList)) {
            try {
                Gasto gasto = new Gasto(
                        Double.parseDouble(editTextPreco.getText().toString()),
                        editTextSobre.getText().toString(),
                        dateFormat.parse(editTextQuando.getText().toString())
                );
                GastoDao gastoDao = new GastoDao(activity);
                gastoDao.salvarGasto(Static.usuario, gasto);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public boolean checkCanFinalize(List<EditText> editTextList) {
        for (EditText ET : editTextList) {
            if (TextUtils.isEmpty(ET.getText().toString())) {
                ET.setError("Este campo não pode estar vazio!");
                return false;
            } else if (!TextUtils.isDigitsOnly(ET.getText().toString())
                    && ET.getInputType() == InputType.TYPE_CLASS_NUMBER) {
                ET.setError("Este campo só deve ter números!");
                return false;
            }
        }

        return true;
    }
}
