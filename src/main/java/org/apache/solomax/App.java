package org.apache.solomax;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.InvalidPropertiesFormatException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;

import org.apache.wicket.extensions.markup.html.repeater.util.SortParam;
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class App {
	private static Logger log = LoggerFactory.getLogger(App.class);
	private static final String OM_ROOT = "/home/solomax/work/openmeetings/openmeetings";
	private static final String APP_ROOT = String.format("%s/%s", OM_ROOT, "openmeetings-web/src/main/java/org/apache/openmeetings/web/app");
	private static final String ENG_XML = String.format("%s/%s", APP_ROOT, "Application.properties.xml");
	private static final String ENTRY_ELEMENT = "entry";
	private static final String KEY_ATTR = "key";
	public static final String FILE_COMMENT = ""
			+ "\n"
			+ "  Licensed to the Apache Software Foundation (ASF) under one\n"
			+ "  or more contributor license agreements.  See the NOTICE file\n"
			+ "  distributed with this work for additional information\n"
			+ "  regarding copyright ownership.  The ASF licenses this file\n"
			+ "  to you under the Apache License, Version 2.0 (the\n"
			+ "  \"License\"); you may not use this file except in compliance\n"
			+ "  with the License.  You may obtain a copy of the License at\n"
			+ "\n"
			+ "      http://www.apache.org/licenses/LICENSE-2.0\n"
			+ "\n"
			+ "  Unless required by applicable law or agreed to in writing,\n"
			+ "  software distributed under the License is distributed on an\n"
			+ "  \"AS IS\" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY\n"
			+ "  KIND, either express or implied.  See the License for the\n"
			+ "  specific language governing permissions and limitations\n"
			+ "  under the License.\n"
			+ "\n"
			+ "\n"
			+ "###############################################\n"
			+ "This File is auto-generated by the LanguageEditor\n"
			+ "to add new Languages or modify/customize it use the LanguageEditor\n"
			+ "see http://openmeetings.apache.org/LanguageEditor.html for Details\n"
			+ "###############################################\n";

	public static Document createDocument() {
		Document document = DocumentHelper.createDocument();
		document.setXMLEncoding(UTF_8.name());
		document.addComment(FILE_COMMENT);
		return document;
	}

	public static Element createRoot(Document document) {
		document.addDocType("properties", null, "http://java.sun.com/dtd/properties.dtd");
		Element root = document.addElement("properties");
		return root;
	}

	public static Element createRoot(Document document, String _root) {
		Element root = document.addElement(_root);
		return root;
	}

	public static void toXml(Writer out, Document doc) throws Exception {
		OutputFormat outformat = OutputFormat.createPrettyPrint();
		outformat.setIndentSize(1);
		outformat.setIndent("\t");
		outformat.setEncoding(UTF_8.name());
		XMLWriter writer = new XMLWriter(out, outformat);
		writer.write(doc);
		writer.flush();
		out.flush();
		out.close();
	}

	public static void toXml(File f, Document doc) throws Exception {
		toXml(new FileOutputStream(f), doc);
	}

	public static void toXml(OutputStream out, Document doc) throws Exception {
		toXml(new OutputStreamWriter(out, "UTF8"), doc);
	}

	private static Properties getLabels(File f) throws InvalidPropertiesFormatException, IOException {
		Properties props = new Properties();
		try (InputStream is = new FileInputStream(f)) {
			props.loadFromXML(is);
		}
		return props;
	}

	public static void check(TreeMap<Long, PatternStringLabel> labels, File dir) throws Exception {
		File[] files = dir.listFiles();
		for (final File file : files) {
			if (file.isDirectory() && (
					"target".equals(file.getName())
					|| "red5-server".equals(file.getName())
					|| ".settings".equals(file.getName())
					|| ".git".equals(file.getName())
					|| "assembly".equals(file.getName())
					)) {
				continue;
			}
			if (file.isDirectory()) {
				check(labels, file);
				continue;
			}
			if (".project".equals(file.getName()) || ".classpath".equals(file.getName()) || file.getName().matches("Application.*.properties.xml")) {
				continue;
			}
			final StringBuilder contents = new StringBuilder();
			try (final BufferedReader reader = new BufferedReader(new FileReader(file))) {
				while (reader.ready()) {
					contents.append(reader.readLine());
				}
			}
			if (file.getName().endsWith(".java")) {
				final String stringContents = contents.toString();
				for (Iterator<Map.Entry<Long, PatternStringLabel>> iter = labels.entrySet().iterator(); iter.hasNext(); ) {
					Map.Entry<Long, PatternStringLabel> e = iter.next();
					Matcher m = e.getValue().p.matcher(stringContents);
					if (m.find(0)) {
						log.debug("{} -> REGEX {}, '{}' [{}, {}] ({})"
								, file.getCanonicalPath().substring(OM_ROOT.length())
								, e.getValue().key
								, stringContents.substring(m.start(), m.end())
								, m.start()
								, m.end()
								, labels.size());
						iter.remove();
					}
				}
			} else if (file.getName().endsWith(".html")) {
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
				factory.setNamespaceAware(true);
				DocumentBuilder builder = factory.newDocumentBuilder();
				org.w3c.dom.Document doc = null;
				String tail = contents.toString();
				if (tail.startsWith("<?xml")) {
					tail = tail.substring("<?xml version=\"1.0\" encoding=\"UTF-8\"?>".length());
				} else if (tail.startsWith("<!DOCTYPE html>")) {
					tail = tail.substring("<!DOCTYPE html>".length());
				}
				try {
					doc = builder.parse(new InputSource(new StringReader("<?xml version=\"1.0\" encoding=\"UTF-8\"?><!DOCTYPE Tokens [<!ENTITY nbsp \"&#xa0;\">]>" + tail)));
				} catch (Exception e) {
					log.error("Unexpectederro while parsing {}, {}", file, tail);
				}
				XPathFactory xPathfactory = XPathFactory.newInstance();
				{
					XPath xpath = xPathfactory.newXPath();
					xpath.setNamespaceContext(new WicketNamespaceContext());
					XPathExpression expr = xpath.compile("//*[@wicket:message]/@wicket:message");
					NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
					for (int i = 0; i < nl.getLength(); ++i) {
						String attr = nl.item(i).getTextContent();
						String[] msgs = attr.split(",");
						for (String msg : msgs) {
							String[] parts = msg.split(":");
							try {
								Long key = Long.valueOf(parts[1]);
								PatternStringLabel lbl = labels.remove(key);
								if (lbl != null) {
									log.debug("{} -> ATTR {}, ({})"
											, file.getCanonicalPath().substring(OM_ROOT.length())
											, key
											, labels.size());
								}
							} catch (Exception e) {
								//
							}
						}
					}
				}
				for (Iterator<Map.Entry<Long, PatternStringLabel>> iter = labels.entrySet().iterator(); iter.hasNext(); ) {
					Map.Entry<Long, PatternStringLabel> e = iter.next();
					{
						XPath xpath = xPathfactory.newXPath();
						xpath.setNamespaceContext(new WicketNamespaceContext());
						XPathExpression expr = xpath.compile(String.format("//wicket:message[@key=\"%s\"]", e.getKey()));
						NodeList nl = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
						if (nl.getLength() > 0) {
							log.debug("{} -> NODE {}, ({})"
									, file.getCanonicalPath().substring(OM_ROOT.length())
									, e.getValue().key
									, labels.size());
							iter.remove();
						}
					}
				}
			}
		}
	}

	private static void storeLabels(List<StringLabel> labels, File file) throws Exception {
		Document d = createDocument();
		Element r = createRoot(d);
		for (StringLabel sl : labels) {
			r.addElement(ENTRY_ELEMENT).addAttribute(KEY_ATTR, sl.key).addCDATA(sl.value);
		}
		toXml(file, d);
	}

	private static List<StringLabel> getLabels(File file, TreeMap<Long, PatternStringLabel> excl) throws Exception {
		List<StringLabel> labels = new ArrayList<>();
		Properties lbls = getLabels(file);
		// sorting here in natural order
		lbls.forEach((k, v) -> {
			boolean willAdd = true;
			try {
				Long key = Long.valueOf((String)k);
				if (key != null && excl.containsKey(key)) {
					willAdd = false; //calendar days
				}
			} catch (Exception ee) {
				// no-op
			}
			if (willAdd) {
				labels.add(new StringLabel((String)k, (String)v));
			}
		});
		Collections.sort(labels, new LabelComparator());
		return labels;
	}

	public static void main(String[] args) throws Exception {
		Set<String> shareLabels = new HashSet<>(Arrays.asList("730",  "731",  "732",  "733",  "734"
							,  "735",  "737",  "738",  "739",  "740"
							,  "741",  "742",  "844",  "869",  "870"
							,  "871",  "872",  "878", "1089", "1090"
							, "1091", "1092", "1093", "1465", "1466"
							, "1467", "1468", "1469", "1470", "1471"
							, "1472", "1473", "1474", "1475", "1476"
							, "1477", "1589", "1598", "1078"
							// additional interview
							, "1386", "913", "914"
							// main menu
							, "124", "582"
							, "290", "1450"
							, "291", "1451"
							, "792", "793"
							, "777", "1506"
							, "779", "1507"
							, "781", "1508"
							, "395", "583"
							, "395", "1452"
							, "6", "586"
							, "125", "1454"
							, "597", "1455"
							, "127", "1456"
							, "186", "1457"
							, "263", "1458"
							, "348", "1459"
							, "1103", "1460"
							, "1571", "1572"
							, "367", "1461"
							// contacts and messages
							, "1252" ,"1239", "1240", "1241", "1242"
							// icons
							, "688", "675", "81", "676"
							, "689", "612", "686", "694"
							// room client statuses
							, "677", "678", "679"
				));
		Properties lbls = getLabels(new File(ENG_XML));
		TreeMap<Long, PatternStringLabel> labels = new TreeMap<>();
		for (Map.Entry<?, ?> e : lbls.entrySet()) {
			if (e.getKey() instanceof String) {
				try {
					if (shareLabels.contains(e.getKey())) {
						continue;
					}
					Long key = Long.valueOf((String)e.getKey());
					if (key != null) {
						if (key.longValue() > 452 && key.longValue() < 467) {
							continue; //calendar days
						}
						if (key.longValue() > 468 && key.longValue() < 481) {
							continue; //calendar months
						}
						if (key.longValue() > 776 && key.longValue() < 783) {
							continue; //room selector
						}
						labels.put(key, new PatternStringLabel(key, (String)e.getValue()));
					}
				} catch (Exception ee) {
					// no-op
				}
			}
		}
		// check duplicates
		TreeMap<Long, PatternStringLabel> dlabels = new TreeMap<>(labels);
		Map<Long, Set<Long>> duplicates = new TreeMap<>();
		for (Map.Entry<Long, PatternStringLabel> e : labels.entrySet()) {
			String val = e.getValue().value;
			for (Iterator<Map.Entry<Long, PatternStringLabel>> iter = dlabels.entrySet().iterator(); iter.hasNext(); ) {
				Map.Entry<Long, PatternStringLabel> ee = iter.next();
				if (e.getKey().equals(ee.getKey())) {
					iter.remove();
					continue;
				}
				if (val.equals(ee.getValue().value)) {
					if (!duplicates.containsKey(e.getKey())) {
						duplicates.put(e.getKey(), new TreeSet<>());
					}
					duplicates.get(e.getKey()).add(ee.getKey());
					iter.remove();
				}
			}
		}
		log.debug("Duplicates are: {}", duplicates);
		check(labels, new File(OM_ROOT));
		for (Map.Entry<Long, PatternStringLabel> e : labels.entrySet()) {
			log.info("KEY {} is NOT used", e.getKey());
		}
		File[] langs = new File(APP_ROOT).listFiles(new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.matches("Application.*.properties.xml");
			}
		});
		for (File f : langs) {
			storeLabels(getLabels(f, labels), f);
		}
	}

	private static class StringLabel {
		final String key;
		final String value;
		StringLabel(String key, String value) {
			this.key = key;
			this.value = value;
		}
		public String getKey() {
			return key;
		}
		public String getValue() {
			return value;
		}
	}

	private static class PatternStringLabel {
		final Long key;
		@SuppressWarnings("unused")
		final String value;
		final Pattern p;
		@SuppressWarnings("unused")
		boolean used;
		PatternStringLabel(Long key, String value) {
			this.key = key;
			this.value = value;
			p = Pattern.compile(String.format("(?:getString|wicket[:]message).{0,120}(?:[\":]%s[\", ])", key), Pattern.MULTILINE);
		}
	}

	private static class LabelComparator implements Comparator<StringLabel> {
		final SortParam<String> sort;

		LabelComparator() {
			this.sort = new SortParam<>(KEY_ATTR, true);
		}

		@SuppressWarnings("unused")
		LabelComparator(SortParam<String> sort) {
			this.sort = sort;
		}

		@Override
		public int compare(StringLabel o1, StringLabel o2) {
			int val = 0;
			if (KEY_ATTR.equals(sort.getProperty())) {
				try {
					int i1 = Integer.parseInt(o1.getKey()), i2 = Integer.parseInt(o2.getKey());
					val = i1 - i2;
				} catch (Exception e) {
					val = o1.getKey().compareTo(o2.getKey());
				}
			} else {
				val = o1.getValue().compareTo(o2.getValue());
			}
			return (sort.isAscending() ? 1 : -1) * val;
		}
	}

	private static class WicketNamespaceContext implements NamespaceContext {
		@Override
		public String getPrefix(String namespaceURI) {
			return null;
		}

		@Override
		public Iterator getPrefixes(String namespaceURI) {
			return null;
		}

		@Override
		public String getNamespaceURI(String prefix) {
			if (prefix == null) {
				throw new NullPointerException("Invalid Namespace Prefix");
			} else if ("wicket".equals(prefix)) {
				return "http://wicket.apache.org";
			} else {
				return XMLConstants.NULL_NS_URI;
			}
		}
	}
}
