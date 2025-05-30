package com.john.freezeapp.daemon.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Xml;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ProtocolException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class XmlUtils {

    public static final void writeMapXml(Map val, OutputStream out)
            throws XmlPullParserException, IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(out, StandardCharsets.UTF_8.name());
        serializer.startDocument(null, true);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        writeMapXml(val, null, serializer);
        serializer.endDocument();
    }


    public static final void writeListXml(List val, OutputStream out)
            throws XmlPullParserException, IOException {
        XmlSerializer serializer = Xml.newSerializer();
        serializer.setOutput(out, StandardCharsets.UTF_8.name());
        serializer.startDocument(null, true);
        serializer.setFeature("http://xmlpull.org/v1/doc/features.html#indent-output", true);
        writeListXml(val, null, serializer);
        serializer.endDocument();
    }


    public static final void writeMapXml(Map val, String name, XmlSerializer out)
            throws XmlPullParserException, IOException {
        writeMapXml(val, name, out, null);
    }


    public static final void writeMapXml(Map val, String name, XmlSerializer out,
                                         WriteMapCallback callback) throws XmlPullParserException, IOException {

        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "map");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        writeMapXml(val, out, callback);

        out.endTag(null, "map");
    }


    public static final void writeMapXml(Map val, XmlSerializer out,
                                         WriteMapCallback callback) throws XmlPullParserException, IOException {
        if (val == null) {
            return;
        }

        Set s = val.entrySet();
        Iterator i = s.iterator();

        while (i.hasNext()) {
            Map.Entry e = (Map.Entry) i.next();
            writeValueXml(e.getValue(), (String) e.getKey(), out, callback);
        }
    }

    public static final void writeListXml(List val, String name, XmlSerializer out)
            throws XmlPullParserException, IOException {
        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "list");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        int N = val.size();
        int i = 0;
        while (i < N) {
            writeValueXml(val.get(i), null, out);
            i++;
        }

        out.endTag(null, "list");
    }

    public static final void writeSetXml(Set val, String name, XmlSerializer out)
            throws XmlPullParserException, IOException {
        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "set");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        for (Object v : val) {
            writeValueXml(v, null, out);
        }

        out.endTag(null, "set");
    }


    public static final void writeByteArrayXml(byte[] val, String name,
                                               XmlSerializer out)
            throws XmlPullParserException, IOException {

        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "byte-array");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        final int N = val.length;
        out.attribute(null, "num", String.valueOf(N));
        out.text(new String(val));
        out.endTag(null, "byte-array");
    }


    public static final void writeIntArrayXml(int[] val, String name,
                                              XmlSerializer out)
            throws XmlPullParserException, IOException {

        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "int-array");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        final int N = val.length;
        out.attribute(null, "num", String.valueOf(N));

        for (int i = 0; i < N; i++) {
            out.startTag(null, "item");
            out.attribute(null, "value", String.valueOf(val[i]));
            out.endTag(null, "item");
        }

        out.endTag(null, "int-array");
    }


    public static final void writeLongArrayXml(long[] val, String name, XmlSerializer out)
            throws XmlPullParserException, IOException {

        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "long-array");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        final int N = val.length;
        out.attribute(null, "num", String.valueOf(N));

        for (int i = 0; i < N; i++) {
            out.startTag(null, "item");
            out.attribute(null, "value", String.valueOf(val[i]));
            out.endTag(null, "item");
        }

        out.endTag(null, "long-array");
    }


    public static final void writeDoubleArrayXml(double[] val, String name, XmlSerializer out)
            throws XmlPullParserException, IOException {

        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "double-array");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        final int N = val.length;
        out.attribute(null, "num", String.valueOf(N));

        for (int i = 0; i < N; i++) {
            out.startTag(null, "item");
            out.attribute(null, "value", String.valueOf(val[i]));
            out.endTag(null, "item");
        }

        out.endTag(null, "double-array");
    }


    public static final void writeStringArrayXml(String[] val, String name, XmlSerializer out)
            throws XmlPullParserException, IOException {

        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "string-array");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        final int N = val.length;
        out.attribute(null, "num", String.valueOf(N));

        for (int i = 0; i < N; i++) {
            out.startTag(null, "item");
            out.attribute(null, "value", val[i]);
            out.endTag(null, "item");
        }

        out.endTag(null, "string-array");
    }


    public static final void writeBooleanArrayXml(boolean[] val, String name,
                                                  XmlSerializer out) throws XmlPullParserException, IOException {

        if (val == null) {
            out.startTag(null, "null");
            out.endTag(null, "null");
            return;
        }

        out.startTag(null, "boolean-array");
        if (name != null) {
            out.attribute(null, "name", name);
        }

        final int N = val.length;
        out.attribute(null, "num", String.valueOf(N));

        for (int i = 0; i < N; i++) {
            out.startTag(null, "item");
            out.attribute(null, "value", String.valueOf(val[i]));
            out.endTag(null, "item");
        }

        out.endTag(null, "boolean-array");
    }


    public static final void writeValueXml(Object v, String name, XmlSerializer out)
            throws XmlPullParserException, IOException {
        writeValueXml(v, name, out, null);
    }


    private static final void writeValueXml(Object v, String name, XmlSerializer out,
                                            WriteMapCallback callback) throws XmlPullParserException, IOException {
        if (v == null) {
            out.startTag(null, "null");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.endTag(null, "null");
            return;
        } else if (v instanceof String) {
            out.startTag(null, "string");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.text(v.toString());
            out.endTag(null, "string");
            return;
        } else if (v instanceof Integer) {
            out.startTag(null, "int");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.attribute(null, "value", String.valueOf(v));
            out.endTag(null, "int");
        } else if (v instanceof Long) {
            out.startTag(null, "long");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.attribute(null, "value", String.valueOf(v));
            out.endTag(null, "long");
        } else if (v instanceof Float) {
            out.startTag(null, "float");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.attribute(null, "value", String.valueOf(v));
            out.endTag(null, "float");
        } else if (v instanceof Double) {
            out.startTag(null, "double");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.attribute(null, "value", String.valueOf(v));
            out.endTag(null, "double");
        } else if (v instanceof Boolean) {
            out.startTag(null, "boolean");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.attribute(null, "value", String.valueOf(v));
            out.endTag(null, "boolean");
        } else if (v instanceof byte[]) {
            writeByteArrayXml((byte[]) v, name, out);
            return;
        } else if (v instanceof int[]) {
            writeIntArrayXml((int[]) v, name, out);
            return;
        } else if (v instanceof long[]) {
            writeLongArrayXml((long[]) v, name, out);
            return;
        } else if (v instanceof double[]) {
            writeDoubleArrayXml((double[]) v, name, out);
            return;
        } else if (v instanceof String[]) {
            writeStringArrayXml((String[]) v, name, out);
            return;
        } else if (v instanceof boolean[]) {
            writeBooleanArrayXml((boolean[]) v, name, out);
            return;
        } else if (v instanceof Map) {
            writeMapXml((Map) v, name, out);
            return;
        } else if (v instanceof List) {
            writeListXml((List) v, name, out);
            return;
        } else if (v instanceof Set) {
            writeSetXml((Set) v, name, out);
            return;
        } else if (v instanceof CharSequence) {
            // XXX This is to allow us to at least write something if
            // we encounter styled text...  but it means we will drop all
            // of the styling information. :(
            out.startTag(null, "string");
            if (name != null) {
                out.attribute(null, "name", name);
            }
            out.text(v.toString());
            out.endTag(null, "string");
            return;
        } else if (callback != null) {
            callback.writeUnknownObject(v, name, out);
            return;
        } else {
            throw new RuntimeException("writeValueXml: unable to write value " + v);
        }
    }


    public static final HashMap<String, ?> readMapXml(InputStream in)
            throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, StandardCharsets.UTF_8.name());
        return (HashMap<String, ?>) readValueXml(parser, new String[1]);
    }


    public static final ArrayList readListXml(InputStream in)
            throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, StandardCharsets.UTF_8.name());
        return (ArrayList) readValueXml(parser, new String[1]);
    }



    public static final HashSet readSetXml(InputStream in)
            throws XmlPullParserException, IOException {
        XmlPullParser parser = Xml.newPullParser();
        parser.setInput(in, StandardCharsets.UTF_8.name());
        return (HashSet) readValueXml(parser, new String[1]);
    }


    public static final HashMap<String, ?> readThisMapXml(XmlPullParser parser, String endTag,
                                                          String[] name) throws XmlPullParserException, IOException {
        return readThisMapXml(parser, endTag, name, null);
    }


    public static final HashMap<String, ?> readThisMapXml(XmlPullParser parser, String endTag,
                                                          String[] name, ReadMapCallback callback)
            throws XmlPullParserException, IOException {
        HashMap<String, Object> map = new HashMap<String, Object>();

        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                Object val = readThisValueXml(parser, name, callback, false);
                map.put(name[0], val);
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return map;
                }
                throw new XmlPullParserException(
                        "Expected " + endTag + " end tag at: " + parser.getName());
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException(
                "Document ended before " + endTag + " end tag");
    }


    public static final ArrayMap<String, ?> readThisArrayMapXml(XmlPullParser parser,
                                                                String endTag, String[] name, ReadMapCallback callback)
            throws XmlPullParserException, IOException {
        ArrayMap<String, Object> map = new ArrayMap<>();

        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                Object val = readThisValueXml(parser, name, callback, true);
                map.put(name[0], val);
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return map;
                }
                throw new XmlPullParserException(
                        "Expected " + endTag + " end tag at: " + parser.getName());
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException(
                "Document ended before " + endTag + " end tag");
    }


    public static final ArrayList readThisListXml(XmlPullParser parser, String endTag,
                                                  String[] name) throws XmlPullParserException, IOException {
        return readThisListXml(parser, endTag, name, null, false);
    }


    private static final ArrayList readThisListXml(XmlPullParser parser, String endTag,
                                                   String[] name, ReadMapCallback callback, boolean arrayMap)
            throws XmlPullParserException, IOException {
        ArrayList list = new ArrayList();

        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                Object val = readThisValueXml(parser, name, callback, arrayMap);
                list.add(val);
                //System.out.println("Adding to list: " + val);
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return list;
                }
                throw new XmlPullParserException(
                        "Expected " + endTag + " end tag at: " + parser.getName());
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException(
                "Document ended before " + endTag + " end tag");
    }


    public static final HashSet readThisSetXml(XmlPullParser parser, String endTag,
                                               String[] name) throws XmlPullParserException, IOException {
        return readThisSetXml(parser, endTag, name, null, false);
    }


    private static final HashSet readThisSetXml(XmlPullParser parser, String endTag,
                                                String[] name, ReadMapCallback callback, boolean arrayMap)
            throws XmlPullParserException, IOException {
        HashSet set = new HashSet();

        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                Object val = readThisValueXml(parser, name, callback, arrayMap);
                set.add(val);
                //System.out.println("Adding to set: " + val);
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return set;
                }
                throw new XmlPullParserException(
                        "Expected " + endTag + " end tag at: " + parser.getName());
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException(
                "Document ended before " + endTag + " end tag");
    }


    public static final byte[] readThisByteArrayXml(XmlPullParser parser,
                                                    String endTag, String[] name)
            throws XmlPullParserException, IOException {

        int num = readIntAttribute(parser, "num");

        // 0 len byte array does not have a text in the XML tag. So, initialize to 0 len array.
        // For all other array lens, HexEncoding.decode() below overrides the array.
        byte[] array = new byte[0];

        int eventType = parser.getEventType();
        do {
            if (eventType == parser.TEXT) {
                if (num > 0) {
                    String values = parser.getText();
                    if (values == null || values.length() != num * 2) {
                        throw new XmlPullParserException(
                                "Invalid value found in byte-array: " + values);
                    }
                    array = values.getBytes();
                }
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return array;
                } else {
                    throw new XmlPullParserException(
                            "Expected " + endTag + " end tag at: "
                                    + parser.getName());
                }
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException(
                "Document ended before " + endTag + " end tag");
    }


    public static final int[] readThisIntArrayXml(XmlPullParser parser,
                                                  String endTag, String[] name)
            throws XmlPullParserException, IOException {

        int num = readIntAttribute(parser, "num");
        parser.next();

        int[] array = new int[num];
        int i = 0;

        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                if (parser.getName().equals("item")) {
                    array[i] = readIntAttribute(parser, "value");
                } else {
                    throw new XmlPullParserException(
                            "Expected item tag at: " + parser.getName());
                }
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return array;
                } else if (parser.getName().equals("item")) {
                    i++;
                } else {
                    throw new XmlPullParserException(
                            "Expected " + endTag + " end tag at: "
                                    + parser.getName());
                }
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException(
                "Document ended before " + endTag + " end tag");
    }


    public static final long[] readThisLongArrayXml(XmlPullParser parser,
                                                    String endTag, String[] name)
            throws XmlPullParserException, IOException {

        int num = readIntAttribute(parser, "num");
        parser.next();

        long[] array = new long[num];
        int i = 0;

        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                if (parser.getName().equals("item")) {
                    array[i] = readLongAttribute(parser, "value");
                } else {
                    throw new XmlPullParserException("Expected item tag at: " + parser.getName());
                }
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return array;
                } else if (parser.getName().equals("item")) {
                    i++;
                } else {
                    throw new XmlPullParserException("Expected " + endTag + " end tag at: " +
                            parser.getName());
                }
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException("Document ended before " + endTag + " end tag");
    }


    public static final double[] readThisDoubleArrayXml(XmlPullParser parser, String endTag,
                                                        String[] name) throws XmlPullParserException, IOException {

        int num = readIntAttribute(parser, "num");
        parser.next();

        double[] array = new double[num];
        int i = 0;

        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                if (parser.getName().equals("item")) {
                    array[i] = readDoubleAttribute(parser, "value");
                } else {
                    throw new XmlPullParserException("Expected item tag at: " + parser.getName());
                }
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return array;
                } else if (parser.getName().equals("item")) {
                    i++;
                } else {
                    throw new XmlPullParserException("Expected " + endTag + " end tag at: " +
                            parser.getName());
                }
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException("Document ended before " + endTag + " end tag");
    }


    public static final String[] readThisStringArrayXml(XmlPullParser parser, String endTag,
                                                        String[] name) throws XmlPullParserException, IOException {

        int num = readIntAttribute(parser, "num");
        parser.next();

        String[] array = new String[num];
        int i = 0;

        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                if (parser.getName().equals("item")) {
                    array[i] = parser.getAttributeValue(null, "value");
                } else {
                    throw new XmlPullParserException("Expected item tag at: " + parser.getName());
                }
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return array;
                } else if (parser.getName().equals("item")) {
                    i++;
                } else {
                    throw new XmlPullParserException("Expected " + endTag + " end tag at: " +
                            parser.getName());
                }
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException("Document ended before " + endTag + " end tag");
    }

    public static final boolean[] readThisBooleanArrayXml(XmlPullParser parser, String endTag,
                                                          String[] name) throws XmlPullParserException, IOException {

        int num = readIntAttribute(parser, "num");
        parser.next();

        boolean[] array = new boolean[num];
        int i = 0;

        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                if (parser.getName().equals("item")) {
                    array[i] = readBooleanAttribute(parser, "value");
                } else {
                    throw new XmlPullParserException("Expected item tag at: " + parser.getName());
                }
            } else if (eventType == parser.END_TAG) {
                if (parser.getName().equals(endTag)) {
                    return array;
                } else if (parser.getName().equals("item")) {
                    i++;
                } else {
                    throw new XmlPullParserException("Expected " + endTag + " end tag at: " +
                            parser.getName());
                }
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException("Document ended before " + endTag + " end tag");
    }


    public static final Object readValueXml(XmlPullParser parser, String[] name)
            throws XmlPullParserException, IOException {
        int eventType = parser.getEventType();
        do {
            if (eventType == parser.START_TAG) {
                return readThisValueXml(parser, name, null, false);
            } else if (eventType == parser.END_TAG) {
                throw new XmlPullParserException(
                        "Unexpected end tag at: " + parser.getName());
            } else if (eventType == parser.TEXT) {
                throw new XmlPullParserException(
                        "Unexpected text: " + parser.getText());
            }
            eventType = parser.next();
        } while (eventType != parser.END_DOCUMENT);

        throw new XmlPullParserException(
                "Unexpected end of document");
    }

    private static final Object readThisValueXml(XmlPullParser parser, String[] name,
                                                 ReadMapCallback callback, boolean arrayMap)
            throws XmlPullParserException, IOException {
        final String valueName = parser.getAttributeValue(null, "name");
        final String tagName = parser.getName();

        //System.out.println("Reading this value tag: " + tagName + ", name=" + valueName);

        Object res;

        if (tagName.equals("null")) {
            res = null;
        } else if (tagName.equals("string")) {
            final StringBuilder value = new StringBuilder();
            int eventType;
            while ((eventType = parser.next()) != parser.END_DOCUMENT) {
                if (eventType == parser.END_TAG) {
                    if (parser.getName().equals("string")) {
                        name[0] = valueName;
                        //System.out.println("Returning value for " + valueName + ": " + value);
                        return value.toString();
                    }
                    throw new XmlPullParserException(
                            "Unexpected end tag in <string>: " + parser.getName());
                } else if (eventType == parser.TEXT) {
                    value.append(parser.getText());
                } else if (eventType == parser.START_TAG) {
                    throw new XmlPullParserException(
                            "Unexpected start tag in <string>: " + parser.getName());
                }
            }
            throw new XmlPullParserException(
                    "Unexpected end of document in <string>");
        } else if ((res = readThisPrimitiveValueXml(parser, tagName)) != null) {
            // all work already done by readThisPrimitiveValueXml
        } else if (tagName.equals("byte-array")) {
            res = readThisByteArrayXml(parser, "byte-array", name);
            name[0] = valueName;
            //System.out.println("Returning value for " + valueName + ": " + res);
            return res;
        } else if (tagName.equals("int-array")) {
            res = readThisIntArrayXml(parser, "int-array", name);
            name[0] = valueName;
            //System.out.println("Returning value for " + valueName + ": " + res);
            return res;
        } else if (tagName.equals("long-array")) {
            res = readThisLongArrayXml(parser, "long-array", name);
            name[0] = valueName;
            //System.out.println("Returning value for " + valueName + ": " + res);
            return res;
        } else if (tagName.equals("double-array")) {
            res = readThisDoubleArrayXml(parser, "double-array", name);
            name[0] = valueName;
            //System.out.println("Returning value for " + valueName + ": " + res);
            return res;
        } else if (tagName.equals("string-array")) {
            res = readThisStringArrayXml(parser, "string-array", name);
            name[0] = valueName;
            //System.out.println("Returning value for " + valueName + ": " + res);
            return res;
        } else if (tagName.equals("boolean-array")) {
            res = readThisBooleanArrayXml(parser, "boolean-array", name);
            name[0] = valueName;
            //System.out.println("Returning value for " + valueName + ": " + res);
            return res;
        } else if (tagName.equals("map")) {
            parser.next();
            res = arrayMap
                    ? readThisArrayMapXml(parser, "map", name, callback)
                    : readThisMapXml(parser, "map", name, callback);
            name[0] = valueName;
            //System.out.println("Returning value for " + valueName + ": " + res);
            return res;
        } else if (tagName.equals("list")) {
            parser.next();
            res = readThisListXml(parser, "list", name, callback, arrayMap);
            name[0] = valueName;
            //System.out.println("Returning value for " + valueName + ": " + res);
            return res;
        } else if (tagName.equals("set")) {
            parser.next();
            res = readThisSetXml(parser, "set", name, callback, arrayMap);
            name[0] = valueName;
            //System.out.println("Returning value for " + valueName + ": " + res);
            return res;
        } else if (callback != null) {
            res = callback.readThisUnknownObjectXml(parser, tagName);
            name[0] = valueName;
            return res;
        } else {
            throw new XmlPullParserException("Unknown tag: " + tagName);
        }

        // Skip through to end tag.
        int eventType;
        while ((eventType = parser.next()) != parser.END_DOCUMENT) {
            if (eventType == parser.END_TAG) {
                if (parser.getName().equals(tagName)) {
                    name[0] = valueName;
                    //System.out.println("Returning value for " + valueName + ": " + res);
                    return res;
                }
                throw new XmlPullParserException(
                        "Unexpected end tag in <" + tagName + ">: " + parser.getName());
            } else if (eventType == parser.TEXT) {
                throw new XmlPullParserException(
                        "Unexpected text in <" + tagName + ">: " + parser.getName());
            } else if (eventType == parser.START_TAG) {
                throw new XmlPullParserException(
                        "Unexpected start tag in <" + tagName + ">: " + parser.getName());
            }
        }
        throw new XmlPullParserException(
                "Unexpected end of document in <" + tagName + ">");
    }

    private static final Object readThisPrimitiveValueXml(XmlPullParser parser, String tagName)
            throws XmlPullParserException, IOException {
        if (tagName.equals("int")) {
            return readIntAttribute(parser, "value");
        } else if (tagName.equals("long")) {
            return readLongAttribute(parser, "value");
        } else if (tagName.equals("float")) {
            return readFloatAttribute(parser, "value");
        } else if (tagName.equals("double")) {
            return readDoubleAttribute(parser, "value");
        } else if (tagName.equals("boolean")) {
            return readBooleanAttribute(parser, "value");
        } else {
            return null;
        }
    }


    public static final void beginDocument(XmlPullParser parser, String firstElementName) throws XmlPullParserException, IOException {
        int type;
        while ((type = parser.next()) != parser.START_TAG
                && type != parser.END_DOCUMENT) {
            ;
        }

        if (type != parser.START_TAG) {
            throw new XmlPullParserException("No start tag found");
        }

        if (!parser.getName().equals(firstElementName)) {
            throw new XmlPullParserException("Unexpected start tag: found " + parser.getName() +
                    ", expected " + firstElementName);
        }
    }


    public static final void nextElement(XmlPullParser parser) throws XmlPullParserException, IOException {
        int type;
        while ((type = parser.next()) != parser.START_TAG
                && type != parser.END_DOCUMENT) {
            ;
        }
    }

    public static boolean nextElementWithin(XmlPullParser parser, int outerDepth)
            throws IOException, XmlPullParserException {
        for (; ; ) {
            int type = parser.next();
            if (type == XmlPullParser.END_DOCUMENT
                    || (type == XmlPullParser.END_TAG && parser.getDepth() == outerDepth)) {
                return false;
            }
            if (type == XmlPullParser.START_TAG
                    && parser.getDepth() == outerDepth + 1) {
                return true;
            }
        }
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static int readIntAttribute(XmlPullParser in, String name, int defaultValue) {
        final String value = in.getAttributeValue(null, name);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static int readIntAttribute(XmlPullParser in, String name) throws IOException {
        final String value = in.getAttributeValue(null, name);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            throw new ProtocolException("problem parsing " + name + "=" + value + " as int");
        }
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static void writeIntAttribute(XmlSerializer out, String name, int value)
            throws IOException {
        out.attribute(null, name, Integer.toString(value));
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static long readLongAttribute(XmlPullParser in, String name, long defaultValue) {
        final String value = in.getAttributeValue(null, name);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static long readLongAttribute(XmlPullParser in, String name) throws IOException {
        final String value = in.getAttributeValue(null, name);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException e) {
            throw new ProtocolException("problem parsing " + name + "=" + value + " as long");
        }
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static void writeLongAttribute(XmlSerializer out, String name, long value)
            throws IOException {
        out.attribute(null, name, Long.toString(value));
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static float readFloatAttribute(XmlPullParser in, String name) throws IOException {
        final String value = in.getAttributeValue(null, name);
        try {
            return Float.parseFloat(value);
        } catch (NumberFormatException e) {
            throw new ProtocolException("problem parsing " + name + "=" + value + " as long");
        }
    }

    public static double readDoubleAttribute(XmlPullParser in, String name) throws IOException {
        final String value = in.getAttributeValue(null, name);
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException e) {
            throw new ProtocolException("problem parsing " + name + "=" + value + " as long");
        }
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static void writeFloatAttribute(XmlSerializer out, String name, float value)
            throws IOException {
        out.attribute(null, name, Float.toString(value));
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static boolean readBooleanAttribute(XmlPullParser in, String name) {
        return readBooleanAttribute(in, name, false);
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static boolean readBooleanAttribute(XmlPullParser in, String name,
                                               boolean defaultValue) {
        final String value = in.getAttributeValue(null, name);
        if (TextUtils.isEmpty(value)) {
            return defaultValue;
        } else {
            return Boolean.parseBoolean(value);
        }
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static void writeBooleanAttribute(XmlSerializer out, String name, boolean value)
            throws IOException {
        out.attribute(null, name, Boolean.toString(value));
    }

    public static Uri readUriAttribute(XmlPullParser in, String name) {
        final String value = in.getAttributeValue(null, name);
        return (value != null) ? Uri.parse(value) : null;
    }

    public static void writeUriAttribute(XmlSerializer out, String name, Uri value)
            throws IOException {
        if (value != null) {
            out.attribute(null, name, value.toString());
        }
    }

    public static String readStringAttribute(XmlPullParser in, String name) {
        return in.getAttributeValue(null, name);
    }

    public static void writeStringAttribute(XmlSerializer out, String name, CharSequence value)
            throws IOException {
        if (value != null) {
            out.attribute(null, name, value.toString());
        }
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static byte[] readByteArrayAttribute(XmlPullParser in, String name) {
        final String value = in.getAttributeValue(null, name);
        if (!TextUtils.isEmpty(value)) {
            return Base64.decode(value, Base64.DEFAULT);
        } else {
            return null;
        }
    }

    @SuppressWarnings("AndroidFrameworkEfficientXml")
    public static void writeByteArrayAttribute(XmlSerializer out, String name, byte[] value)
            throws IOException {
        if (value != null) {
            out.attribute(null, name, Base64.encodeToString(value, Base64.DEFAULT));
        }
    }

    public static Bitmap readBitmapAttribute(XmlPullParser in, String name) {
        final byte[] value = readByteArrayAttribute(in, name);
        if (value != null) {
            return BitmapFactory.decodeByteArray(value, 0, value.length);
        } else {
            return null;
        }
    }

    @Deprecated
    public static void writeBitmapAttribute(XmlSerializer out, String name, Bitmap value)
            throws IOException {
        if (value != null) {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();
            value.compress(CompressFormat.PNG, 90, os);
            writeByteArrayAttribute(out, name, os.toByteArray());
        }
    }


    public interface WriteMapCallback {

        void writeUnknownObject(Object v, String name, XmlSerializer out)
                throws XmlPullParserException, IOException;
    }


    public interface ReadMapCallback {
        Object readThisUnknownObjectXml(XmlPullParser in, String tag)
                throws XmlPullParserException, IOException;
    }
}