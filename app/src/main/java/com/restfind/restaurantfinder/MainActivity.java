package com.restfind.restaurantfinder;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {

    AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set up UI-Elements
        Button searchByNameBtn = (Button) findViewById(R.id.searchByNameBtn);
        Button searchAllBtn = (Button) findViewById(R.id.searchAllBtn);

        createDialog();

        searchByNameBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //show Text Input Dialog
                builder.show();
            }
        });

        searchAllBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, SearchOptionsActivity.class));
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        createDialog();
    }

    //create Text Input Dialog
    private void createDialog(){
        builder = new AlertDialog.Builder(this);
        builder.setTitle(getResources().getString(R.string.search_by_name));

        // Set up the input
        final EditText etInput = new EditText(this);
        etInput.setInputType(InputType.TYPE_CLASS_TEXT);
        builder.setView(etInput);

        // Set up buttons
        builder.setPositiveButton("Search", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String searchText = etInput.getText().toString();

                Intent intent = new Intent(MainActivity.this, SearchResultsActivity.class);
                intent.putExtra(getResources().getString(R.string.search_options), new SearchOptions(searchText));
                startActivity(intent);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
    }

    @Override
    public void onBackPressed() {
        //Do nothing (don't go back to Login)
    }
}
