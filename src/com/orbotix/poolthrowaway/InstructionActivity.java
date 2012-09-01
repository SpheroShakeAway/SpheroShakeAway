package com.orbotix.poolthrowaway;

import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
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
    	  Drawable d = getResources().getDrawable(R.drawable.inst2);
    	  this.findViewById(R.id.instructionsScreen).setBackgroundDrawable(d);
    	  this.findViewById(R.id.instructionsScreen).invalidate();
//    	View previousInstruction = findViewById(R.id.textView1);
//    	previousInstruction.setVisibility(-1);
//    	
//    	View nextInstruction = findViewById(R.id.textView2);
//    	nextInstruction.setVisibility(1);
      }
      else if (currentInstruction == 1)
      {
    	  Drawable d = getResources().getDrawable(R.drawable.inst3);
    	  this.findViewById(R.id.instructionsScreen).setBackgroundDrawable(d);
    	  this.findViewById(R.id.instructionsScreen).invalidate();
//    	View previousInstruction = findViewById(R.id.textView2);
//    	previousInstruction.setVisibility(-1);
    	
    	//View nextInstruction = findViewById(R.id.textView3);
    	//nextInstruction.setVisibility(1);
      }
      else if (currentInstruction == 2)
      {
    	  Drawable d = getResources().getDrawable(R.drawable.inst4);
    	  this.findViewById(R.id.instructionsScreen).setBackgroundDrawable(d);
    	  this.findViewById(R.id.instructionsScreen).invalidate();
//    	View previousInstruction = findViewById(R.id.textView3);
//    	previousInstruction.setVisibility(-1);
//    	
//    	View nextInstruction = findViewById(R.id.textView4);
//    	nextInstruction.setVisibility(1);
      }
      else if (currentInstruction == 3)
      {
    	  Drawable d = getResources().getDrawable(R.drawable.inst5);
    	  this.findViewById(R.id.instructionsScreen).setBackgroundDrawable(d);
    	  this.findViewById(R.id.instructionsScreen).invalidate();
//    	View previousInstruction = findViewById(R.id.textView4);
//    	previousInstruction.setVisibility(-1);
//    	
//    	View nextInstruction = findViewById(R.id.textView5);
//    	nextInstruction.setVisibility(1);
      }
      if (currentInstruction == 4)
      {
    	finish();  
      }
    	currentInstruction++;
    }
}
