package com.stanfy.spoon.example.test;

import android.support.test.annotation.UiThreadTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.TextView;

import com.squareup.spoon.SpoonRule;
import com.stanfy.spoon.example.MainActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;

/**
 * Tests for MainActivity.
 */
@RunWith(AndroidJUnit4.class)
public class AnotherMainActivityTest {

  @Rule
  public final SpoonRule spoon = new SpoonRule();
  @Rule
  public final ActivityTestRule<MainActivity> activityRule =
      new ActivityTestRule<>(MainActivity.class);

  @Test
  @UiThreadTest
  public void testSetText() throws Throwable {
    MainActivity act = activityRule.getActivity();
    final TextView text = (TextView) act.findViewById(android.R.id.text1);
    assertNotNull(text);
    spoon.screenshot(act, "startup");

    final int steps = 5;
    for (int i = 1; i <= steps; i++) {
      final String step = String.valueOf(i);
      act.setText(step);
      spoon.screenshot(act, "step-" + i);
      assertEquals(text.getText().toString(), step);
    }
  }
}
