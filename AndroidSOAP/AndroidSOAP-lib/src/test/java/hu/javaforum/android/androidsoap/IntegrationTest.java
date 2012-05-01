/**
 * CC-LGPL 2.1
 * http://creativecommons.org/licenses/LGPL/2.1/
 */
package hu.javaforum.android.androidsoap;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 * Integration tests with real WebService servers. Can be swith on
 * with the -Dandroidsoap.run.itest=true property.
 *
 * @author Gábor Auth <gabor.auth@javaforum.hu>
 */
public class IntegrationTest
{

  /**
   * The PowefOfPlanets: list of skins service.
   *
   * @throws IOException IOException
   */
  @Test
  public final void getPowerOfPlanetsSkins() throws IOException
  {
    if (System.getProperty("androidsoap.run.itest") == null)
    {
      return;
    }

    ListSkinPacksRequest request = new ListSkinPacksRequest();
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("request", request);

    Envelope envelope = new SimpleEnvelope("http://skinpack.pop.javaforum.hu/");
    envelope.setHeader(new SimpleHeader());
    envelope.setBody(new SimpleBody("listSkinPacks", parameters));

    String url = "https://services.power.of.planets.hu/PoP-SkinPack-remote/listSkinPacks";
    String username = "androidsoap.demo@javaforum.hu";
    String password = "demopassword";
    HttpsTransport transport = new HttpsTransport(url, username, password);
    transport.setTrustAll(Boolean.TRUE);
    ListSkinPacksResponse response = transport.call(envelope, ListSkinPacksResponse.class);

    List<String> fileNames = new ArrayList<String>();
    List<String> names = new ArrayList<String>();
    for (SkinPackMetadata metadata : response.getReturn().getSkinPacksList())
    {
      fileNames.add(metadata.getFileName());
      names.add(metadata.getName());
    }

    assertTrue(fileNames.contains("default.properties"), "'default.properties' not found");
    assertTrue(fileNames.contains("big.xml"), "'big.xml' not found");
    assertTrue(names.contains("Default skin"), "'Default skin' not found");
    assertTrue(names.contains("Big skin"), "'Big skin' not found");
  }

  /**
   * The MNB (Hungarian National Bank): list of currencies.
   *
   * @throws IOException IOException
   */
  @Test
  public final void getMnbCurrencies() throws IOException
  {
    if (System.getProperty("androidsoap.run.itest") == null)
    {
      return;
    }

    GetCurrencies request = new GetCurrencies();
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("GetCurrencies", request);

    Envelope envelope = new SimpleEnvelope("http://www.mnb.hu/webservices/");
    envelope.setHeader(new SimpleHeader());
    envelope.setBody(new DotNetBody("ns", parameters));

    Transport transport = new HttpTransport("http://www.mnb.hu/arfolyamok.asmx");
    GetCurrenciesResponse response = transport.call(envelope, GetCurrenciesResponse.class);

    assertNotNull(response.getGetCurrenciesResult(), "response is null");
  }

  /**
   * The MNB (Hungarian National Bank): list of currencies.
   *
   * @throws IOException IOException
   */
  @Test
  public final void getMnbEur() throws IOException
  {
    if (System.getProperty("androidsoap.run.itest") == null)
    {
      return;
    }

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

    assertNotNull(response.getGetExchangeRatesResult(), "response is null");
  }

  /**
   * The W3CSchool's TemperatureConvert webservice.
   *
   * @throws IOException IOException
   */
  @Test
  public final void convertCelsiusToFarenheit() throws IOException
  {
    if (System.getProperty("androidsoap.run.itest") == null)
    {
      return;
    }

    CelsiusToFahrenheit request = new CelsiusToFahrenheit();
    request.setCelsius("50");
    Map<String, Object> parameters = new HashMap<String, Object>();
    parameters.put("CelsiusToFahrenheit", request);

    Envelope envelope = new SimpleEnvelope("http://tempuri.org/");
    envelope.setHeader(new SimpleHeader());
    envelope.setBody(new DotNetBody("ns", parameters));

    Transport transport = new HttpTransport("http://www.w3schools.com/webservices/tempconvert.asmx");
    CelsiusToFahrenheitResponse response = transport.call(envelope, CelsiusToFahrenheitResponse.class);

    assertNotNull(response, "response is null");
    assertNotNull(response.getCelsiusToFahrenheitResult(), "response is null");
    assertEquals(response.getCelsiusToFahrenheitResult(), "122", "values are not equal");
  }
}
