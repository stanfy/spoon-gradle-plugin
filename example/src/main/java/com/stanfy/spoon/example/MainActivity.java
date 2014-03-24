package com.stanfy.spoon.example;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.TextView;

/**
 * Sample activity.
 */
public class MainActivity extends Activity {

  /** Text size. */
  private static final int TEXT_SIZE = 30;

  /** Text view. */
  private TextView textView;

  @Override
  protected void onCreate(final Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    textView = new TextView(this);
    textView.setGravity(Gravity.CENTER);
    textView.setTextSize(TEXT_SIZE);
    textView.setId(android.R.id.text1);
    setContentView(textView);
  }

  public void setText(final String textView) {
    this.textView.setText(textView);
  }

}
