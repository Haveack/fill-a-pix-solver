package com.raid.fpsolver;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends Activity {
	
	int m = 0;
	MyView mv;
	View dialogView;
	String[] files;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mv = new MyView(this, "");
		setContentView(mv);
		files = getResources().getStringArray(R.array.files);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public boolean onOptionsItemSelected (MenuItem item) {
		
		switch(item.getItemId()) {
		case R.id.action_settings: {
			new AlertDialog.Builder(this)
							.setTitle("Select Puzzle")
							.setItems(files, onDialog)
							.setNegativeButton("Cancel", null)
							.show();
			break;
		}
		case R.id.action_about: {
			Toast.makeText(this, "raid.xu@gmail.com", Toast.LENGTH_SHORT).show();
		}
		}
		
		return super.onOptionsItemSelected(item);
	}
	
	DialogInterface.OnClickListener onDialog = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			mv = new MyView(getApplicationContext(), files[which]);
			MainActivity.this.setContentView(mv);
		}
	};

}
