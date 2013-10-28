package com.helloks.flatpress;

import java.util.Hashtable;

import org.xmlrpc.android.XMLRPCClient;
import org.xmlrpc.android.XMLRPCException;
import org.xmlrpc.android.XMLRPCFault;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class MainActivity extends SherlockActivity {
	
  private XMLRPCClient mClient;
  private EditText Title, Content;
  private EditText address, pw, usrname;
  private String addressValue, pwValue, usrnameValue;
  
  // 액티비티에서 사용할 값을 저장
  private SharedPreferences prvPref;
   
  // 애플리케이션 전체에서 사용할 값
  private SharedPreferences.Editor prvEditor;
  
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    ActionBar bar = getSupportActionBar();
    setContentView(R.layout.activity_main);
    bar.setTitle("FlatPress");
    
    Title = (EditText) findViewById(R.id.title);
    Content = (EditText) findViewById(R.id.content);
    
    prvPref = PreferenceManager.getDefaultSharedPreferences(MainActivity.this);
     
    prvEditor = prvPref.edit();
  }
  
  private void check() {
	  if(Title.getText().toString().trim().length() <= 0) {
		  Toast.makeText(getApplicationContext(), R.string.toast_enter_title, Toast.LENGTH_SHORT).show();
		  return;
	  } else if (Content.getText().toString().trim().length() <= 0) {
		  Toast.makeText(getApplicationContext(), R.string.toast_enter_content, Toast.LENGTH_SHORT).show();
		  return;
	  } else {
		  Log.e("TAG","title : " + Title.getText().toString() + "content : " + Content.getText().toString());
		  feed();
	  }
  }
  
  private void feed()
  {
    try
    {
    		addressValue = prvPref.getString("address", "");
    		usrnameValue = prvPref.getString("usrname", "");
    		pwValue = prvPref.getString("pw", "");
      Hashtable<String, String> hashtable = new Hashtable<String, String>();
      
      hashtable.put("title", Title.getText().toString());
      hashtable.put("description", Content.getText().toString());
      
      Object[] params = new Object[5];
      params[0] = usrnameValue; // blog api id
      params[1] = usrnameValue;    // user id
      params[2] = pwValue;  // password
      params[3] = hashtable;
      params[4] = Boolean.valueOf(true);
      
      mClient = new XMLRPCClient(addressValue);
      
      WritePost method = new WritePost("metaWeblog.newPost", new XMLRPCMethodCallback()
      {
        public void callFinished(Object result)
        {
        }
      });
      method.call(params);          
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
  
	// call back 인터페이스  
	public interface XMLRPCMethodCallback
	{
	  void callFinished(Object result);
	}
  
  public class WritePost extends Thread
  {
  	private String method;
  	private Object[] params;
  	private Handler handler;
  	private XMLRPCMethodCallback callBack;

    public WritePost(String method, XMLRPCMethodCallback callBack)
    {
      this.method = method;
      this.callBack = callBack;
      handler = new Handler();
    }

    public void call()
    {
      call(null);
    }

    public void call(Object[] params)
    {
      this.params = params;
      start();
    }

    @Override
    public void run()
    {
      try
      {
        final Object result = mClient.callEx(method, params);
        handler.post(new Runnable()
        {
          public void run()
          {
            callBack.callFinished(result);
      	  Toast.makeText(getApplicationContext(), R.string.toast_success, Toast.LENGTH_SHORT).show();
          }
        });
      }
      catch (final XMLRPCFault e)
      {
        handler.post(new Runnable()
        {
          public void run()
          {
        	  Toast.makeText(getApplicationContext(), R.string.toast_fail, Toast.LENGTH_LONG).show();
          }
        });
      }
      catch (final XMLRPCException e)
      {
        handler.post(new Runnable()
        {
          public void run()
          {
        	  Toast.makeText(getApplicationContext(), R.string.toast_fail, Toast.LENGTH_LONG).show();
          }
        });
      }
    }
  }

  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
     MenuInflater inflater = getSupportMenuInflater();
     inflater.inflate(R.menu.main, menu);
     return super.onCreateOptionsMenu(menu);  
  }
  
  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
  int id = item.getItemId();
  switch(id){
  case R.id.write:
	  check();
	  break;
  case R.id.settings:
	  showset();
	  break;
  }
  return super.onOptionsItemSelected(item);
  }
  
  private void showset() {
	  Context mContext = getApplicationContext();
	  LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(LAYOUT_INFLATER_SERVICE);
	  View layout = inflater.inflate(R.layout.apidialog,(ViewGroup) findViewById(R.id.layout_root));
      address = (EditText)layout.findViewById(R.id.address);
  	  usrname = (EditText)layout.findViewById(R.id.username);
  	  pw = (EditText)layout.findViewById(R.id.password);
	  						
	  AlertDialog.Builder aDialog = new AlertDialog.Builder(MainActivity.this);
	  aDialog.setTitle(R.string.setting_title);
	  aDialog.setView(layout);
	  						
	  aDialog.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
	  	public void onClick(DialogInterface dialog, int which) {
            String addressValue = address.getText().toString();
            String usrnameValue = usrname.getText().toString();
            String pwValue = pw.getText().toString();
            prvEditor.putString("address", addressValue);
            prvEditor.putString("usrname", usrnameValue);
            prvEditor.putString("pw", pwValue);
            prvEditor.commit();
            Toast.makeText(getApplicationContext(), R.string.toast_saved, Toast.LENGTH_SHORT).show();
	  	}
	  });
	  aDialog.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
	  	public void onClick(DialogInterface dialog, int which) {
	  	}
	  });
	  AlertDialog ad = aDialog.create();
	  ad.show();
  }
  
}