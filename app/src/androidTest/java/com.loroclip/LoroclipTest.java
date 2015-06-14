package com.loroclip;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.core.deps.guava.collect.Iterables;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.support.v7.widget.RecyclerView;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.longClick;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.RootMatchers.isDialog;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.Matchers.is;


/**
 * Created by stompesi on 15. 6. 7..
 */
public class LoroclipTest extends ActivityInstrumentationTestCase2<LoginActivity> {
  private final String TAG = "LoroclipTest";

  public LoroclipTest(){
    super(LoginActivity.class);
  }

  @Before
  public void setUp() throws Exception {
    super.setUp();
    injectInstrumentation(InstrumentationRegistry.getInstrumentation());
    getActivity();
  }

  @Test
  public void test1recordActivityOpenTest() {
    Log.d(TAG, "RecordActivity open test start.");
    onView(withId(R.id.fab)).perform(click());
    onView(withId(R.id.record_activity)).check(matches(isDisplayed()));

    onView(withId(R.id.record_trash_img)).check(matches(isEnabled()));
    onView(withId(R.id.record_trash_img)).check(matches(withImageDrawable(R.drawable.trash)));
    onView(withId(R.id.record_action_img)).check(matches(isEnabled()));
    onView(withId(R.id.record_action_img)).check(matches(withImageDrawable(R.drawable.record)));
    onView(withId(R.id.record_done_img)).check(matches(not(isEnabled())));
    onView(withId(R.id.record_done_img)).check(matches(withImageDrawable(R.drawable.done)));
    Log.d(TAG, "RecordActivity open test end.\n");
  }


  @Test
  public void test2recordActivityCancelNegativeTest() {
    Log.d(TAG, "Record Cancel-Negative test start.");
    onView(withId(R.id.fab)).perform(click());
    onView(withId(R.id.record_trash_img)).perform(click());
    onView(withText(R.string.delete_record)).inRoot(isDialog()).check(matches(isDisplayed()));
    onView(withText(R.string.cancel)).perform(click());
    onView(withId(R.id.record_activity)).check(matches(isDisplayed()));
    Log.d(TAG, "Record Cancel-Negative test end.\n");
  }

  @Test
  public void test3recordActivityCancelPositiveTest() {
    Log.d(TAG, "Record Cancel-Positive test start.");
    onView(withId(R.id.fab)).perform(click());
    onView(withId(R.id.record_trash_img)).perform(click());
    onView(withText(R.string.delete_record)).inRoot(isDialog()).check(matches(isDisplayed()));
    onView(withText(R.string.delete)).perform(click());
    onView(withId(R.id.main_activity)).check(matches(isDisplayed()));
    Log.d(TAG, "Record Cancel-Positive test end.\n");
  }

  @Test
  public void test4recordActivitySaveNegativeTest() {
    Log.d(TAG, "Record Save-Negative test start.");
    onView(withId(R.id.fab)).perform(click());
    onView(withId(R.id.record_done_img)).check(matches(not(isEnabled())));
    onView(withId(R.id.record_action_img)).perform(click());
    onView(withId(R.id.record_done_img)).check(matches((isEnabled())));
    onView(withId(R.id.record_done_img)).perform(click());
    onView(withText(R.string.edit_name)).inRoot(isDialog()).check(matches(isDisplayed()));
    onView(withText(R.string.cancel)).perform(click());
    onView(withId(R.id.record_activity)).check(matches(isDisplayed()));
    onView(withId(R.id.record_done_img)).check(matches((isEnabled())));
    onView(withId(R.id.record_action_img)).check(matches(withImageDrawable(R.drawable.record)));
    Log.d(TAG, "Record Save-Negative test end.\n");
  }

  @Test
  public void test6recordActivitySavePositiveTest() {
    Log.d(TAG, "Record Save-Positive test start.");
    onView(withId(R.id.fab)).perform(click());
    onView(withId(R.id.record_done_img)).check(matches(not(isEnabled())));
    onView(withId(R.id.record_action_img)).perform(click());
    onView(withId(R.id.bookmark_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    onView(withId(R.id.bookmark_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
    onView(withId(R.id.bookmark_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
    onView(withId(R.id.record_done_img)).check(matches((isEnabled())));
    onView(withId(R.id.record_done_img)).perform(click());
    onView(withText(R.string.edit_name)).inRoot(isDialog()).check(matches(isDisplayed()));
    onView(withText(R.string.save_button)).perform(click());
    onView(withId(R.id.main_activity)).check(matches(isDisplayed()));
    onView(withText(R.string.saved)).inRoot(withDecorView(not(is(getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    Log.d(TAG, "Record Save-Positive test end.\n");
  }

  @Test
  public void test5recordActivityRecordStartPauseTest() {
    Log.d(TAG, "Record Start-Pause test start.");
    onView(withId(R.id.fab)).perform(click());
    onView(withId(R.id.record_action_img)).check(matches(withImageDrawable(R.drawable.record)));
    onView(withId(R.id.record_action_img)).perform(click());
    onView(withId(R.id.record_action_img)).check(matches(withImageDrawable(R.drawable.pause)));
    onView(withId(R.id.record_action_img)).perform(click());
    onView(withId(R.id.record_action_img)).check(matches(withImageDrawable(R.drawable.record)));
    Log.d(TAG, "Record Start-Pause test end.\n");
  }
  @Test
  public void testBmainActivityDeleteFromDeviceNegativeTest() {
    Log.d(TAG, "DeleteFromDevice-Negative test start.");
    MainActivity mainActivity = null;
    try {
      mainActivity = (MainActivity) getCurrentActivity();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }

    onView(withId(R.id.record_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, longClick()));
    onView(withText(mainActivity.getResources().getStringArray(R.array.record_options)[2])).perform(click());
    onView(withText(R.string.cancel)).perform(click());
    onView(withId(R.id.main_activity)).check(matches(isDisplayed()));
    Log.d(TAG, "DeleteFromDevice-Negative test end.\n");
  }
  @Test
  public void testCmainActivityDeleteFromDevicePositiveTest() {
    Log.d(TAG, "DeleteFromDevice-Positive test start.");
    MainActivity mainActivity = null;
    try {
      mainActivity = (MainActivity) getCurrentActivity();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }

    onView(withId(R.id.record_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, longClick()));
    onView(withText(mainActivity.getResources().getStringArray(R.array.record_options)[2])).perform(click());
    onView(withText(R.string.delete)).perform(click());
    onView(withId(R.id.main_activity)).check(matches(isDisplayed()));
    onView(withText(R.string.deleted)).inRoot(withDecorView(not(is(getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));

    onView(withId(R.id.record_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, longClick()));
    onView(withText(mainActivity.getResources().getStringArray(R.array.record_options)[2])).perform(click());
    onView(withText(R.string.delete)).perform(click());
    onView(withId(R.id.main_activity)).check(matches(isDisplayed()));
    onView(withText(R.string.file_not_exist)).inRoot(withDecorView(not(is(getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    Log.d(TAG, "DeleteFromDevice-Positive test end.\n");
  }


  @Test
  public void testGmainActivityDeletePermanentlyPositiveTest() {
    Log.d(TAG, "DeletePermanently-Positive test start.");
    MainActivity mainActivity = null;
    try {
      mainActivity = (MainActivity) getCurrentActivity();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }

    onView(withId(R.id.record_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, longClick()));
    onView(withText(mainActivity.getResources().getStringArray(R.array.record_options)[1])).perform(click());
    onView(withText(R.string.delete)).perform(click());
    onView(withId(R.id.main_activity)).check(matches(isDisplayed()));
    onView(withText(R.string.deleted)).inRoot(withDecorView(not(is(getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    Log.d(TAG, "DeletePermanently-Positive test end.\n");
  }

  @Test
  public void test7mainActivityEditTitleTest() {
    Log.d(TAG, "Edit Title test start.");
    MainActivity mainActivity = null;
    try {
      mainActivity = (MainActivity) getCurrentActivity();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }

    RecyclerView lv = (RecyclerView) mainActivity.findViewById(R.id.record_list);
    TextView textViewDrawerTitle = (TextView) lv.getChildAt(1).findViewById(R.id.list_item_title);
    String selectedFromList = textViewDrawerTitle.getText().toString();
    onView(withText(selectedFromList)).perform(longClick());
    onView(withText(mainActivity.getResources().getStringArray(R.array.record_options)[0])).perform(click());
    onView(withText(selectedFromList)).perform(typeText("UI-Test"));
    onView(withText(R.string.ok)).perform(click());
    onView(withText(R.string.changed)).inRoot(withDecorView(not(is(getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    Log.d(TAG, "Edit Title test end.\n");
  }


  @Test
  public void testDmainActivityDownloadNegativeTest() {
    Log.d(TAG, "DownLoad-Negative test start.");
    onView(withId(R.id.record_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
    onView(withText(R.string.record_not_found)).inRoot(withDecorView(not(is(getActivity().getWindow().getDecorView())))).check(matches(isDisplayed()));
    onView(withText(R.string.cancel)).perform(click());
    onView(withId(R.id.main_activity)).check(matches(isDisplayed()));
    Log.d(TAG, "DownLoad-Negative test end.\n");
  }


  @Test
  public void test8editActivityOpenTest() {
    Log.d(TAG, "EditActivity open test start.");
    onView(withId(R.id.record_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
    onView(withId(R.id.edit_activity)).check(matches(isDisplayed()));
    Log.d(TAG, "EditActivity open test end.\n");
  }
  @Test
  public void test9editActivityBookmarkHistoryCheck() {
    Log.d(TAG, "BookmarkHistory play test start.");
    onView(withId(R.id.record_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
    onView(withId(R.id.edit_activity)).check(matches(isDisplayed()));
    onView(withId(R.id.viewpager)).perform(swipeLeft());

    onView(withId(R.id.bookmark_history_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, click()));
    onView(withId(R.id.bookmark_history_list)).perform(RecyclerViewActions.actionOnItemAtPosition(0, click()));
    Log.d(TAG, "BookmarkHistory play test end.\n");
  }

  @Test
  public void testFmainActivityDeletePermanentlyNegativeTest() {
    Log.d(TAG, "DeletePermanently-Negative test start.");
    MainActivity mainActivity = null;
    try {
      mainActivity = (MainActivity) getCurrentActivity();
    } catch (Throwable throwable) {
      throwable.printStackTrace();
    }

    onView(withId(R.id.record_list)).perform(RecyclerViewActions.actionOnItemAtPosition(1, longClick()));
    onView(withText(mainActivity.getResources().getStringArray(R.array.record_options)[1])).perform(click());
    onView(withText(R.string.cancel)).perform(click());
    onView(withId(R.id.main_activity)).check(matches(isDisplayed()));
    Log.d(TAG, "DeletePermanently-Ne3gative test end.\n");
  }

  private static Matcher<View> withImageDrawable(final int resourceId) {
    return new BoundedMatcher<View, ImageView>(ImageView.class) {
      @Override
      public void describeTo(Description description) {
        description.appendText("has image drawable resource " + resourceId);
      }

      @Override
      public boolean matchesSafely(ImageView imageView) {
        return sameBitmap(imageView.getContext(), imageView.getDrawable(), resourceId);
      }
    };
  }

  private static boolean sameBitmap(Context context, Drawable drawable, int resourceId) {
    Drawable otherDrawable = context.getResources().getDrawable(resourceId);
    if (drawable == null || otherDrawable == null) {
      return false;
    }
    if (drawable instanceof StateListDrawable && otherDrawable instanceof StateListDrawable) {
      drawable = drawable.getCurrent();
      otherDrawable = otherDrawable.getCurrent();
    }
    if (drawable instanceof BitmapDrawable) {
      Bitmap bitmap = ((BitmapDrawable) drawable).getBitmap();
      Bitmap otherBitmap = ((BitmapDrawable) otherDrawable).getBitmap();
      return bitmap.sameAs(otherBitmap);
    }
    return false;
  }

  Activity getCurrentActivity() throws Throwable {
    getInstrumentation().waitForIdleSync();
    final Activity[] activity = new Activity[1];
    runTestOnUiThread(new Runnable() {
      @Override
      public void run() {
        java.util.Collection<Activity> activites = ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(Stage.RESUMED);
        activity[0] = Iterables.getOnlyElement(activites);
      }});
    return activity[0];
  }
}