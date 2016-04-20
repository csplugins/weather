/**
   COPYRIGHT (C) 2015 Tim O’Neil. All rights reserved.
   Weather forecast assignment: get and parse 6-day forecast from National Weather Service.
   queryWebService() method by Glen Mazza, https://web-gmazza.rhcloud.com/blog/entry/calling-rpc-encoded-web-services
   @author Tim O’Neil
   @version 1.01 10.20.2015
*/

package proxyassignment;

import java.io.StringReader;
import java.net.URL;
import java.time.LocalDate;
import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.soap.MessageFactory;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPBodyElement;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPFactory;
import javax.xml.soap.SOAPFault;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.ws.Dispatch;
import javax.xml.ws.Service;
import javax.xml.ws.soap.SOAPFaultException;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class WeatherForecast implements Weather {

    public WeatherForecast(String z) {
        zipCode = z;
        buildForecast();
    }

    public WeatherForecast() {
        zipCode = "44325";
        buildForecast();
    }
    
    private void buildForecast() {
        data = new WeatherData[12];
        parse(queryWebService(zipCode));
    }
    
    @Override
    public WeatherData[] getForecast() {
        return data;
    }

    private String queryWebService(String zipCode) {
        try {
            String nsSchema = "http://graphical.weather.gov/xml/DWMLgen/schema/DWML.xsd";
            String soapSchema = "http://schemas.xmlsoap.org/soap/envelope/";
            String xsiSchema = "http://www.w3.org/2001/XMLSchema-instance";
            String encodingStyle = "http://schemas.xmlsoap.org/soap/encoding/";
            String zipRequest = "<S:Envelope "
                    + " xmlns:S=\"" + soapSchema + "\" "
                    + " xmlns:xsi=\"" + xsiSchema + "\" "
                    + " xmlns:schNS=\"" + nsSchema + "\"> "
                    + "<S:Body>"
                    + "   <LatLonListZipCode S:encodingStyle=\""
                    + encodingStyle + "\">"
                    + "<schNS:zipCodeList xsi:type=\"schNS:zipCodeListType\">"
                    + zipCode
                    + "</schNS:zipCodeList>"
                    + "</LatLonListZipCode>"
                    + "</S:Body>"
                    + "</S:Envelope>";
            String wsdl = "http://graphical.weather.gov/xml/SOAP_server/ndfdXMLserver.php?wsdl";
            String targetNS = "http://graphical.weather.gov/xml/DWMLgen/wsdl/ndfdXML.wsdl";
            URL url = new URL(wsdl);
            QName serviceName = new QName(targetNS, "ndfdXML");
            QName portName = new QName(targetNS, "ndfdXMLPort");
            Service service = Service.create(url, serviceName);
            Dispatch<Source> dispatch = service.createDispatch(portName,
                    Source.class, Service.Mode.MESSAGE);
            Source zipResponse = dispatch.invoke(
                    new StreamSource(new StringReader(zipRequest)));
            MessageFactory msgFactory = MessageFactory.newInstance();
            SOAPMessage geocodeMsg = msgFactory.createMessage();
            SOAPPart env = geocodeMsg.getSOAPPart();
            env.setContent(zipResponse);
            if (geocodeMsg.getSOAPBody().hasFault()) {
                SOAPFault fault = geocodeMsg.getSOAPBody().getFault();
                System.out.println("Could not obtain forecast for zipcode "
                        + zipCode + ": "
                        + fault.getFaultString() + "; " + fault.getDetail().getValue());
            }
            String geocodeBuffer = geocodeMsg.getSOAPBody().
                    getElementsByTagName("listLatLonOut")
                    .item(0).getFirstChild().getNodeValue();
            String geocodeVals = geocodeBuffer.substring(
                    geocodeBuffer.indexOf("<latLonList>") + 12,
                    geocodeBuffer.indexOf("</latLonList>"));
            SOAPFactory soapFactory = SOAPFactory.newInstance();
            SOAPMessage getWeatherMsg = msgFactory.createMessage();
            SOAPHeader header = getWeatherMsg.getSOAPHeader();
            header.detachNode();  // no header needed
            SOAPBody body = getWeatherMsg.getSOAPBody();
            Name functionCall = soapFactory.createName(
                    "NDFDgenLatLonList", "schNS",
                    nsSchema);
            SOAPBodyElement fcElement = body.addBodyElement(functionCall);
            Name attname = soapFactory.createName("encodingStyle", "S",
                    soapSchema);
            fcElement.addAttribute(attname, soapSchema);
            SOAPElement geocodeElement = fcElement.addChildElement("listLatLon");
            geocodeElement.addTextNode(geocodeVals);
            SOAPElement product = fcElement.addChildElement("product");
            product.addTextNode("glance");
            Dispatch<SOAPMessage> smDispatch = null;
            smDispatch = service.createDispatch(portName,
                    SOAPMessage.class, Service.Mode.MESSAGE);
            SOAPMessage weatherMsg = smDispatch.invoke(getWeatherMsg);
            weatherMsg.getSOAPBody().getElementsByTagName("dwmlOut")
                    .item(0).normalize();
            String weatherResponse = weatherMsg.getSOAPBody().
                    getElementsByTagName("dwmlOut")
                    .item(0).getFirstChild().getNodeValue();
            return weatherResponse;
        } catch (SOAPFaultException e) {
            System.out.println("SOAPFaultException: " + e.getFault().getFaultString());
        } catch (Exception e) {
            System.out.println("Exception: " + e.getMessage());
        }
        return ("Error");
    }
    private void parse(String s) {
        try {
            WeatherData[] wd = new WeatherData[14];
            LocalDate d = LocalDate.now();
            for (int i = 0; i < 14; i += 2) {
                wd[i] = new WeatherData();
                wd[i].setAMPM("     ");
                wd[i + 1] = new WeatherData();
                wd[i + 1].setAMPM("Night");
                wd[i].setDate(d);
                wd[i + 1].setDate(d);
                d = d.plusDays(1);
            }
            SAXParserFactory f = SAXParserFactory.newInstance();
            SAXParser sp = f.newSAXParser();
            DefaultHandler h = new DefaultHandler() {
                boolean isName = false, isIcon = false, wasSet = true;
                int count = 0, i = 0;
                public void startElement(String u, String ln, String qName, Attributes a) throws SAXException {
                    if (qName.equalsIgnoreCase("Temperature")) {
                        isName = true;
                    }
                    if (qName.equalsIgnoreCase("icon-link")) {
                        isIcon = true;
                    }
                }
                public void endElement(String u, String ln, String qName) throws SAXException {
                    if (qName.equalsIgnoreCase("Temperature")) {
                        isName = false;
                    }
                    if (qName.equalsIgnoreCase("icon-link")) {
                        isIcon = false;
                    }
                }
                public void characters(char c[], int s, int l) throws SAXException {
                    if (isName) {
                        String x = new String(c, s, l);
                        if (x.length() < 5) {
                            wd[count].setTemp(Integer.parseInt(x));
                            count += 2;
                            if (count > 13) {
                                count = 1;
                            }
                        }
                    }
                    if (isIcon) {
                        String x = new String(c, s, l);
                        if ((x.substring(39, 40)).equalsIgnoreCase("N") && !wasSet) {
                            if (i < 14) {
                                wd[i].setDescr(x);
                                i++;
                            }
                            wasSet = true;
                        } else if (!((x.substring(39, 40)).equalsIgnoreCase("N")) && wasSet) {
                            if (i < 14) {
                                wd[i].setDescr(x);
                                i++;
                            }
                            wasSet = false;
                        }
                    }
                }
            };
            sp.parse(new InputSource(new StringReader(s)), h);
            System.arraycopy(wd, 0, data, 0, 12);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private String zipCode;
    private WeatherData[] data;
}