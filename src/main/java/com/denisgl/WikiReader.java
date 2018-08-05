package com.denisgl;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.denisgl.Startup.CmAtrribute.*;
import static com.denisgl.Startup.CmType.*;

public class WikiReader {

    private static final String WIKI_URL = "https://ru.wikipedia.org/w/api.php?action=query&format=xml&list=categorymembers&cmprop=title|type|ids&cmlimit=500&cmtitle=Category:";
    private static final String WIKI_PAGE_URL = "https://ru.wikipedia.org/w/api.php?format=xml&action=query&prop=extracts&explaintext&exsectionformat=plain&pageids=";

    private static DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    private static DocumentBuilder db;

    static {
        try {
            db = dbf.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
    }

    public static void fillCmWithChildren(CategoryMember cm) {
        if (cm.getType() == page) return;

        Document doc = null;
        try {
            doc = db.parse(new URL(WIKI_URL + cm.getTitle()).openStream());
        } catch (SAXException | IOException e) {
            e.printStackTrace();
            return;
        }

        NodeList categorymembers = doc.getElementsByTagName("cm");

        int countPages = 0;
        int countSubCat = 0;
        List<CategoryMember> categoryMemberList = new ArrayList<>();
        for (int i = 0; i < categorymembers.getLength() - 1; i++) {
            Node item = categorymembers.item(i);
            NamedNodeMap attributes = item.getAttributes();

            CategoryMember categoryMember = new CategoryMember();
            categoryMember.setParent(cm);

            String typeAttr = attributes.getNamedItem(type.name()).getNodeValue();
            Startup.CmType type = getByName(typeAttr);
            categoryMember.setType(type);


            String pageId = attributes.getNamedItem(pageid.name()).getNodeValue();
            categoryMember.setPageId(Integer.parseInt(pageId));

            String titleAttr = attributes.getNamedItem(title.name()).getNodeValue();
            if (type != page) {
                titleAttr = titleAttr.substring(10); // remove word "Категрия:"
            }
            categoryMember.setTitle(titleAttr);

            String number = String.format("%03d", type == page ? ++countPages : ++countSubCat);
            categoryMember.setNumber(number);

            categoryMemberList.add(categoryMember);
        }

        cm.setChildren(categoryMemberList);
    }

    public static void fillPageText(CategoryMember cm) {
        if (cm.getType() == page) {
            try {
                Document docPage = db.parse(new URL(WIKI_PAGE_URL + cm.getPageId()).openStream());
                NodeList textSingleList = docPage.getElementsByTagName("extract");
                String textContent = textSingleList.item(0).getTextContent();
                cm.setText(textContent);
            } catch (IOException | SAXException e) {
                e.printStackTrace();
            }
        }
    }

}
