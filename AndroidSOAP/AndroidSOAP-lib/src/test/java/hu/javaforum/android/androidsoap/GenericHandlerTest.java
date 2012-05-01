/**
 * CC-LGPL 2.1
 * http://creativecommons.org/licenses/LGPL/2.1/
 */
package hu.javaforum.android.androidsoap;

import hu.javaforum.android.soap.ExceptionHandler;
import hu.javaforum.android.soap.GenericHandler;
import hu.javaforum.pop.skinpack.list.ListSkinPacksResponse;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import static org.testng.Assert.assertNotNull;
import org.testng.annotations.Test;
import org.xmlpull.v1.XmlPullParserException;

/**
 * Test class of GenericHandler.
 *
 * @author Gábor Auth <gabor.auth@javaforum.hu>
 */
public class GenericHandlerTest
{

  /**
   * Test of a simple response.
   *
   * @throws IOException IOException
   * @throws IllegalAccessException IllegalAccessException
   * @throws InstantiationException InstantiationException
   * @throws NoSuchFieldException NoSuchFieldException
   * @throws XmlPullParserException XmlPullParserException
   */
  @Test
  public final void testResult() throws IOException, InstantiationException,
          IllegalAccessException, XmlPullParserException, NoSuchFieldException
  {
    String soapXml = "<env:Envelope xmlns:env='http://schemas.xmlsoap.org/soap/envelope/'>"
            + "<env:Header></env:Header>"
            + "<env:Body>"
            + "<ns2:listSkinPacksResponse xmlns:ns2=\"http://skinpack.pop.javaforum.hu/\">"
            + "<return>"
            + "<skinPacksList>"
            + "<systemName>default</systemName>"
            + "<fileName>default.properties</fileName>"
            + "<name>Default skin</name>"
            + "<version>2010-10-18 21:06:00</version>"
            + "</skinPacksList>"
            + "<skinPacksList>"
            + "<systemName>big</systemName>"
            + "<fileName>big.xml</fileName>"
            + "<name>Big skin</name>"
            + "<version>2010-10-18 21:07:00</version>"
            + "</skinPacksList>"
            + "</return>"
            + "</ns2:listSkinPacksResponse>"
            + "</env:Body>"
            + "</env:Envelope>";
    InputStream is = new ByteArrayInputStream(soapXml.getBytes("UTF-8"));

    GenericHandler gh = new GenericHandler(ListSkinPacksResponse.class);
    gh.parseWithPullParser(is);
    is.close();
    ListSkinPacksResponse response = (ListSkinPacksResponse) gh.getObject();
    assertNotNull(response, "response is null");
    assertNotNull(response.getReturn(), "return is null");
    assertNotNull(response.getReturn().getSkinPacksList(), "list is null");
  }

  @Test
  public final void testFault() throws UnsupportedEncodingException,
          InstantiationException, IllegalAccessException, XmlPullParserException,
          IOException, NoSuchFieldException
  {
    String soapXml = "<?xml version=\"1.0\" encoding=\"utf-8\"?>"
            + "<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\" "
            + "xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "
            + "xmlns:xsd=\"http://www.w3.org/2001/XMLSchema\">"
            + "  <soap:Body>"
            + "    <soap:Fault>"
            + "     <faultcode>soap:Server</faultcode>"
            + "     <faultstring>Server was unable to process request.</faultstring>"
            + "     <detail>"
            + "       <e:myfaultdetails xmlns:e=\"Some-URI\">"
            + "         <message>"
            + "            My application didn't work"
            + "         </message>"
            + "         <errorcode>"
            + "            1001"
            + "         </errorcode>"
            + "       </e:myfaultdetails>"
            + "     </detail>"
            + "   </soap:Fault>"
            + " </soap:Body>"
            + "</soap:Envelope>";

    InputStream is = new ByteArrayInputStream(soapXml.getBytes("UTF-8"));
    ExceptionHandler gh = new ExceptionHandler(Exception.class);
  }
}
