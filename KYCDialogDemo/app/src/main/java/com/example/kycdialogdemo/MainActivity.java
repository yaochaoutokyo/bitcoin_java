package com.example.kycdialogdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

	private Button btnShowDialog;
	private MyDialog mMyDialog;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main_activity_layout);

		btnShowDialog = findViewById(R.id.showDialog);
		btnShowDialog.setOnClickListener((v) -> {
			View view = getLayoutInflater().inflate(R.layout.dialog_layout, null);
			mMyDialog = new MyDialog(this, 0, 0, view, R.style.DialogTheme);
			mMyDialog.setCancelable(true);
			mMyDialog.show();

			TextView cancel = view.findViewById(R.id.cancel);
			TextView confirm = view.findViewById(R.id.confirm);
			cancel.setOnClickListener((v2) -> {
				mMyDialog.dismiss();
			});
		});
	}
}
