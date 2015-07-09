package com.stanfy.spoon.example.test;

import android.os.Bundle;
import android.test.InstrumentationTestRunner;
import android.util.Log;

/** Custom runner implementation to check how arguments are passed.  */
public class CustomTestRunner extends InstrumentationTestRunner {

  public void onCreate(final Bundle arguments) {
    if (arguments == null) {
      throw new IllegalStateException("Arguments are supposed to be passed to instrumenation in this example");
    }
    if (!"bar".equals(arguments.getString("foo"))) {
      throw new IllegalArgumentException("foo=bar argument is supposed to be passed to instrumentaion in this example");
    }
    Log.i("CustomTestRunner", "arguments=" + arguments);
    super.onCreate(arguments);
  }

}
