/**
 * CC-LGPL 2.1
 * http://creativecommons.org/licenses/LGPL/2.1/
 */
package hu.javaforum.android.androidsoap;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import hu.javaforum.android.soap.Envelope;
import hu.javaforum.android.soap.HttpTransport;
import hu.javaforum.android.soap.HttpsTransport;
import hu.javaforum.android.soap.Transport;
import hu.javaforum.android.soap.impl.DotNetBody;
import hu.javaforum.android.soap.impl.SimpleBody;
import hu.javaforum.android.soap.impl.SimpleEnvelope;
import hu.javaforum.android.soap.impl.SimpleHeader;
import hu.javaforum.mnb.exchangerates.GetCurrencies;
import hu.javaforum.mnb.exchangerates.GetCurrenciesResponse;
import hu.javaforum.mnb.exchangerates.GetExchangeRates;
import hu.javaforum.mnb.exchangerates.GetExchangeRatesResponse;
import hu.javaforum.pop.skinpack.list.ListSkinPacksRequest;
import hu.javaforum.pop.skinpack.list.ListSkinPacksResponse;
import hu.javaforum.pop.skinpack.list.SkinPackMetadata;
import hu.javaforum.w3c.tempconvert.CelsiusToFahrenheit;
import hu.javaforum.w3c.tempconvert.CelsiusToFahrenheitResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * AndroidSOAP example Activity
 *
 * It is:
 * * download a list of the skinpacks (with demo account)
 *
 * Changelog:
 * ANDROIDSOAP-6 - 2011-01-08
 * ANDROIDSOAP-4 - 2011-01-07
 * The first implementation (2010-04-22)
 *
 * @author Gábor AUTH <auth.gabor@javaforum.hu>
 */
public class Main extends Activity
{

  /**
   * onCreate method
   *
   * @param savedInstanceState The saved state
   */
  @Override
  public void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);

    setContentView(R.layout.main);

    LinearLayout layout = (LinearLayout) findViewById(R.main.layout);

    try
    {
      Button w3cButton = (Button) findViewById(R.main.toFarenheit);
      w3cButton.setOnClickListener(new View.OnClickListener()
      {

        public void onClick(View view)
        {
          EditText editText = (EditText) findViewById(R.main.celsius);
          TextView textView = (TextView) findViewById(R.main.farenheit);

          CelsiusToFahrenheit request = new CelsiusToFahrenheit();
          request.setCelsius(editText.getText().toString());
          Map<String, Object> parameters = new HashMap<String, Object>();
          parameters.put("CelsiusToFahrenheit", request);

          Envelope envelope = new SimpleEnvelope("http://tempuri.org/");
          envelope.setHeader(new SimpleHeader());
          envelope.setBody(new DotNetBody("ns", parameters));

          Transport transport = new HttpTransport("http://www.w3schools.com/webservices/tempconvert.asmx");
          CelsiusToFahrenheitResponse response;
          try
          {
            response = transport.call(envelope, CelsiusToFahrenheitResponse.class);
            textView.setText(editText.getText().toString() + "C° is " + response.getCelsiusToFahrenheitResult() + "F°");
          } catch (IOException ex)
          {
            textView.setText(ex.toString());
          }
        }
      });
    } catch (Exception except)
    {
      Log.e(this.getClass().getSimpleName(), except.toString(), except);
    }

    try
    {
      TextView textView = new TextView(this);
      textView.setText("Skins from planets.hu:");
      textView.setTextSize(20.0f);
      layout.addView(textView);
      
      ListSkinPacksRequest request = new ListSkinPacksRequest();
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("request", request);

      Envelope envelope = new SimpleEnvelope("http://skinpack.pop.javaforum.hu/");
      envelope.setHeader(new SimpleHeader());
      envelope.setBody(new SimpleBody("listSkinPacks", parameters));

      HttpsTransport transport = new HttpsTransport("https://services.power.of.planets.hu/PoP-SkinPack-remote/listSkinPacks", "androidsoap.demo@javaforum.hu", "demopassword");
      transport.setTrustAll(Boolean.TRUE);
      ListSkinPacksResponse response = transport.call(envelope, ListSkinPacksResponse.class);

      for (SkinPackMetadata metadata : response.getReturn().getSkinPacksList())
      {
        String fileName = metadata.getFileName();
        String name = metadata.getName();

        TextView row = new TextView(this);
        row.setText(name + " - " + fileName);
        row.setEnabled(true);
        layout.addView(row);
      }
    } catch (Exception except)
    {
      Log.e(this.getClass().getSimpleName(), except.toString(), except);
    }

    try
    {
      TextView textView = new TextView(this);
      textView.setText("Currencies from MNB:");
      textView.setTextSize(20.0f);
      layout.addView(textView);

      GetCurrencies request = new GetCurrencies();
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("GetCurrencies", request);

      Envelope envelope = new SimpleEnvelope("http://www.mnb.hu/webservices/");
      envelope.setHeader(new SimpleHeader());
      envelope.setBody(new DotNetBody("ns", parameters));

      Transport transport = new HttpTransport("http://www.mnb.hu/arfolyamok.asmx");
      GetCurrenciesResponse response = transport.call(envelope, GetCurrenciesResponse.class);

      Log.i(this.getClass().getSimpleName(), "GetCurrenciesResult: " + response.getGetCurrenciesResult());
      TextView row = new TextView(this);
      row.setText(response.getGetCurrenciesResult());
      layout.addView(row);
    } catch (Exception except)
    {
      Log.e(this.getClass().getSimpleName(), except.toString(), except);
    }

    try
    {
      TextView textView = new TextView(this);
      textView.setText("EUR-HUF exchange rate:");
      textView.setTextSize(20.0f);
      layout.addView(textView);

      GetExchangeRates request = new GetExchangeRates();
      request.setStartDate("2010-12-31");
      request.setEndDate("2011-01-08");
      request.setCurrencyNames("EUR");
      Map<String, Object> parameters = new HashMap<String, Object>();
      parameters.put("GetExchangeRates", request);

      Envelope envelope = new SimpleEnvelope("http://www.mnb.hu/webservices/");
      envelope.setHeader(new SimpleHeader());
      envelope.setBody(new DotNetBody("ns", parameters));

      Transport transport = new HttpTransport("http://www.mnb.hu/arfolyamok.asmx");
      GetExchangeRatesResponse response = transport.call(envelope, GetExchangeRatesResponse.class);

      Log.i(this.getClass().getSimpleName(), "getGetExchangeRatesResult(): " + response.getGetExchangeRatesResult());
      TextView row = new TextView(this);
      row.setText(response.getGetExchangeRatesResult());
      layout.addView(row);
    } catch (Exception except)
    {
      Log.e(this.getClass().getSimpleName(), except.toString(), except);
    }
  }

  /**
   * onResume method
   */
  @Override
  public void onResume()
  {
    super.onResume();

    Log.d(this.getClass().getName(), "onResume");
  }

  /**
   * onPause method
   */
  @Override
  public void onPause()
  {
    super.onPause();

    Log.d(this.getClass().getName(), "onPause");
  }
}
