package com.stanfy.spoon.example.test;

import android.test.ActivityInstrumentationTestCase2;
import android.test.UiThreadTest;
import android.widget.TextView;

import com.squareup.spoon.Spoon;
import com.stanfy.spoon.example.MainActivity;

/**
 * Tests for MainActivity.
 */
public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {


  public MainActivityTest() {
    super(MainActivity.class);
  }

  @UiThreadTest
  public void testSetText() throws Throwable {
    final MainActivity act = getActivity();
    final TextView text = (TextView) act.findViewById(android.R.id.text1);
    assertNotNull(text);
    Spoon.screenshot(act, "startup");

    final int steps = 5;
    for (int i = 1; i <= steps; i++) {
      final String step = String.valueOf(i);
      act.setText(step);
      Spoon.screenshot(act, "step-" + i);
      assertEquals(text.getText().toString(), step);
    }

  }

}
