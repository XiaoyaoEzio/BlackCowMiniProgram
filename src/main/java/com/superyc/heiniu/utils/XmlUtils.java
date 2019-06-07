package com.superyc.heiniu.utils;

import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import javax.xml.bind.*;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;

/**
 *
 */
@Component
public class XmlUtils {
    public static <T> String getXmlStr(T t, Class<T> clazz) {
        JAXBContext context = getJAXBContext(clazz);

        Marshaller marshaller = null;
        try {
            marshaller = context.createMarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        Assert.notNull(marshaller);

        try {
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
        } catch (PropertyException e) {
            e.printStackTrace();
        }

        Writer writer = new StringWriter();
        try {
            marshaller.marshal(t, writer);
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return writer.toString();
    }

    public static  <T> T parseXml(String xmlData, Class<T> clazz) {
        JAXBContext context = getJAXBContext(clazz);
        Unmarshaller unmarshaller = null;
        try {
            unmarshaller = context.createUnmarshaller();
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        Assert.notNull(unmarshaller);

        T result = null;
        try {
            result = (T) unmarshaller.unmarshal(new StringReader(xmlData));
        } catch (JAXBException e) {
            e.printStackTrace();
        }

        return result;
    }

    private static <T> JAXBContext getJAXBContext(Class<T> clazz) {
        JAXBContext context = null;
        try {
            context = JAXBContext.newInstance(clazz);
        } catch (JAXBException e) {
            e.printStackTrace();
        }
        Assert.notNull(context);

        return context;
    }
}
