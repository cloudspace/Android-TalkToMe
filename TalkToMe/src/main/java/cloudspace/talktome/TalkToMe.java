package cloudspace.talktome;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.content.Context;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.widget.ImageButton;

public class TalkToMe extends Activity {
  /* Response request to contacts picker*/
  private static final int CONTACT_PICKER_RESULT = 1;

  /* String for logging the class name */
  private final String TAG = getClass().getSimpleName();

  @Override
  protected void onCreate(Bundle savedState) {
    super.onCreate(savedState);

    /* Initialize new Activity */
    doCreate(savedState);

    /* Log if anything was restored */
    Log.i(TAG, "onCreate" + (savedState == null ? " Restored state" : ""));
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    /* Inflate the menu; this adds items to the action bar if it is present. */
    getMenuInflater().inflate(R.menu.talk_to_me, menu);
    return true;
  }

  @Override
  protected void onRestart() {
    super.onRestart();

    /* Activity is starting */
    Log.i(TAG, "onRestart");
  }

  @Override
  protected void onStart() {
    super.onStart();

    /* Activity has started */
    Log.i(TAG, "onStart");
  }

  @Override
  protected void onResume() {
    super.onResume();

    /* Activity is interacting with the user */
    Log.i(TAG, "onResume");
  }

  @Override
  protected void onPause() {
    super.onPause();

    /* Activity is no longer interacting with the user */
    Log.i(TAG, "onPause" + (isFinishing() ? " Finishing" : ""));
  }

  @Override
  protected void onStop() {
    super.onStop();

    /* Activity is no longer visible */
    Log.i(TAG, "onStop");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();

    /* Activity is destroyed */
    Log.i(TAG, "onDestroy" + (isFinishing() ? " Finishing" : ""));
  }

  @Override
  protected void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);

    /* Save user information */
    saveState(outState);

    /* State is saving */
    Log.i(TAG, "onSaveInstanceState");
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedState) {
    super.onSaveInstanceState(savedState);

    /* Restore user information */
    if(savedState == null) restoreState(savedState);

    /* Log if anything was restored */
    Log.i(TAG, "onRestoreInstanceState" + (savedState == null ? " Restored state" : ""));
  }

  /********************************
  *****   App Specific Code   *****
  ********************************/

  /**
   * Restore saved information
   * @param savedState Bundle of saved information
   */
  private void restoreState(Bundle savedState){
    // Restore state here
  }

  /**
   * Save information
   * @param outState Bundle of information to be saved
   */
  private void saveState(Bundle outState){
    // Save state here
  }

  /**
   * Perform initialization on creation of Activity
   * @param savedState Bundle of saved information
   */
  private void doCreate(Bundle savedState){
    setContentView(R.layout.activity_main);

    /* Restore user information */
    if(savedState != null) restoreState(savedState);

    /* Declare view components */
    final ImageButton dial = (ImageButton)findViewById(R.id.dial);
    final ImageButton contacts = (ImageButton)findViewById(R.id.contacts);

    /* Create a local listener to track phone call progress */
    PhoneCallListener phoneListener = new PhoneCallListener();
    TelephonyManager telephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
    telephonyManager.listen(phoneListener,PhoneStateListener.LISTEN_CALL_STATE);

    /* Call a new phone number */
    dial.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        callNewNumber();
      }
    });

    /* Call a phone number from contacts */
    contacts.setOnClickListener(new View.OnClickListener() {
      public void onClick(View v) {
        doLaunchContactPicker();
      }
    });
  }

  /**
   * Launch users contact list to call from
   */
  public void doLaunchContactPicker() {
    Intent contactPickerIntent = new Intent(Intent.ACTION_PICK, Contacts.CONTENT_URI);

    /* Show only contacts with a phone number */
    contactPickerIntent.setType(Phone.CONTENT_TYPE);

    /* Launch contact picker and get the phone number of the selected contact */
    startActivityForResult(contactPickerIntent, CONTACT_PICKER_RESULT);
  }

  /**
   * Launch dialer to call a new phone number
   */
  public void callNewNumber(){
    Intent dial = new Intent();
    dial.setAction("android.intent.action.DIAL");

    /* Launch the dialer */
    startActivity(dial);
  }

  /**
   * Bypass the Dialer and automatically call a new phone number
   * @param phoneNumber String containing the number we are calling
   */
  public void dialNumber(String phoneNumber){
    Intent call= new Intent(Intent.ACTION_CALL);

    /* Phone numbers must be in the form of "tel:#######" */
    call.setData(Uri.parse("tel:"+phoneNumber));

    /* Call phone number */
    startActivity(call);
  }

  @Override
  public void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    // Check which request we're responding to
    if (requestCode == CONTACT_PICKER_RESULT) {

      // Make sure the request was successful
      if (resultCode == Activity.RESULT_OK) {
        /* Get the URI that points to the selected contact */
        Uri contactUri = data.getData();

        /* Query the database for this contacts information*/
        Cursor cursor=this.getContentResolver().query(contactUri, null, null, null, null);

        while (cursor != null && cursor.moveToNext()) {
          /* Get the phone number for this contact */
          int column = cursor.getColumnIndex(Phone.NUMBER);
          String phoneNumber = cursor.getString(column);

          /* Call this number */
          dialNumber(phoneNumber);
        }
      }
    }
  }

  private class PhoneCallListener extends PhoneStateListener {
    /* Check if a call is in progress */
    private boolean isPhoneCalling = false;

    @Override
    public void onCallStateChanged(int state, String incomingNumber) {

      if (TelephonyManager.CALL_STATE_RINGING == state) {
        /* Attempting to connect to phone number*/
        Log.i(TAG, "RINGING, number: " + incomingNumber);
      }

      if (TelephonyManager.CALL_STATE_OFFHOOK == state) {
        /* Phone call in progress*/
        Log.i(TAG, "InProgress");
        isPhoneCalling = true;
      }

      if (TelephonyManager.CALL_STATE_IDLE == state) {
        /* Nothing happening*/
        Log.i(TAG, "IDLE");

        /* If the phone is idle and we recently had a phone call, restart the activity */
        if (isPhoneCalling) {
          Log.i(TAG, "restart activity");

          /* restart activity */
          Intent i = getBaseContext().getPackageManager()
                  .getLaunchIntentForPackage(
                          getBaseContext().getPackageName());
          i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
          startActivity(i);

          /* Reset local variable*/
          isPhoneCalling = false;
        }
      }
    }
  }
}
