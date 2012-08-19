package com.orbotix.streamingexample;

import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.widget.Button;

public class InstructionActivity extends Activity {

	private int currentInstruction = 0;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instruction);
        
        Button next = (Button) findViewById(R.id.button2);
        next.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                nextInstruction();
            }

        });
        
    }
    
    private void nextInstruction() {
    	
      if (currentInstruction == 0)
      {
    	View previousInstruction = findViewById(R.id.textView1);
    	previousInstruction.setVisibility(-1);
    	
    	View nextInstruction = findViewById(R.id.textView2);
    	nextInstruction.setVisibility(1);
      }
      if (currentInstruction == 1)
      {
    	finish();  
      }
    	currentInstruction++;
    }
}
