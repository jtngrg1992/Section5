package com.example.jatingarg.section5;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Created by jatingarg on 26/04/17.
 */


import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static junit.framework.Assert.assertTrue;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.ViewMatchers.isRoot;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.any;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;



@RunWith(AndroidJUnit4.class)
public class AppTests {

    @Rule
    public ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<MainActivity>(MainActivity.class);


    @Test
    public void testApp(){
        onView(withId(R.id.playBtn)).check(matches(isDisplayed()));
        onView(withId(R.id.cameraBtn)).check(matches(isDisplayed()));

        //testing media playback
        onView(withId(R.id.playBtn)).perform(click());
        assertNotNull(mActivityTestRule.getActivity().getPlayerInstance());
        assertTrue(mActivityTestRule.getActivity().getPlayerInstance().isPlaying());

        //testing camera display

        onView(withId(R.id.cameraBtn)).perform(click());
        onView(withId(R.id.texttureView)).check(matches(isDisplayed()));

        //NOTE: espresso doesn't support Camera2 API testing.
        //The tester will need to make sure that the camera feed is showing after this button click
    }

}
