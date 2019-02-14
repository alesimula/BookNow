package com.pdd.booknow;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;

import java.util.Calendar;

public class DatePickerFragment extends DialogFragment {
    private Calendar date;
    private DatePickerFragmentListener listener; //variabile listener

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        super.onCreateDialog(savedInstanceState); //uso il metodo di Dialog

        if(date==null){ //in questo caso creo dei valori di default
            date = Calendar.getInstance();
            date.set(Calendar.YEAR, 1997);
            date.set(Calendar.MONTH, Calendar.JANUARY);
            date.set(Calendar.DAY_OF_MONTH, 1);
        }
        final DatePicker datePicker = new DatePicker(getActivity()); //creo il datepicker, il costruttore prende il contesto
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity()); //creo il contenitore del datepicker

        builder.setView(datePicker); //inserisco all'interno del contenitore il datepicker

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) { //bottone OK, assegna a "date" mese, giorno, anno
                date.set(Calendar.YEAR, datePicker.getYear());
                date.set(Calendar.MONTH, datePicker.getMonth());
                date.set(Calendar.DAY_OF_MONTH, datePicker.getDayOfMonth());

                if(listener!=null){
                    listener.onDatePickerFragmentOkButton(DatePickerFragment.this, date);//genera l'evento che aggiorna i valori
                    //della data nella text view
                }
            }
        });

        builder.setNegativeButton("Annulla", new DialogInterface.OnClickListener() { //bottone ANNULLA
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(listener!=null){
                    listener.onDatePickerFragmentCancelButton(DatePickerFragment.this);//genera l'evento che aggiorna i valori
                    //della data nella text view
                }
            }
        });
        return builder.create(); //crea il builder
    }

    //interazione con il picker
    public Calendar getData(){
        return date;
    }
    //metodo di setter per il listener
    public void setOnDatePickerFragmentChanged (DatePickerFragmentListener l){
        this.listener =l;
    }
    //interfaccia con i metodi che gestiranno gli eventi
    public interface DatePickerFragmentListener{
        public void onDatePickerFragmentOkButton(DialogFragment dialog, Calendar date);
        public void onDatePickerFragmentCancelButton(DialogFragment dialog);
    }
}
