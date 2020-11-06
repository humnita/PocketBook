package com.example.pocketbook.activity;

import android.os.SystemClock;
import android.view.View;

import androidx.test.rule.ActivityTestRule;

import com.example.pocketbook.R;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

public class LogInActivityTest {

    @Rule
    public ActivityTestRule<LogInActivity> Activity = new ActivityTestRule<LogInActivity>(LogInActivity.class);

    private LogInActivity currentActivity = null;
    @Before
    public void setUp() throws Exception {
        currentActivity = Activity.getActivity();
    }


    @Test
    public void launchTest() {
        View emailField = currentActivity.findViewById(R.id.UserReg);
        assertNotNull(emailField);
        View passwordField = currentActivity.findViewById(R.id.PasswordReg);
        assertNotNull(passwordField);
    }

    @Test
    public void LogInTest(){
        SystemClock.sleep(800);
        assertNotNull(currentActivity.findViewById(R.id.LoginBtn));
        onView(withId(R.id.UserReg)).perform(typeText("jane@gmail.com")).perform(closeSoftKeyboard());
        onView(withId(R.id.PasswordReg)).perform(typeText("123456")).perform(closeSoftKeyboard());
    }

}


